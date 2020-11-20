package com.redhat.cloud.policies.engine.actions.plugins;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * NotificationActionPluginListener sends a JSON encoded message in the format understood by the
 * notifications-backend in cloud.redhat.com. The encoding schema is defined in Avro and generated during
 * the compile.
 */
@Plugin(name = "notification")
@Dependent
public class NotificationActionPluginListener implements ActionPluginListener {
    public static final String APP_NAME = "Policies";
    public static final String EVENT_TYPE_NAME = "All";

    @Inject
    @Channel("webhook")
    @OnOverflow(OnOverflow.Strategy.UNBOUNDED_BUFFER)
    Emitter<String> channel;

    @Inject
    @Metric(absolute = true, name = "engine.actions.webhook.processed")
    Counter messagesCount;

    class PoliciesParamsBuilder {
        private Map<String, String> triggers;

        public PoliciesParamsBuilder() {
            this.triggers = new HashMap<>();
        }

        public PoliciesParamsBuilder addTrigger(String key, String value) {
            this.triggers.put(key, value);
            return this;
        }

        public String build() throws JsonProcessingException {
            Map<String, Object> params = new HashMap<>();
            params.put("triggers", this.triggers);
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(params);
        }

    }

    @Override
    public void process(ActionMessage actionMessage) throws Exception {
        messagesCount.inc();
        Action notificationAction = new Action();
        notificationAction.setEventType(EVENT_TYPE_NAME);
        notificationAction.setApplication(APP_NAME);
        notificationAction.setTimestamp(LocalDateTime.ofInstant(Instant.ofEpochMilli(actionMessage.getAction().getCtime()), ZoneId.systemDefault()));
        notificationAction.setEventId(actionMessage.getAction().getEventId());
        List<Tag> tags = new ArrayList<>();
        for (Map.Entry<String, String> tagEntry : actionMessage.getAction().getEvent().getTags().entries()) {
            String value = tagEntry.getValue();
            if (value == null) {
                // Same behavior as previously with JsonObjectNoNullSerializer with old hooks
                value = "";
            }
            Tag tag = new Tag(tagEntry.getKey(), value);
            tags.add(tag);
        }

        notificationAction.setTags(tags);
        Context context = new Context();
        context.setAccountId(actionMessage.getAction().getTenantId());
        context.setMessage(new HashMap<>());
        notificationAction.setEvent(context);

        // Add the wanted properties here..
        Trigger trigger = actionMessage.getAction().getEvent().getTrigger();
        addToMessage(notificationAction, "policy_id", trigger.getId());
        addToMessage(notificationAction, "policy_name", trigger.getName());
        addToMessage(notificationAction, "policy_description", trigger.getDescription());


        PoliciesParamsBuilder paramsBuilder = new PoliciesParamsBuilder();

        for (Set<ConditionEval> evalSet : actionMessage.getAction().getEvent().getEvalSets()) {
            for (ConditionEval conditionEval : evalSet) {
                if (conditionEval instanceof EventConditionEval) {
                    EventConditionEval eventEval = (EventConditionEval) conditionEval;
                    addToMessage(notificationAction, "policy_condition", eventEval.getCondition().getExpression());

                    addToMessage(notificationAction, "insights_id", eventEval.getContext().get("insights_id"));
                    addToMessage(notificationAction, "display_name", eventEval.getValue().getTags().get("display_name").iterator().next());

                    String name = actionMessage.getAction().getEvent().getTrigger().getName();
                    String triggerId = actionMessage.getAction().getEvent().getTrigger().getId();

                    paramsBuilder.addTrigger(triggerId, name);
                }
            }
        }

        notificationAction.setParams(paramsBuilder.build());

        channel.send(serializeAction(notificationAction));
    }

    private void addToMessage(Action action, String key, String value) {
        // Adding a null value is not allowed by the Avro schema
        if (key != null && value != null) {
            action.getEvent().getMessage().put(key, value);
        }
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
