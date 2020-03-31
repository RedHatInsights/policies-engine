package com.redhat.cloud.policies.engine.process;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.metrics.Counter;
import org.eclipse.microprofile.metrics.annotation.Metric;
import org.eclipse.microprofile.reactive.messaging.Acknowledgment;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.hawkular.alerts.api.model.event.Event;
import org.hawkular.alerts.api.services.AlertsService;
import org.hawkular.commons.log.MsgLogger;
import org.hawkular.commons.log.MsgLogging;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static org.hawkular.alerts.api.util.Util.isEmpty;

/**
 * This is the main process for Policies. It ingests data from Kafka, enriches it with information from
 * insights-host-inventory and then sends it for event processing in the engine.
 */
@ApplicationScoped
public class Receiver {
    private final MsgLogger log = MsgLogging.getMsgLogger(Receiver.class);

    public static final String INSIGHTS_REPORT_DATA_ID = "platform.inventory.host-egress";

    public static final String CATEGORY_NAME = "insight_report";
    public static final String INSIGHT_ID_FIELD = "insights_id";
    public static final String DISPLAY_NAME_FIELD = "display_name";

    private static final String HOST_FIELD = "host";
    private static final String TENANT_ID_FIELD = "account";
    private static final String SYSTEM_PROFILE_FIELD = "system_profile";
    private static final String NETWORK_INTERFACES_FIELD = "network_interfaces";
    private static final String YUM_REPOS_FIELD = "yum_repos";
    private static final String NAME_FIELD = "name";

    @ConfigProperty(name = "engine.receiver.store-events")
    boolean storeEvents;

    @Inject
    AlertsService alertsService;

    @Inject
    @Metric(absolute = true, name = "engine.input.processed", tags = {"queue=host-egress"})
    Counter incomingMessagesCount;

    @Inject
    @Metric(absolute = true, name = "engine.input.processed.errors", tags = {"queue=host-egress"})
    Counter processingErrors;

    @Incoming("host-egress")
    @Acknowledgment(Acknowledgment.Strategy.MANUAL)
    public CompletionStage<Void> processAsync(Message<String> input) {
        return
                CompletableFuture.supplyAsync(() -> {
                    // smallrye-messaging 1.1.0 and up has its own metric for received messages
                    incomingMessagesCount.inc();
                    if(log.isTraceEnabled()) {
                        log.tracef("Received message, input payload: %s", input.getPayload());
                    }
                    JsonObject json = new JsonObject(input.getPayload());
                    return json;
                }).thenApplyAsync(json -> {
                    if(json.containsKey(HOST_FIELD)) {
                        json = json.getJsonObject(HOST_FIELD);
                    } else {
                        return null;
                    }

                    String insightsId = json.getString(INSIGHT_ID_FIELD);

                    if (isEmpty(insightsId)) {
                        return null;
                    }

                    String tenantId = json.getString(TENANT_ID_FIELD);
                    String displayName = json.getString(DISPLAY_NAME_FIELD);
                    String text = String.format("host-egress report %s for %s", insightsId, displayName);

                    Event event = new Event(tenantId, UUID.randomUUID().toString(), INSIGHTS_REPORT_DATA_ID, CATEGORY_NAME, text);
                    // Indexed searchable events
                    Map<String, String> tagsMap = new HashMap<>();
                    tagsMap.put(DISPLAY_NAME_FIELD, displayName);
                    event.setTags(tagsMap);

                    // Additional context for processing
                    Map<String, String> contextMap = new HashMap<>();
                    contextMap.put(INSIGHT_ID_FIELD, insightsId);
                    event.setContext(contextMap);

                    JsonObject sp = json.getJsonObject(SYSTEM_PROFILE_FIELD);
                    event.setFacts(parseSystemProfile(sp));
                    return event;
                }).thenAcceptAsync(event -> {
                    if(event == null) {
                        return;
                    }
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
                }).handle((aVoid, throwable) -> {
                    if (throwable != null) {
                        processingErrors.inc();
                        log.errorf("Failed to process input message: %s", throwable.getMessage());
                    }
                    input.ack();
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

        JsonArray networkInterfaces = json.getJsonArray(NETWORK_INTERFACES_FIELD);
        if(networkInterfaces != null) {
            facts.put(NETWORK_INTERFACES_FIELD, namedObjectsToMap(networkInterfaces));
        }

        JsonArray yumRepos = json.getJsonArray(YUM_REPOS_FIELD);
        if(yumRepos != null) {
            facts.put(YUM_REPOS_FIELD, namedObjectsToMap(yumRepos));
        }

        return facts;
    }

    static Map<String, Object> namedObjectsToMap(JsonArray objectArray) {
        Map<String, Object> arrayObjectKey = new HashMap<>();
        for (Object o : objectArray) {
            JsonObject json = (JsonObject) o;
            String name = json.getString(NAME_FIELD);
            if (name == null || name.isEmpty()) {
                continue;
            }
            arrayObjectKey.put(name, json.getMap());
        }
        return arrayObjectKey;
    }
}
