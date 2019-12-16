package org.hawkular.alerts.api.model.trigger;

/**
 * The mode of the Trigger.  Triggers always start in <code>FIRING<code> mode.  If the auto-resolve feature is enabled
 * for the Trigger, then it will switch to <code>AUTORESOLVE<code> mode after firing.  When the auto-resolve condition
 * set is satisfied, or if the Trigger is reloaded (manually, via edit, or at startup), the trigger returns to
 * <code>FIRING<code> mode.  The mode is also needed when defining a trigger, to indicate the relevant mode for a
 * conditions or dampening definition.
 *
 * @author jay shaughnessy
 * @author lucas ponce
 */
public enum Mode {
    FIRING, AUTORESOLVE
};
