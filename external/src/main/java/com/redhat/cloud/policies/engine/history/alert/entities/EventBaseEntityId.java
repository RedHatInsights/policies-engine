package com.redhat.cloud.policies.engine.history.alert.entities;

import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class EventBaseEntityId implements Serializable {

    @NotNull
    private String tenantId;

    @NotNull
    private String eventId;

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof EventBaseEntityId) {
            EventBaseEntityId other = (EventBaseEntityId) o;
            return Objects.equals(tenantId, other.tenantId) &&
                    Objects.equals(eventId, other.eventId);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(tenantId, eventId);
    }
}
