package com.redhat.cloud.custompolicies.engine.process;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.metrics.Counter;
import org.eclipse.microprofile.metrics.annotation.Metric;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.hawkular.alerts.api.model.event.Event;
import org.hawkular.alerts.api.services.AlertsService;
import org.hawkular.commons.log.MsgLogger;
import org.hawkular.commons.log.MsgLogging;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;

/**
 * This is the main process for Custom Policies. It ingests data from Kafka, enriches it with information from
 * insights-host-inventory and then sends it for event processing in the engine.
 */
@ApplicationScoped
public class Receiver {
    private final MsgLogger log = MsgLogging.getMsgLogger(Receiver.class);

    private static String TENANT_ID = "account";
    public static String INSIGHT_ID_FIELD = "insights_id";
    private static String EVENT_TYPE = "type";
    private static String SYSTEM_PROFILE = "system_profile";

    @ConfigProperty(name = "engine.receiver.store-events")
    boolean storeEvents;

    @ConfigProperty(name = "engine.receiver.log-events", defaultValue = "false")
    boolean logEvents;

    @Inject
    SystemProfileClient client;

    @Inject
    AlertsService alertsService;

    @Inject
    @Metric(name="engine-incoming",absolute = true)
    Counter counter ;

    @Incoming("kafka-hosts")
    public CompletionStage<Void> processAsync(JsonObject json) {
        counter.inc();
        String tenantId = json.getString(TENANT_ID);
        String insightsId = json.getString(INSIGHT_ID_FIELD);
        if (logEvents) {
            log.infof("Got msg for tenant [%s] and insightsId [%s] \n ",  tenantId , insightsId);
        }

        JsonObject systemProfile = json.getJsonObject("system_profile");
        CompletionStage<JsonObject> systemProfileStage;

        if(systemProfile != null) {
            systemProfileStage = CompletableFuture.supplyAsync(() -> systemProfile);
        } else {
            systemProfileStage = client.getSystemProfile(tenantId, insightsId);
        }

        return CompletableFuture.supplyAsync(() -> {
            Event event = new Event(tenantId, UUID.randomUUID().toString(),"insight_report", "just another report which needs a name");
            // Indexed searchable events
            Map<String, String> tagsMap = new HashMap<>();
            tagsMap.put("display_name", json.getString("display_name"));
            tagsMap.put(INSIGHT_ID_FIELD, insightsId);
            event.setTags(tagsMap);

            // Additional context for processing
            Map<String, String> contextMap = new HashMap<>();
            event.setContext(contextMap);
            return event;
        }).thenCombine(systemProfileStage, (event, sp) -> {
            event.setFacts(parseSystemProfile(sp));
            return event;
        }).thenAcceptAsync(event -> {
            try {
                List<Event> eventList = new ArrayList<>(1);
                eventList.add(event);
                if (storeEvents) {
                    alertsService.addEvents(eventList);
                } else {
                    alertsService.sendEvents(eventList);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).handleAsync((BiFunction<Void, Throwable, Void>) (event, throwable) -> {
            log.errorf("Failed to enrich the data: " + throwable.getMessage());
            return null;
        });
    }

    /**
     * parseSystemProfile extracts certain parts of the input JSON and modifies them for easier use
     */
    static Map<String, Object> parseSystemProfile(JsonObject json) {
        if(json == null) {
            return new HashMap<>();
        }
        Map<String, Object> facts = json.getMap();

        JsonArray networkInterfaces = json.getJsonArray("network_interfaces");
        if(networkInterfaces != null) {
            facts.put("network_interfaces", namedObjectsToMap(networkInterfaces));
        }

        JsonArray yumRepos = json.getJsonArray("yum_repos");
        if(yumRepos != null) {
            facts.put("yum_repos", namedObjectsToMap(yumRepos));
        }

        return facts;
    }

    static Map<String, Object> namedObjectsToMap(JsonArray objectArray) {
        Map<String, Object> arrayObjectKey = new HashMap<>();
        for (Object o : objectArray) {
            JsonObject json = (JsonObject) o;
            String name = json.getString("name");
            if (name == null || name.isEmpty()) {
                continue;
            }
            arrayObjectKey.put(name, json.getMap());
        }
        return arrayObjectKey;
    }
}
