package com.redhat.cloud.custompolicies.engine.process;

import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.hawkular.alerts.api.model.event.Event;
import org.hawkular.alerts.api.services.AlertsService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.*;

@ApplicationScoped
public class Receiver {

    private static final Logger LOGGER = LoggerFactory.getLogger("Receiver");

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
        Event event = new Event(tenantId, UUID.randomUUID().toString(),"insight_report", "insight_report", "just another report which needs a name");
        event.setFacts(systemProfile.getMap());

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
}
