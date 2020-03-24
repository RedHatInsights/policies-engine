package com.redhat.cloud.policies.engine.actions.plugins;

import io.smallrye.reactive.messaging.annotations.Channel;
import io.smallrye.reactive.messaging.annotations.Emitter;
import io.vertx.axle.core.Vertx;
import io.vertx.axle.ext.web.client.WebClient;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClientOptions;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.metrics.Counter;
import org.eclipse.microprofile.metrics.annotation.Metric;
import org.hawkular.alerts.actions.api.ActionMessage;
import org.hawkular.alerts.actions.api.ActionPluginListener;
import org.hawkular.alerts.actions.api.Plugin;
import org.hawkular.commons.log.MsgLogger;
import org.hawkular.commons.log.MsgLogging;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

@Plugin(name = "hooks")
@Dependent
public class BetaHooksActionPluginRegister implements ActionPluginListener {
    private final MsgLogger log = MsgLogging.getMsgLogger(BetaHooksActionPluginRegister.class);

    private static String APPLICATION_NAME = "policies";
    private static String EVENT_TYPE = "any";
    private static String LEVEL = "all";

    @Inject
    Vertx vertx;

    @ConfigProperty(name="external.notifications-backend.register.url")
    String notificationsRegisterUrl;

    @Inject
    @Channel("hooks")
    Emitter<JsonObject> channel;

    @Inject
    @Metric(absolute = true, name = "engine.actions.hooks.processed")
    Counter messagesCount;

    private WebClient client;

    @PostConstruct
    public void initialize() {
        log.debugf("Trying to register to: %s", notificationsRegisterUrl);
        client = WebClient.create(vertx, new WebClientOptions());
        client.postAbs(notificationsRegisterUrl)
                .sendJsonObject(createApplicationRegistrationPayload())
                .thenApply(resp -> {
                    if (resp.statusCode() == 200) {
                        log.info("Application registered to beta hooks backend");
                    } else {
                        log.error("Failed to register application to beta hooks backend: " + resp.bodyAsString());
                    }
                    return null;
                })
                .handle((BiFunction<Object, Throwable, Void>) (aVoid, throwable) -> {
                    log.errorf("Failed to connect to beta hooks backend " + throwable.getMessage());
                    return null;
                });
    }

    private JsonObject createApplicationRegistrationPayload() {
        JsonObject registration = new JsonObject();
        JsonObject application = new JsonObject();
        application.put("name", APPLICATION_NAME);
        application.put("title", "Policies");
        registration.put("application", application);

        JsonObject level = new JsonObject();
        level.put("id", LEVEL);
        level.put("title", "All");

        JsonObject eventType = new JsonObject();
        eventType.put("id", EVENT_TYPE);
        eventType.put("title", "Triggered policy");
        eventType.put("levels", new JsonArray().add(level));

        registration.put("event_types", new JsonArray().add(eventType));
        return registration;
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

    @Override
    public void process(ActionMessage msg) throws Exception {
        // Fields and terminology straight from the target project
        JsonObject message = new JsonObject();
        message.put("application", APPLICATION_NAME);
        message.put("account_id", msg.getAction().getTenantId());
        message.put("event_type", EVENT_TYPE);
        message.put("level", LEVEL);
        message.put("timestamp", msg.getAction().getCtime());
        message.put("message", JsonObject.mapFrom(msg.getAction()));
        channel.send(message);
        messagesCount.inc();
    }

    @Override
    public void flush() {

    }
}
