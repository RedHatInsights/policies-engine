package org.hawkular.alerts.api.model;

/**
 * Severity set for a {@link org.hawkular.alerts.api.model.trigger.Trigger} and assigned to an
 * {@link Alert} it generates.
 *
 * @author jay shaughnessy
 * @author lucas ponce
 */
public enum Severity {
    LOW, MEDIUM, HIGH, CRITICAL
}
