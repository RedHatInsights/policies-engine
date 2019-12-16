package org.hawkular.alerts.api.services;

/**
 * An event that represents a change of a trigger distribution in the current node.
 *
 * In cluster topologies triggers are distributed across all available nodes.
 * In this way, a trigger is only executed by one node of the cluster, allowing to scale in horizontal way.
 *
 * When a trigger is added, modified or removed, the core engine or third party alerters should be notified to perform
 * load/unload operations of the trigger in their own logic.
 *
 * Each node will received the events related which triggers should be loaded/unloaded.
 *
 * {@code DistributedEvent} targets only triggers and the granularity is the whole trigger, meaning that a change on
 * a condition or a dampening is considered an update of an existing trigger.
 *
 * {@code DistributedEvent} are invoked via {@code DistributedListener} which are registered
 * via {@code DefinitionsService}.
 *
 * @author jay shaughnessy
 * @author lucas ponce
 */
public class DistributedEvent {

    public enum Operation {
        /**
         * A new trigger has been added
         */
        ADD,

        /**
         * An existing trigger or any of its related conditions or dampenings have been modified.
         */
        UPDATE,

        /**
         * An existing trigger has been removed
         */
        REMOVE
    }

    private Operation operation;
    private String tenantId;
    private String triggerId;

    public DistributedEvent(Operation operation, String tenantId, String triggerId) {
        this.operation = operation;
        this.tenantId = tenantId;
        this.triggerId = triggerId;
    }

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
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

        DistributedEvent that = (DistributedEvent) o;

        if (operation != that.operation) return false;
        if (tenantId != null ? !tenantId.equals(that.tenantId) : that.tenantId != null) return false;
        return triggerId != null ? triggerId.equals(that.triggerId) : that.triggerId == null;
    }

    @Override
    public int hashCode() {
        int result = operation != null ? operation.hashCode() : 0;
        result = 31 * result + (tenantId != null ? tenantId.hashCode() : 0);
        result = 31 * result + (triggerId != null ? triggerId.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "DistributedEvent{" +
                "operation=" + operation +
                ", tenantId='" + tenantId + '\'' +
                ", triggerId='" + triggerId + '\'' +
                '}';
    }
}
