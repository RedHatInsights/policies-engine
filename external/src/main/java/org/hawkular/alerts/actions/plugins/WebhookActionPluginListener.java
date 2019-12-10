package org.hawkular.alerts.actions.plugins;

import io.quarkus.runtime.StartupEvent;
import io.smallrye.reactive.messaging.annotations.Channel;
import io.smallrye.reactive.messaging.annotations.Emitter;
import io.vertx.core.json.JsonObject;
import org.hawkular.alerts.actions.api.ActionMessage;
import org.hawkular.alerts.actions.api.ActionPluginListener;
import org.hawkular.alerts.actions.api.Plugin;
import org.hawkular.alerts.api.model.action.Action;
import org.hawkular.alerts.api.model.event.Event;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Plugin(name = "webhook")
public class WebhookActionPluginListener implements ActionPluginListener {
    @Inject
    @Channel("webhook")
    Emitter<JsonObject> channel;

    void findLimits(@Observes StartupEvent event) {
        System.out.println(event.toString());
    }

    @Override
    public void process(ActionMessage actionMessage) throws Exception {
        System.out.println("process was fired in the WebhookActionPluginListener");
        Action action = actionMessage.getAction();
        Event actionEvent = action.getEvent();
        JsonObject actionJson = new JsonObject();
        // Example mapping, we could add payload, method, timeout etc for example
        actionJson.put("url", action.getProperties().get("url"));

        // TODO Do we want something for
        channel.send(actionJson);
    }

    @Override
    public Set<String> getProperties() {
        Set<String> properties = new HashSet<>();
        properties.add("url");
        return properties;
    }

    @Override
    public Map<String, String> getDefaultProperties() {
        return new HashMap<>();
    }
}
