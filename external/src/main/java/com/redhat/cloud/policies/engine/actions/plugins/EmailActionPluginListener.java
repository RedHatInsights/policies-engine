package com.redhat.cloud.policies.engine.actions.plugins;

import com.google.common.collect.Multimap;
import com.redhat.cloud.policies.engine.process.Receiver;
import io.vertx.core.json.JsonObject;
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
import org.hawkular.alerts.log.MsgLogger;
import org.hawkular.alerts.log.MsgLogging;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;

@Plugin(name = "email")
@Dependent
public class EmailActionPluginListener implements ActionPluginListener {

    private final MsgLogger log = MsgLogging.getMsgLogger(EmailActionPluginListener.class);

    @Inject
    @Channel("email")
    @OnOverflow(OnOverflow.Strategy.UNBOUNDED_BUFFER)
    Emitter<JsonObject> channel;

    @Inject
    @Metric(absolute = true, name = "engine.actions.email.processed")
    Counter messagesCount;

    @Inject
    @Metric(absolute = true, name = "engine.actions.email.processed.aggregated")
    Counter messagesCountAggregated;

    public EmailActionPluginListener() {
        notifyBuffer = new ConcurrentSkipListMap<>();
    }

    private ConcurrentSkipListMap<String, Notification> notifyBuffer = null;

    @Override
    public void process(ActionMessage actionMessage) throws Exception {
        for (Set<ConditionEval> evalSet : actionMessage.getAction().getEvent().getEvalSets()) {
            for (ConditionEval conditionEval : evalSet) {
                if(conditionEval instanceof EventConditionEval) {
                    String tenantId = actionMessage.getAction().getTenantId();
                    EventConditionEval eventEval = (EventConditionEval) conditionEval;
                    String insightId = eventEval.getContext().get(Receiver.INSIGHT_ID_FIELD);
                    if(insightId == null) {
                        // Fallback, this won't merge anything
                        insightId = eventEval.getValue().getId();
                    }
                    Multimap<String, String> tags = eventEval.getValue().getTags();
                    String name = actionMessage.getAction().getEvent().getTrigger().getName();
                    String triggerId = actionMessage.getAction().getEvent().getTrigger().getId();

                    Notification notification = new Notification(tenantId, insightId);
                    Map<String, String> notificationTags = notification.getTags();
                    tags.entries().forEach(entry -> {
                        if(entry.getValue() != null && entry.getValue().length() > 0) {
                            notificationTags.put(entry.getKey(), entry.getValue());
                        }
                    });
                    notification.getTriggers().put(triggerId, name);

                    notifyBuffer.merge(insightId, notification, (existing, addition) -> {
                        existing.getTags().putAll(addition.getTags());
                        existing.getTriggers().putAll(addition.getTriggers());

                        return existing;
                    });

                    messagesCount.inc();
                }
            }
        }
    }

    @Override
    public void flush() {
        if(log.isDebugEnabled()) { // buffer .size() is expensive and blocks processing
            log.debugf("Starting flush of %d email messages", notifyBuffer.size());
        }
        for (; ; ) {
            Map.Entry<String, Notification> notificationEntry = notifyBuffer.pollFirstEntry();
            if (notificationEntry == null) {
                break;
            }
            Notification notification = notificationEntry.getValue();
            channel.send(JsonObject.mapFrom(notification));
            messagesCountAggregated.inc();
        }
    }

    @Override
    public Set<String> getProperties() {
        return new HashSet<>();
    }

    @Override
    public Map<String, String> getDefaultProperties() {
        Map<String, String> defaultProperties = new HashMap<>();
        defaultProperties.put("_managed", "true");
        return defaultProperties;
    }

    /**
     * This supports the current requirements for policies mails
     */
    private static class Notification {
        private String tenantId;
        private String insightId;
        private Map<String, String> triggers;
        private Map<String, String> tags;

        public Notification(String tenantId, String insightId) {
            this.tenantId = tenantId;
            this.insightId = insightId;
            this.triggers = new HashMap<>();
            this.tags = new HashMap<>();
        }

        public String getTenantId() {
            return tenantId;
        }

        public Map<String, String> getTags() {
            return tags;
        }

        public String getInsightId() {
            return insightId;
        }

        public Map<String, String> getTriggers() {
            return triggers;
        }
    }
}
