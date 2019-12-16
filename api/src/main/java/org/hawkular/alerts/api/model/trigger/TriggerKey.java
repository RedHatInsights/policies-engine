package org.hawkular.alerts.api.model.trigger;

import java.io.Serializable;

/**
 * @author Jay Shaughnessy
 * @author Lucas Ponce
 */
public class TriggerKey implements Serializable {
    private String tenantId;
    private String triggerId;

    public TriggerKey(String tenantId, String triggerId) {
        this.tenantId = tenantId;
        this.triggerId = triggerId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getTriggerId() {
        return triggerId;
    }

    public void setTriggerId(String triggerId) {
        this.triggerId = triggerId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TriggerKey that = (TriggerKey) o;

        if (tenantId != null ? !tenantId.equals(that.tenantId) : that.tenantId != null) return false;
        return triggerId != null ? triggerId.equals(that.triggerId) : that.triggerId == null;

    }

    @Override
    public int hashCode() {
        int result = tenantId != null ? tenantId.hashCode() : 0;
        result = 31 * result + (triggerId != null ? triggerId.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "TriggerKey{" +
                "tenantId='" + tenantId + '\'' +
                ", triggerId='" + triggerId + '\'' +
                '}';
    }
}
