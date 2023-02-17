package com.redhat.cloud.policies.engine.process;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.redhat.cloud.event.apps.policies.v1.Policy;
import com.redhat.cloud.event.apps.policies.v1.PolicyTriggered;
import com.redhat.cloud.event.apps.policies.v1.RHELSystemTag;
import com.redhat.cloud.event.apps.policies.v1.SystemClass;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PoliciesTriggeredCloudEvent {

    public static Builder builder() {
        return new Builder();
    }

    static class Builder {
        private UUID id;
        private String subjectPrefix = "urn:redhat:subject:rhel_system:";
        private LocalDateTime time;
        private String account;
        private String orgId;

        final private SystemClass system;
        final private List<Policy> policies;
        final private List<RHELSystemTag> systemTags;

        public Builder() {
            this.system = new SystemClass();
            this.policies = new ArrayList<>();
            this.systemTags = new ArrayList<>();
        }

        public Builder setId(UUID id) {
            this.id = id;
            return this;
        }

        public Builder setTime(LocalDateTime time) {
            this.time = time;
            return this;
        }

        public Builder setAccount(String account) {
            this.account = account;
            return this;
        }

        public Builder setOrgId(String orgId) {
            this.orgId = orgId;
            return this;
        }

        public Builder setSystemInventoryId(String inventoryId) {
            this.system.setInventoryID(inventoryId);
            return this;
        }

        public Builder setSystemDisplayName(String displayName) {
            this.system.setDisplayName(displayName);
            return this;
        }

        public Builder setSystemCheckinTime(LocalDateTime checkinTime) {
            this.system.setCheckIn(checkinTime.atOffset(ZoneOffset.UTC));
            return this;
        }

        public Builder addSystemTag(String namespace, String key, String value) {
            this.systemTags.add(
                    createSystemTag(namespace, key, value)
            );
            return this;
        }

        public Builder addPolicy(String id, String name, String description, String condition, String url) {
            this.policies.add(
                    createPolicy(id, name, description, condition, url)
            );
            return this;
        }

        public PoliciesTriggeredCloudEvent build() {
            SystemClass system = new SystemClass();
            system.setDisplayName(this.system.getDisplayName());
            system.setInventoryID(this.system.getInventoryID());
            system.setCheckIn(this.system.getCheckIn());
            system.setTags(
                    this.systemTags
                            .stream()
                            .map(t -> createSystemTag(t.getNamespace(), t.getKey(), t.getValue()))
                            .toArray(RHELSystemTag[]::new)
            );

            PoliciesTriggeredCloudEvent event = new PoliciesTriggeredCloudEvent();
            event.id = id;
            event.subject = subjectPrefix + this.system.getInventoryID();
            event.data = new PolicyTriggered();
            event.getData().setSystem(system);
            event.getData().setPolicies(policies
                    .stream()
                    .map(p -> createPolicy(p.getID(), p.getName(), p.getDescription(), p.getCondition(), p.getURL()))
                    .toArray(Policy[]::new)
            );
            event.time = time;
            event.redhatAccount = account;
            event.redhatOrgId = orgId;

            return event;
        }

        private RHELSystemTag createSystemTag(String namespace, String key, String value) {
            RHELSystemTag tag = new RHELSystemTag();
            tag.setNamespace(namespace);
            tag.setKey(key);
            tag.setValue(value);
            return tag;
        }

        private Policy createPolicy(String id, String name, String description, String condition, String url) {
            Policy policy = new Policy();
            policy.setID(id);
            policy.setName(name);
            policy.setDescription(description);
            policy.setCondition(condition);
            policy.setURL(url);
            return policy;
        }
    }

    private UUID id;
    private final String source = "urn:redhat:source:policies:insights:policies";
    private String subject;

    @JsonProperty(value = "specversion")
    private final String specVersion = "1.0";

    private final String type = "com.redhat.console.insights.policies.policy-triggered";
    private final String dataschema = "https://console.redhat.com/api/schemas/apps/policies/v1/policy-triggered.json";
    private PolicyTriggered data;
    private LocalDateTime time;

    @JsonProperty(value = "redhataccount")
    private String redhatAccount;

    @JsonProperty(value = "redhatorgid")
    private String redhatOrgId;

    public UUID getId() {
        return id;
    }

    public String getSource() {
        return source;
    }

    public String getSubject() {
        return subject;
    }

    public String getSpecVersion() {
        return specVersion;
    }

    public String getType() {
        return type;
    }

    public String getDataschema() {
        return dataschema;
    }

    public PolicyTriggered getData() {
        return data;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public String getRedhatAccount() {
        return redhatAccount;
    }

    public String getRedhatOrgId() {
        return redhatOrgId;
    }
}
