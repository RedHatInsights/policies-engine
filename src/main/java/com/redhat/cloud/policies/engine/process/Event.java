package com.redhat.cloud.policies.engine.process;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class Event {

    /*
    Policies supports using tags in the conditions - The condition does not take into account the namespace of the tag.
    As we need to include the namespace as part of the cloud event - we are adding this class to hold that value.
    Policies adds the display-name and inventory-id as part of the tags - so we keep track of these synthetic
    tags to avoid sending them as "real" tags.
     */
    public static class TagContent {
        public final String value;
        public final String namespace;

        public final boolean isSynthetic;

        public TagContent(String namespace, String value) {
            this(namespace, value, false);
        }

        public TagContent(String namespace, String value, boolean isSynthetic) {
            this.namespace = namespace;
            this.value = value;
            this.isSynthetic = isSynthetic;
        }
    }

    public static class HostGroup {
        public final String id;
        public final String name;

        public HostGroup(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public HostGroup(JsonObject obj) {
            this.id = obj.getString("id", "");
            this.name = obj.getString("name", "");
        }

        public JsonObject toJsonObject() {
            return new JsonObject(this.toMap());
        }

        public Map<String, Object> toMap() {
            return Map.of("id", this.id, "name", this.name);
        }

        @Override
        public String toString() {
            return "HostGroup["+ name +";"+ id +"]";
        }
    }

    private String recordKey;
    private String accountId;
    private String orgId;
    private String id;
    private List<HostGroup> hostGroups = new LinkedList<HostGroup>();
    private long ctime;
    private String category;
    private String text;
    private Map<String, String> context;
    private Map<String, Set<TagContent>> tags = new HashMap<>();
    private Map<String, Object> facts;

    public Event() {}

    public Event(String recordKey, String accountId, String orgId, String id, String category, String text) {
        this.recordKey = recordKey;
        this.accountId = accountId;
        this.orgId = orgId;
        this.id = id;
        setCtime(ctime);
        this.category = category;
        this.text = text;
    }

    public String getRecordKey() {
        return recordKey;
    }

    public void setRecordKey(String recordKey) {
        this.recordKey = recordKey;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<HostGroup> getHostGroups() {
        return this.hostGroups;
    }

    public void setHostGroups(List<HostGroup> hostGroups) {
        this.hostGroups = hostGroups;
    }

    public void addHostGroups(Iterable<Object> hostGroups) throws RuntimeException {
        hostGroups.forEach(obj -> {
            if (obj instanceof JsonObject) {
                this.hostGroups.add(new HostGroup((JsonObject) obj));
            } else {
                throw new RuntimeException("Cannot handle host group of type "+ obj.getClass().toString()
                                           + " to be addded to the collection");
            }
        });
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public long getCtime() {
        return ctime;
    }

    public void setCtime(long ctime) {
        this.ctime = (ctime <= 0) ? System.currentTimeMillis() : ctime;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Map<String, Set<TagContent>> getTags() {
        return tags;
    }

    /**
     * Returns the tags associated with the given {@code key}. Never returns {@code null}.
     * @param key the key
     * @return the tags associated with the key or an empty collection
     */
    public Set<TagContent> getTags(String key) {
        return tags.computeIfAbsent(key, unused -> new HashSet<>());
    }

    public void setTags(Map<String, Set<TagContent>> tags) {
        this.tags = tags;
    }

    public void addTag(String namespace, String key, String value) {
        addTag(namespace, key, value, false);
    }

    public void addTag(String namespace, String key, String value, boolean isSynthetic) {
        if (null == namespace || null == key || null == value) {
            throw new IllegalArgumentException("Tag must have non-null namespace, key and value");
        }
        getTags(key).add(new TagContent(namespace, value, isSynthetic));
    }

    public int getTotalTagsSize() {
        int size = 0;
        for (Set<TagContent> tagValues : tags.values()) {
            size += tagValues.size();
        }
        return size;
    }

    public Map<String, Object> getFacts() {
        return facts;
    }

    public void setFacts(Map<String, Object> facts) {
        this.facts = facts;
    }

    public Map<String, String> getContext() {
        if (null == context) {
            context = new HashMap<>();
        }
        return context;
    }

    public void setContext(Map<String, String> context) {
        this.context = context;
    }

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    @Override
    public String toString() {
        return "Event [recordKey=" + recordKey + ", accountId=" + accountId + ", orgId=" + orgId + ", id=" + id
                + ", ctime=" + ctime + ", category=" + category + ", text=" + text + ", context=" + context
                + ", tags=" + tags + "]";
    }
}
