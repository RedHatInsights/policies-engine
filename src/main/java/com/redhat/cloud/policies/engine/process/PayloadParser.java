package com.redhat.cloud.policies.engine.process;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.quarkus.logging.Log;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import jakarta.annotation.PostConstruct;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@ApplicationScoped
public class PayloadParser {

    public static final String POLICIES_TAG_NAMESPACE = "policies";
    public static final String DISPLAY_NAME_FIELD = "display_name";
    public static final String INVENTORY_ID_FIELD = "inventory_id";
    public static final String CHECK_IN_FIELD = "check_in";
    public static final String HOST_FIELD = "host";
    public static final String HOST_ID = "id";
    public static final String HOST_GROUPS_FIELD = "groups";
    public static final String CATEGORY_NAME = "insight_report";
    public static final String FQDN_NAME_FIELD = "fqdn";
    public static final String NETWORK_INTERFACES_FIELD = "network_interfaces";
    public static final String YUM_REPOS_FIELD = "yum_repos";

    private static final Set<String> ACCEPTED_REPORTERS = Set.of("puptoo");
    private static final Set<String> ACCEPTED_TYPES = Set.of("created", "updated");
    private static final String TYPE_FIELD = "type";
    private static final String REPORTER_FIELD = "reporter";
    private static final String TENANT_ID_FIELD = "account";
    private static final String ORG_ID = "org_id";
    private static final String SYSTEM_PROFILE_FIELD = "system_profile";
    private static final String NAME_FIELD = "name";
    private static final String TAGS_FIELD = "tags";
    private static final String TAGS_KEY_FIELD = "key";
    private static final String TAGS_VALUE_FIELD = "value";
    private static final String TAGS_NAMESPACE_FIELD = "namespace";
    private static final String UPDATED = "updated";

    @Inject
    MeterRegistry meterRegistry;

    Counter incomingMessagesCount;
    Counter rejectedCount;
    Counter rejectedCountType;
    Counter rejectedCountHost;
    Counter rejectedCountReporter;
    Counter rejectedCountId;
    Counter processingErrors;

    @PostConstruct
    void postConstruct() {
        incomingMessagesCount = meterRegistry.counter("engine.input.processed", "queue", "host-egress");
        rejectedCount = meterRegistry.counter("engine.input.rejected", "queue", "host-egress");
        rejectedCountType = meterRegistry.counter("engine.input.rejected.detail", "queue", "host-egress", "reason", "type");
        rejectedCountHost = meterRegistry.counter("engine.input.rejected.detail", "queue", "host-egress", "reason", "noHost");
        rejectedCountReporter = meterRegistry.counter("engine.input.rejected.detail", "queue", "host-egress", "reason", "reporter");
        rejectedCountId = meterRegistry.counter("engine.input.rejected.detail", "queue", "host-egress", "reason", "insightsId");
        processingErrors = meterRegistry.counter("engine.input.processed.errors", "queue", "host-egress");
    }

    public Optional<Event> parse(String payload) {
        incomingMessagesCount.increment();

        JsonObject json;
        try {
            json = new JsonObject(payload);
        } catch(Exception e) {
            processingErrors.increment();
            throw e;
        }
        if (json.containsKey(TYPE_FIELD)) {
            String eventType = json.getString(TYPE_FIELD);
            if(!ACCEPTED_TYPES.contains(eventType)) {
                Log.debugf("Got a request with type='%s', ignoring ", eventType);
                rejectedCount.increment();
                rejectedCountType.increment();
                return Optional.empty();
            }
        }

        if (json.containsKey(HOST_FIELD)) {
            json = json.getJsonObject(HOST_FIELD);
        } else {
            rejectedCount.increment();
            rejectedCountHost.increment();
            return Optional.empty();
        }

        // Verify host.reporter (not platform_metadata.metadata.reporter!) is one of the accepted values
        String reporter = json.getString(REPORTER_FIELD);
        if(!ACCEPTED_REPORTERS.contains(reporter)) {
            rejectedCount.increment();
            rejectedCountReporter.increment();
            return Optional.empty();
        }

        String inventoryId = json.getString(HOST_ID);

        if (inventoryId == null || inventoryId.isBlank()) {
            rejectedCount.increment();
            rejectedCountId.increment();
            return Optional.empty();
        }

        String tenantId = json.getString(TENANT_ID_FIELD);
        String orgId = json.getString(ORG_ID);
        String displayName = json.getString(DISPLAY_NAME_FIELD);
        String text = String.format("host-egress report %s for %s", inventoryId, displayName);

        Event event = new Event(tenantId, orgId, UUID.randomUUID().toString(), CATEGORY_NAME, text);

        // Indexed searchable events
        Map<String, Set<Event.TagContent>> tagsMap = parseTags(json.getJsonArray(TAGS_FIELD));
        event.setTags(tagsMap);
        event.addTag(POLICIES_TAG_NAMESPACE, DISPLAY_NAME_FIELD, displayName, true);
        event.addTag(POLICIES_TAG_NAMESPACE, INVENTORY_ID_FIELD, json.getString(HOST_ID), true);

        // Add Host Groups
        JsonArray groups = json.getJsonArray(HOST_GROUPS_FIELD);
        if (groups != null) {
            event.addHostGroups(groups);
        }

        // Additional context for processing
        Map<String, String> contextMap = new HashMap<>();
        contextMap.put(INVENTORY_ID_FIELD, inventoryId);
        contextMap.put(CHECK_IN_FIELD, json.getString(UPDATED));
        event.setContext(contextMap);

        JsonObject sp = json.getJsonObject(SYSTEM_PROFILE_FIELD);
        Map<String, Object> systemProfile = parseSystemProfile(sp);

        systemProfile.put(FQDN_NAME_FIELD, json.getString(FQDN_NAME_FIELD));

        event.setFacts(systemProfile);

        return Optional.of(event);
    }

    /**
     * parseSystemProfile extracts certain parts of the input JSON and modifies them for easier use
     */
    static Map<String, Object> parseSystemProfile(JsonObject json) {
        if (json == null) {
            return new HashMap<>();
        }
        Map<String, Object> facts = json.getMap();

        JsonArray networkInterfaces = json.getJsonArray(NETWORK_INTERFACES_FIELD);
        if (networkInterfaces != null) {
            facts.put(NETWORK_INTERFACES_FIELD, namedObjectsToMap(networkInterfaces));
        }

        JsonArray yumRepos = json.getJsonArray(YUM_REPOS_FIELD);
        if (yumRepos != null) {
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

    static Map<String, Set<Event.TagContent>> parseTags(JsonArray tagsInput) {
        Map<String, Set<Event.TagContent>> tags = new HashMap<>();
        for (Object o : tagsInput) {
            JsonObject json = (JsonObject) o;
            String key = json.getString(TAGS_KEY_FIELD).toLowerCase();
            String value = json.getString(TAGS_VALUE_FIELD);
            String namespace = json.getString(TAGS_NAMESPACE_FIELD);
            tags.computeIfAbsent(key, unused -> new HashSet<>()).add(new Event.TagContent(namespace, value));
        }
        return tags;
    }
}
