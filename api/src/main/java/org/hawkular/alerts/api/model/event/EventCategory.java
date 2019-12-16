package org.hawkular.alerts.api.model.event;

/**
 * Every Event has a category to help identify what it represents.  It can be one of these, or it can be something
 * specified by the Event creator.  This just tries to predefine common events.
 *
 * @author jay shaughnessy
 * @author lucas ponce
 */
public enum EventCategory {
    ALERT, DEPLOYMENT, LOG, TRIGGER
}
