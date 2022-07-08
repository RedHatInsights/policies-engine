package com.redhat.cloud.policies.engine.process;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Event {

    private String accountId;
    private String orgId;
    private String id;
    private long ctime;
    private String category;
    private String text;
    private Map<String, String> context;
    private Map<String, Set<String>> tags = new HashMap<>();
    private Map<String, Object> facts;

    public Event() {}

    public Event(String accountId, String orgId, String id, String category, String text) {
        this.accountId = accountId;
        this.orgId = orgId;
        this.id = id;
        setCtime(ctime);
        this.category = category;
        this.text = text;
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

    public Map<String, Set<String>> getTags() {
        return tags;
    }

    /**
     * Returns the tags associated with the given {@code key}. Never returns {@code null}.
     * @param key the key
     * @return the tags associated with the key or an empty collection
     */
    public Set<String> getTags(String key) {
        return tags.computeIfAbsent(key, unused -> new HashSet<>());
    }

    public void setTags(Map<String, Set<String>> tags) {
        this.tags = tags;
    }

    public void addTag(String name, String value) {
        if (null == name || null == value) {
            throw new IllegalArgumentException("Tag must have non-null name and value");
        }
        getTags(name).add(value);
    }

    public int getTotalTagsSize() {
        int size = 0;
        for (Set<String> tagValues : tags.values()) {
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
        return "Event [accountId=" + accountId + ", orgId=" + orgId + ", id=" + id + ", ctime=" + ctime + ", category=" + category
                + ", text=" + text + ", context=" + context + ", " + "tags=" + tags + "]";
    }
}
