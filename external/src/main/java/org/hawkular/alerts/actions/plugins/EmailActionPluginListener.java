package org.hawkular.alerts.actions.plugins;

import io.quarkus.runtime.StartupEvent;
import io.smallrye.reactive.messaging.annotations.Channel;
import io.smallrye.reactive.messaging.annotations.Emitter;
import io.vertx.core.json.JsonObject;
import org.hawkular.alerts.actions.api.ActionMessage;
import org.hawkular.alerts.actions.api.ActionPluginListener;
import org.hawkular.alerts.actions.api.Plugin;
import org.hawkular.alerts.api.json.JsonUtil;
import org.hawkular.alerts.api.model.action.Action;
import org.hawkular.alerts.api.model.event.Event;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Plugin(name = "email")
public class EmailActionPluginListener implements ActionPluginListener {
    @Inject
    @Channel("email")
    Emitter<String> channel;

    void findLimits(@Observes StartupEvent event) {
        System.out.println(event.toString());
    }

    @Override
    public void process(ActionMessage actionMessage) throws Exception {
        channel.send(JsonUtil.toJson(actionMessage));
//        Action action = actionMessage.getAction();
//        Event actionEvent = action.getEvent();
//        JsonObject actionJson = new JsonObject();
//        // TODO Example mapping, we could add payload, method, timeout etc for example
//        actionJson.put("to", action.getProperties().get("to"));
//        actionJson.put("from", action.getProperties().get("from"));
//        channel.send(actionJson);
    }

    @Override
    public Set<String> getProperties() {
        Set<String> properties = new HashSet<>();
        properties.add("from");
        properties.add("to");
        return properties;
    }

    @Override
    public Map<String, String> getDefaultProperties() {
        return new HashMap<>();
    }
}
