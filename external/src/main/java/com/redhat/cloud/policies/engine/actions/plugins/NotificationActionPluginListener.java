package com.redhat.cloud.policies.engine.actions.plugins;

import com.redhat.cloud.notifications.ingress.Action;
import com.redhat.cloud.notifications.ingress.Context;
import com.redhat.cloud.notifications.ingress.Event;
import com.redhat.cloud.notifications.ingress.Metadata;
import com.redhat.cloud.notifications.ingress.Parser;
import com.redhat.cloud.notifications.ingress.Payload;
import com.redhat.cloud.policies.engine.actions.plugins.notification.PoliciesAction;
import io.smallrye.reactive.messaging.kafka.api.OutgoingKafkaRecordMetadata;
import io.vertx.core.json.JsonObject;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.eclipse.microprofile.metrics.Counter;
import org.eclipse.microprofile.metrics.annotation.Metric;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.OnOverflow;
import org.hawkular.alerts.actions.api.ActionMessage;
import org.hawkular.alerts.actions.api.ActionPluginListener;
import org.hawkular.alerts.actions.api.Plugin;
import org.hawkular.alerts.api.model.condition.ConditionEval;
import org.hawkular.alerts.api.model.condition.EventConditionEval;
import org.hawkular.alerts.api.model.trigger.Trigger;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * NotificationActionPluginListener sends a JSON encoded message in the format understood by the
 * notifications-backend in cloud.redhat.com. The encoding schema is defined in Avro and generated during
 * the compile.
 */
@Plugin(name = "notification")
@Dependent
public class NotificationActionPluginListener implements ActionPluginListener {
    public static final String BUNDLE_NAME = "rhel";
    public static final String APP_NAME = "policies";
    public static final String EVENT_TYPE_NAME = "policy-triggered";
    public static final String WEBHOOK_CHANNEL = "webhook";
    public static final String MESSAGE_ID_HEADER = "rh-message-id";

    private static final Logger log = Logger.getLogger(NotificationActionPluginListener.class.getName());
    private final ConcurrentSkipListMap<String, PoliciesAction> notifyBuffer = new ConcurrentSkipListMap<>();

    @Inject
    @Channel(WEBHOOK_CHANNEL)
    @OnOverflow(OnOverflow.Strategy.UNBOUNDED_BUFFER)
    Emitter<String> channel;

    @Inject
    @Metric(absolute = true, name = "engine.actions.notifications.processed")
    Counter messagesCount;

    @Inject
    @Metric(absolute = true, name = "engine.actions.notifications.aggregated")
    Counter messagesAggregated;

    @Override
    public void process(ActionMessage actionMessage) throws Exception {
        messagesCount.inc();
        PoliciesAction policiesAction = new PoliciesAction();
        policiesAction.setAccountId(actionMessage.getAction().getTenantId());
        policiesAction.setTimestamp(
            LocalDateTime.ofInstant(Instant.ofEpochMilli(actionMessage.getAction().getCtime()), ZoneOffset.UTC)
        );

        PoliciesAction.Context context = policiesAction.getContext();
        Set<PoliciesAction.Event> events = policiesAction.getEvents();

        for (Map.Entry<String, String> tagEntry : actionMessage.getAction().getEvent().getTags().entries()) {
            String value = tagEntry.getValue();
            if (value == null) {
                // Same behavior as previously with JsonObjectNoNullSerializer with old hooks
                value = "";
            }

            context.getTags().computeIfAbsent(tagEntry.getKey(), _key -> new HashSet<>()).add(value);
        }

        Trigger trigger = actionMessage.getAction().getEvent().getTrigger();

        for (Set<ConditionEval> evalSet : actionMessage.getAction().getEvent().getEvalSets()) {
            for (ConditionEval conditionEval : evalSet) {
                if (conditionEval instanceof EventConditionEval) {
                    EventConditionEval eventEval = (EventConditionEval) conditionEval;

                    context.setSystemCheckIn(LocalDateTime.from(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(eventEval.getContext().get("check_in"))));
                    context.setInventoryId(eventEval.getContext().get("inventory_id"));
                    context.setDisplayName(eventEval.getValue().getTags().get("display_name").iterator().next());

                    PoliciesAction.Event event = new PoliciesAction.Event();
                    event.getPayload().setPolicyCondition(eventEval.getCondition().getExpression());
                    event.getPayload().setPolicyId(trigger.getId());
                    event.getPayload().setPolicyName(trigger.getName());
                    event.getPayload().setPolicyDescription(trigger.getDescription());

                    policiesAction.getEvents().add(event);
                    break;
                }
            }
        }

        notifyBuffer.merge(policiesAction.getKey(), policiesAction,(existing, addition) -> {
            for (Map.Entry<String, Set<String>> tagEntry : addition.getContext().getTags().entrySet()) {
                existing.getContext().getTags().merge(tagEntry.getKey(), tagEntry.getValue(), (existingTags, additionTags) -> {
                    existingTags.addAll(additionTags);
                    return existingTags;
                });
            }

            existing.getEvents().addAll(addition.getEvents());

            return existing;
        });
    }

    @Override
    public void flush() {
        log.fine(() -> String.format("Starting flush of %d email messages", notifyBuffer.size()));

        while (true) {
            Map.Entry<String, PoliciesAction> notificationEntry = notifyBuffer.pollFirstEntry();
            if (notificationEntry == null) {
                break;
            }

            PoliciesAction action = notificationEntry.getValue();
            try {
                String payload = serializeAction(action);
                channel.send(buildMessageWithId(payload));
                messagesAggregated.inc();
            } catch (IOException ex) {
                log.log(Level.WARNING, ex, () -> "Failed to serialize action for accountId" + action.getAccountId());
            }

        }
    }

    @Override
    public Set<String> getProperties() {
        Set<String> properties = new HashSet<>();
        properties.add("endpoint_id");
        return properties;
    }

    @Override
    public Map<String, String> getDefaultProperties() {
        Map<String, String> defaultProperties = new HashMap<>();
        defaultProperties.put("_managed", "true");
        defaultProperties.put("endpoint_id", "");
        return defaultProperties;
    }

    private String serializeAction(PoliciesAction policiesAction) throws IOException {

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

                    return new Event.EventBuilder()
                            .withMetadata(new Metadata.MetadataBuilder().build())
                            .withPayload(payloadBuilder.build())
                            .build();
                }).collect(Collectors.toList()))
                .build();

        return Parser.encode(action);
    }

    private static Message buildMessageWithId(String payload) {
        byte[] messageId = UUID.randomUUID().toString().getBytes(UTF_8);
        OutgoingKafkaRecordMetadata metadata = OutgoingKafkaRecordMetadata.builder()
                .withHeaders(new RecordHeaders().add(MESSAGE_ID_HEADER, messageId))
                .build();
        return Message.of(payload).addMetadata(metadata);
    }
}
