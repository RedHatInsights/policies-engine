package org.hawkular.alerts.filter;

import java.io.Serializable;

public class CacheKey implements Serializable {
    private static final long serialVersionUID = 1L;

    private String tenantId;
    private String dataId;

    public CacheKey(String tenantId, String dataId) {
        this.tenantId = tenantId;
        this.dataId = dataId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getDataId() {
        return dataId;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((dataId == null) ? 0 : dataId.hashCode());
        result = prime * result + ((tenantId == null) ? 0 : tenantId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CacheKey other = (CacheKey) obj;
        if (dataId == null) {
            if (other.dataId != null)
                return false;
        } else if (!dataId.equals(other.dataId))
            return false;
        if (tenantId == null) {
            if (other.tenantId != null)
                return false;
        } else if (!tenantId.equals(other.tenantId))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "CacheKey [tenantId=" + tenantId + ", dataId=" + dataId + "]";
    }

}
