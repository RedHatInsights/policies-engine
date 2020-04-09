package com.redhat.cloud.policies.engine.actions.plugins;

import io.vertx.core.json.JsonObject;
import org.eclipse.microprofile.metrics.Counter;
import org.eclipse.microprofile.metrics.annotation.Metric;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.hawkular.alerts.actions.api.ActionMessage;
import org.hawkular.alerts.actions.api.ActionPluginListener;
import org.hawkular.alerts.actions.api.Plugin;
import org.hawkular.alerts.api.model.action.Action;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Plugin(name = "webhook")
@Dependent
public class WebhookActionPluginListener implements ActionPluginListener {
    @Inject
    @Channel("webhook")
    Emitter<JsonObject> channel;

    @Inject
    @Metric(absolute = true, name = "engine.actions.webhook.processed")
    Counter messagesCount;

    @Override
    public void process(ActionMessage actionMessage) throws Exception {
        Action action = actionMessage.getAction();
        messagesCount.inc();
        channel.send(JsonObject.mapFrom(action));
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
}
