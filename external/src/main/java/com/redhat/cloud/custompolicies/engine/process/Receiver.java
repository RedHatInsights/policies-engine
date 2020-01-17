package com.redhat.cloud.custompolicies.engine.process;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.hawkular.alerts.api.model.event.Event;
import org.hawkular.alerts.api.services.AlertsService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.*;

@ApplicationScoped
public class Receiver {

    private static String TENANT_ID = "account";
    private static String SYSTEM_PROFILE = "system_profile";
    private static String INSIGHT_ID_FIELD = "insight_id";
    private static String EVENT_TYPE = "type";

    @ConfigProperty(name = "engine.receiver.store-events")
    boolean storeEvents;

    @Inject
    AlertsService alertsService;

    @Incoming("kafka-hosts")
    public void process(JsonObject json) {
        // This process should potentially be in external system

        // TODO Remember to correctly map data_id, otherwise duplication removal will filter most of the data.

        String tenantId = json.getString(TENANT_ID);
        String insightsId = json.getString(INSIGHT_ID_FIELD);
        JsonObject systemProfile = json.getJsonObject(SYSTEM_PROFILE);
        // TODO These are hardcoded for demo purposes
        Event event = new Event(tenantId, UUID.randomUUID().toString(),"insight_report", "just another report which needs a name");
        event.setFacts(parseSystemProfile(systemProfile));

        // Indexed searchable events
        // TODO Examples for demo purposes
        Map<String, String> tagsMap = new HashMap<>();
        tagsMap.put("subscription_manager_id", json.getString("subscription_manager_id"));
        tagsMap.put("satellite_id", json.getString("satellite_id"));
        tagsMap.put("ansible_host", json.getString("ansible_host"));
        event.setTags(tagsMap);

        // Additional context for processing
        // TODO Examples for demo purposes
        Map<String, String> contextMap = new HashMap<>();
        contextMap.put("rhel_machine_id", json.getString("rhel_machine_id"));
//        contextMap.put(INSIGHT_ID_FIELD, insightsId);
        contextMap.put("infrastructure_vendor", (String) systemProfile.getMap().get("infrastructure_vendor"));
        event.setContext(contextMap);

        try {
            List<Event> eventList = new ArrayList<>(1);
            eventList.add(event);
            if(storeEvents) {
                alertsService.addEvents(eventList);
            } else {
                alertsService.sendEvents(eventList);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * parseSystemProfile extracts certain parts of the input JSON and modifies them for easier use
     */
    static Map<String, Object> parseSystemProfile(JsonObject json) {
        Map<String, Object> facts = json.getMap();

        JsonArray networkInterfaces = json.getJsonArray("network_interfaces");
        JsonArray yumRepos = json.getJsonArray("yum_repos");

        facts.put("network_interfaces", namedObjectsToMap(networkInterfaces));
        facts.put("yum_repos", namedObjectsToMap(yumRepos));

        return facts;
    }

    static Map<String, Object> namedObjectsToMap(JsonArray objectArray) {
        Map<String, Object> arrayObjectKey = new HashMap<>();
        for (Object o : objectArray) {
            JsonObject json = (JsonObject) o;
            String name = json.getString("name");
            if(name == null || name.isEmpty()) {
                continue;
            }
            arrayObjectKey.put(name, json.getMap());
        }
        return arrayObjectKey;
    }
}
