package com.redhat.cloud.policies.engine.process;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

import java.util.HashMap;
import java.util.Map;

public class Event {

    private String accountId;
    private String orgId;
    private String id;
    private long ctime;
    private String category;
    private String text;
    private Map<String, String> context;
    private Multimap<String, String> tags;
    private Map<String, Object> facts;

    public Event() {}

    public Event(String accountId, String id, String category, String text) {
        this.accountId = accountId;
        this.id = id;
        setCtime(ctime);
        this.category = category;
        this.text = text;
    }

    public static Event createEventWithOrgId(String orgId, String id, String categoryName, String text) {
        Event event = new Event();
        event.setOrgId(orgId);
        event.setId(id);
        event.setCategory(categoryName);
        event.setText(text);

        return event;
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

    private Multimap<String, String> tagsBuilder() {
        return MultimapBuilder.hashKeys().hashSetValues().build();
    }

    public Multimap<String, String> getTags() {
        if (null == tags) {
            tags = tagsBuilder();
        }
        return tags;
    }

    public Map<String, Object> getFacts() {
        return facts;
    }

    public void setFacts(Map<String, Object> facts) {
        this.facts = facts;
    }

    public void setTags(Multimap<String, String> tags) {
        this.tags = tags;
    }

    public void addTag(String name, String value) {
        if (null == name || null == value) {
            throw new IllegalArgumentException("Tag must have non-null name and value");
        }
        getTags().put(name, value);
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
        return "Event [accountId=" + accountId + ", id=" + id + ", ctime=" + ctime + ", category=" + category
                + ", text=" + text + ", context=" + context + ", " + "tags=" + tags + "]";
    }
}
