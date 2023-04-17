package com.redhat.cloud.policies.engine.process;

import com.redhat.cloud.event.apps.policies.v1.Policy;
import com.redhat.cloud.event.apps.policies.v1.PolicyTriggered;
import com.redhat.cloud.event.apps.policies.v1.RHELSystemTag;
import com.redhat.cloud.event.apps.policies.v1.SystemClass;
import com.redhat.cloud.event.parser.GenericConsoleCloudEvent;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PoliciesTriggeredCloudEvent extends GenericConsoleCloudEvent<PolicyTriggered> {

    public static Builder builder() {
        return new Builder();
    }

    static class Builder {

        private static final String source = "urn:redhat:source:policies:insights:policies";
        private static final String specVersion = "1.0";
        private static final String type = "com.redhat.console.insights.policies.policy-triggered";
        private static final String dataschema = "https://console.redhat.com/api/schemas/apps/policies/v1/policy-triggered.json";
        private static final String subjectPrefix = "urn:redhat:subject:rhel_system:";
        private UUID id;
        private LocalDateTime time;
        private String account;
        private String orgId;

        private final SystemClass system = new SystemClass();
        private final List<Policy> policies = new ArrayList<>();
        private final List<RHELSystemTag> systemTags = new ArrayList<>();

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
            event.setId(id);
            event.setSource(source);
            event.setSpecVersion(specVersion);
            event.setType(type);
            event.setDataSchema(dataschema);

            event.setSubject(subjectPrefix + this.system.getInventoryID());
            event.setData(new PolicyTriggered());
            event.getData().setSystem(system);
            event.getData().setPolicies(policies
                    .stream()
                    .map(p -> createPolicy(p.getID(), p.getName(), p.getDescription(), p.getCondition(), p.getURL()))
                    .toArray(Policy[]::new)
            );
            event.setTime(time);
            event.setAccountId(account);
            event.setOrgId(orgId);

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
}
