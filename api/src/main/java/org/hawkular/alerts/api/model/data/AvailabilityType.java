package org.hawkular.alerts.api.model.data;

/**
 * Severity set for a {@link org.hawkular.alerts.api.model.trigger.Trigger} and assigned to an
 * {@link Alert} it generates.
 *
 * @author jay shaughnessy
 * @author lucas ponce
 */
public enum AvailabilityType {
    UP, DOWN, UNAVAILABLE
}
