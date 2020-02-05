package com.redhat.cloud.custompolicies.engine.actions.plugins;

import com.redhat.cloud.custompolicies.engine.process.Receiver;
import io.quarkus.runtime.StartupEvent;
import io.smallrye.reactive.messaging.annotations.Channel;
import io.smallrye.reactive.messaging.annotations.Emitter;
import io.vertx.core.json.JsonObject;
import org.hawkular.alerts.actions.api.ActionMessage;
import org.hawkular.alerts.actions.api.ActionPluginListener;
import org.hawkular.alerts.actions.api.Plugin;
import org.hawkular.alerts.api.model.condition.ConditionEval;
import org.hawkular.alerts.api.model.condition.EventConditionEval;
import org.hawkular.commons.log.MsgLogger;
import org.hawkular.commons.log.MsgLogging;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;

@Plugin(name = "email")
public class EmailActionPluginListener implements ActionPluginListener {

    private final MsgLogger log = MsgLogging.getMsgLogger(EmailActionPluginListener.class);

    @Inject
    @Channel("email")
    Emitter<JsonObject> channel;

    void findLimits(@Observes StartupEvent event) {
        System.out.println(event.toString());
    }

    public EmailActionPluginListener() {
        notifyBuffer = new ConcurrentSkipListMap<>();
    }

    private ConcurrentSkipListMap<String, Notification> notifyBuffer = null;

    @Override
    public void process(ActionMessage actionMessage) throws Exception {
        for (Set<ConditionEval> evalSet : actionMessage.getAction().getEvent().getEvalSets()) {
            for (ConditionEval conditionEval : evalSet) {
                EventConditionEval eventEval = (EventConditionEval) conditionEval;
                String insightId = eventEval.getContext().get(Receiver.INSIGHT_ID_FIELD);
                if(insightId == null) {
                    // Fallback, this won't merge anything
                    insightId = actionMessage.getAction().getEventId();
                }
                log.infof("Processing insightReport action %s\n", insightId);
                Map<String, String> tags = actionMessage.getAction().getEvent().getTags();
                String name = actionMessage.getAction().getEvent().getTrigger().getName();

                Notification notification = new Notification();
                notification.getTriggerNames().add(name);
                notification.getTags().putAll(tags);

                notifyBuffer.merge(insightId, notification, (existing, addition) -> {
                    existing.getTags().putAll(addition.getTags());
                    existing.getTriggerNames().addAll(addition.getTriggerNames());

                    return existing;
                });
            }
        }
    }

    @Override
    public void flush() {
        log.info("Starting flush of email messages");
        for (; ; ) {
            Map.Entry<String, Notification> notificationEntry = notifyBuffer.pollFirstEntry();
            if (notificationEntry == null) {
                break;
            }
            Notification notification = notificationEntry.getValue();
            log.infof("Sending %s", notificationEntry.getKey());
            channel.send(JsonObject.mapFrom(notification));
        }
    }

    @Override
    public Set<String> getProperties() {
        Set<String> properties = new HashSet<>();
        properties.add("from");
        properties.add("to");
        properties.add("cc");
        return properties;
    }

    @Override
    public Map<String, String> getDefaultProperties() {
        return new HashMap<>();
    }

    /**
     * This supports the current
     */
    private static class Notification {
        private Map<String, String> tags;
        private Set<String> triggerNames;

        public Notification() {
            this.tags = new HashMap<>();
            this.triggerNames = new HashSet<>();
        }

        public Map<String, String> getTags() {
            return tags;
        }

        public Set<String> getTriggerNames() {
            return triggerNames;
        }
    }
}
