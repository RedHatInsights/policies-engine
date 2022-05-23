package com.redhat.cloud.policies.engine.process;

import com.redhat.cloud.notifications.ingress.Action;
import com.redhat.cloud.notifications.ingress.Context;
import com.redhat.cloud.notifications.ingress.Metadata;
import com.redhat.cloud.notifications.ingress.Parser;
import com.redhat.cloud.notifications.ingress.Payload;
import io.smallrye.reactive.messaging.kafka.api.OutgoingKafkaRecordMetadata;
import io.vertx.core.json.JsonObject;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.eclipse.microprofile.metrics.Counter;
import org.eclipse.microprofile.metrics.annotation.Metric;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.OnOverflow;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.eclipse.microprofile.reactive.messaging.OnOverflow.Strategy.UNBOUNDED_BUFFER;

@ApplicationScoped
public class NotificationSender {

    public static final String WEBHOOK_CHANNEL = "webhook";
    public static final String MESSAGE_ID_HEADER = "rh-message-id";
    public static final String BUNDLE_NAME = "rhel";
    public static final String APP_NAME = "policies";
    public static final String EVENT_TYPE_NAME = "policy-triggered";

    private static final Logger LOGGER = Logger.getLogger(NotificationSender.class);

    @Inject
    @Channel(WEBHOOK_CHANNEL)
    @OnOverflow(UNBOUNDED_BUFFER)
    Emitter<String> emitter;

    @Inject
    @Metric(absolute = true, name = "engine.actions.notifications.aggregated")
    Counter notificationsCounter;

    public void send(PoliciesAction policiesAction) {
        String payload = serializeAction(policiesAction);
        LOGGER.debugf("Sending Kafka payload %s", payload);
        Message<String> message = buildMessageWithId(payload);
        emitter.send(message);
        notificationsCounter.inc();
    }

    private static String serializeAction(PoliciesAction policiesAction) {
        Context.ContextBuilder contextBuilder = new Context.ContextBuilder();
        Map<String, Object> context = JsonObject.mapFrom(policiesAction.getContext()).getMap();
        context.forEach(contextBuilder::withAdditionalProperty);

        Action action = new Action.ActionBuilder()
                .withBundle(BUNDLE_NAME)
                .withApplication(APP_NAME)
                .withEventType(EVENT_TYPE_NAME)
                .withAccountId(policiesAction.getAccountId())
                .withTimestamp(policiesAction.getTimestamp())
                .withContext(contextBuilder.build())
                .withEvents(policiesAction.getEvents().stream().map(event -> {

                    Payload.PayloadBuilder payloadBuilder = new Payload.PayloadBuilder();
                    Map<String, Object> payload = JsonObject.mapFrom(event.getPayload()).getMap();
                    payload.forEach(payloadBuilder::withAdditionalProperty);

                    return new com.redhat.cloud.notifications.ingress.Event.EventBuilder()
                            .withMetadata(new Metadata.MetadataBuilder().build())
                            .withPayload(payloadBuilder.build())
                            .build();
                }).collect(Collectors.toList()))
                .build();

        return Parser.encode(action);
    }

    private static Message<String> buildMessageWithId(String payload) {
        byte[] messageId = UUID.randomUUID().toString().getBytes(UTF_8);
        OutgoingKafkaRecordMetadata metadata = OutgoingKafkaRecordMetadata.builder()
                .withHeaders(new RecordHeaders().add(MESSAGE_ID_HEADER, messageId))
                .build();
        return Message.of(payload).addMetadata(metadata);
    }
}
