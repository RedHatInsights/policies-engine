package org.hawkular.alerts.api.services;

import org.hawkular.alerts.api.model.condition.Condition;
import org.hawkular.alerts.api.model.event.Event;
import org.hawkular.alerts.api.model.trigger.FullTrigger;
import org.hawkular.alerts.api.model.trigger.Trigger;

import java.util.Collection;

/**
 * Simple engine implementation that does not rely on Infinispan or Drools for the Kafka messages processing.
 */
public interface LightweightEngine {

    /**
     * Validates a policy condition.
     * @param condition the condition to validate
     * @throws IllegalArgumentException if the condition is not valid
     */
    void validateCondition(String condition);

    /**
     * Adds a trigger to the in-memory collection of known triggers.
     * @param fullTrigger the trigger to load
     * @deprecated Only used when step 2 of the policies refactoring is not enabled
     */
    @Deprecated(forRemoval = true)
    void loadTrigger(FullTrigger fullTrigger);

    /**
     * Updates a trigger in the in-memory collection of known triggers.
     * @param trigger the trigger to update
     * @param conditions the trigger conditions
     * @deprecated Only used when step 2 of the policies refactoring is not enabled
     */
    @Deprecated(forRemoval = true)
    void reloadTrigger(Trigger trigger, Collection<Condition> conditions);

    /**
     * Removes a trigger from the in-memory collection of known triggers.
     * @param tenantId the tenant id of the trigger owner
     * @param triggerId the trigger id
     * @deprecated Only used when step 2 of the policies refactoring is not enabled
     */
    @Deprecated(forRemoval = true)
    void removeTrigger(String tenantId, String triggerId);

    /**
     * Processes an {@link Event}, evaluating conditions of all triggers owned by the tenant of the event. If the
     * conditions of at least one trigger are satisfied, then a Kafka message will be sent to the notifications topic.
     * If the conditions of multiple triggers are satisfied, one Kafka message is sent and it contains one event for
     * each trigger that was fired. In that case, tags from each fired trigger are also merged into a single collection
     * and added to the Kafka payload. Each fired trigger also results in a new record in the policies history DB table.
     * @param event the event
     */
    void process(Event event);
}
