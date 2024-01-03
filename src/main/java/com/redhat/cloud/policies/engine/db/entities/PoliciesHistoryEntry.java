package com.redhat.cloud.policies.engine.db.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Objects;
import java.util.UUID;
import io.vertx.core.json.JsonArray;

import org.hibernate.annotations.Type;
import io.quarkiverse.hibernate.types.json.JsonBinaryType;
import io.quarkiverse.hibernate.types.json.JsonTypes;

@Entity
@Table(name = "policies_history")
public class PoliciesHistoryEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String tenantId;

    private String orgId;

    private String policyId;

    private long ctime;

    private String hostId;

    private String hostName;

    @Type(JsonBinaryType.class)
    @Column(name = "host_groups", nullable = false, columnDefinition = JsonTypes.JSON_BIN)
    private JsonArray hostGroups = new JsonArray();

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    public String getPolicyId() {
        return policyId;
    }

    public void setPolicyId(String policyId) {
        this.policyId = policyId;
    }

    public long getCtime() {
        return ctime;
    }

    public void setCtime(long ctime) {
        this.ctime = ctime;
    }

    public String getHostId() {
        return hostId;
    }

    public void setHostId(String hostId) {
        this.hostId = hostId;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public JsonArray getHostGroups() {
        return hostGroups;
    }

    public void setHostGroups(JsonArray hostGroups) {
        this.hostGroups = hostGroups;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof PoliciesHistoryEntry) {
            PoliciesHistoryEntry other = (PoliciesHistoryEntry) o;
            return Objects.equals(id, other.id);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "PoliciesHistoryEntry [id=" + id + ", tenantId=" + tenantId + ", orgId=" + orgId + ", policyId=" + policyId + ", ctime=" + ctime + ", hostId=" + hostId + ", hostName=" + hostName + "]";
    }
}
