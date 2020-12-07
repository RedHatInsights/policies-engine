package com.redhat.cloud.policies.engine.actions.plugins.notification;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PoliciesPayloadBuilder {
    private static class Tag {
        private String key;
        private String value;

        Tag(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }
    }

    private static DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm 'UTC'");
    private static ObjectMapper mapper = new ObjectMapper();

    private Map<String, String> triggers = new HashMap<>();
    private List<Tag> tags = new ArrayList<>();
    private String displayName;
    private String systemCheckIn;
    private String policyId;
    private String policyName;
    private String policyDescription;
    private String policyCondition;
    private String insightsId;

    public PoliciesPayloadBuilder addTrigger(String key, String value) {
        this.triggers.put(key, value);
        return this;
    }

    public PoliciesPayloadBuilder setDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    public PoliciesPayloadBuilder setSystemCheckIn(LocalDateTime datetime) {
        this.systemCheckIn = datetime.format(dateFormatter);
        return this;
    }

    public PoliciesPayloadBuilder addTag(String key, String value) {
        this.tags.add(new Tag(key, value));
        return this;
    }

    public PoliciesPayloadBuilder setPolicyId(String policyId) {
        this.policyId = policyId;
        return this;
    }

    public PoliciesPayloadBuilder setPolicyName(String policyName) {
        this.policyName = policyName;
        return this;
    }

    public PoliciesPayloadBuilder setPolicyDescription(String policyDescription) {
        this.policyDescription = policyDescription;
        return this;
    }

    public PoliciesPayloadBuilder setPolicyCondition(String policyCondition) {
        this.policyCondition = policyCondition;
        return this;
    }

    public PoliciesPayloadBuilder setInsightsId(String insightsId) {
        this.insightsId = insightsId;
        return this;
    }

    public String build() throws JsonProcessingException {
        Map<String, Object> params = new HashMap<>();
        params.put("display_name", this.displayName);
        params.put("system_check_in", this.systemCheckIn);
        params.put("policy_id", this.policyId);
        params.put("policy_name", this.policyName);
        params.put("policy_description", this.policyDescription);
        params.put("policy_condition", this.policyCondition);
        params.put("insights_id", this.insightsId);
        params.put("triggers", this.triggers);
        params.put("tags", this.tags);
        return mapper.writeValueAsString(params);
    }

}
