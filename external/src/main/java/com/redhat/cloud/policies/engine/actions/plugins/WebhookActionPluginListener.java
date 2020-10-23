package com.redhat.cloud.policies.engine.actions.plugins;

import com.redhat.cloud.notifications.ingress.Action;
import com.redhat.cloud.notifications.ingress.Context;
import com.redhat.cloud.notifications.ingress.Tag;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.io.JsonEncoder;
import org.apache.avro.specific.SpecificDatumWriter;
import org.eclipse.microprofile.metrics.Counter;
import org.eclipse.microprofile.metrics.annotation.Metric;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.OnOverflow;
import org.hawkular.alerts.actions.api.ActionMessage;
import org.hawkular.alerts.actions.api.ActionPluginListener;
import org.hawkular.alerts.actions.api.Plugin;
import org.hawkular.alerts.api.model.condition.ConditionEval;
import org.hawkular.alerts.api.model.condition.EventConditionEval;
import org.hawkular.alerts.api.model.trigger.Trigger;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * WebhookActionPluginListener sends a JSON encoded message in the format understood by the
 * notifications-backend in cloud.redhat.com. The encoding schema is defined in Avro and generated during
 * the compile.
 */
@Plugin(name = "webhook")
@Dependent
public class WebhookActionPluginListener implements ActionPluginListener {
    public static final String APP_NAME = "Policies";
    public static final String EVENT_TYPE_NAME = "All";

    @Inject
    @Channel("webhook")
    @OnOverflow(OnOverflow.Strategy.UNBOUNDED_BUFFER)
    Emitter<String> channel;

    @Inject
    @Metric(absolute = true, name = "engine.actions.webhook.processed")
    Counter messagesCount;

    @Override
    public void process(ActionMessage actionMessage) throws Exception {
        messagesCount.inc();
        Action webhookAction = new Action();
        webhookAction.setEventType(EVENT_TYPE_NAME);
        webhookAction.setApplication(APP_NAME);
        webhookAction.setTimestamp(LocalDateTime.from(Instant.ofEpochMilli(actionMessage.getAction().getCtime())));
        webhookAction.setEventId(actionMessage.getAction().getEventId());
        List<Tag> tags = new ArrayList<>();
        for (Map.Entry<String, String> tagEntry : actionMessage.getAction().getEvent().getTags().entries()) {
            Tag tag = new Tag(tagEntry.getKey(), tagEntry.getValue());
            tags.add(tag);
        }

        webhookAction.setTags(tags);
        Context context = new Context();
        context.setAccountId(actionMessage.getAction().getTenantId());

        // Add the wanted properties here..
        Trigger trigger = actionMessage.getAction().getEvent().getTrigger();
        context.put("policy_id", trigger.getId());
        context.put("policy_name", trigger.getName());
        context.put("policy_description", trigger.getDescription());

        Outer:
        for (Set<ConditionEval> evalSet : actionMessage.getAction().getEvent().getEvalSets()) {
            for (ConditionEval conditionEval : evalSet) {
                if (conditionEval instanceof EventConditionEval) {
                    EventConditionEval eventEval = (EventConditionEval) conditionEval;
                    context.put("policy_condition", eventEval.getCondition().getExpression());

                    context.put("insights_id", eventEval.getContext().get("insights_id"));
                    context.put("display_name", eventEval.getValue().getTags().get("display_name").iterator().next());
                    break Outer; // We only want to process the first one
                }
            }
        }

        webhookAction.setEvent(context);
        channel.send(serializeAction(webhookAction));
    }

    @Override
    public void flush() {

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

    public static String serializeAction(Action action) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JsonEncoder jsonEncoder = EncoderFactory.get().jsonEncoder(Action.getClassSchema(), baos);
        DatumWriter<Action> writer = new SpecificDatumWriter<>(Action.class);
        writer.write(action, jsonEncoder);
        jsonEncoder.flush();

        return baos.toString(StandardCharsets.UTF_8);
    }
}
