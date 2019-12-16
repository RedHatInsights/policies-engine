package org.hawkular.alerts.api.services;

import java.util.List;

/**
 * A listener for reacting to definitions changes.
 *
 * {@code DefinitionsListener} are registered via {@code DefinitionsService}.
 *
 * {@code DefinitionsListener} are invoked locally on the node which performs the definitions operation,
 * in distributed scenarios these events are not propagated and others nodes are not aware of the changes.
 *
 * @author Jay Shaughnessy
 * @author Lucas Ponce
 */
public interface DefinitionsListener {

    /**
     * React to one or more definitions change events sent to {@link DefinitionsListener}.  Multiple events may be
     * received in one notification due to several updates being imported in a batch.
     *
     * @param events change events triggering the notification.
     */
    void onChange(List<DefinitionsEvent> events);
}
