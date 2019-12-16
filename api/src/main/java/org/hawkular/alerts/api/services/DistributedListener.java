package org.hawkular.alerts.api.services;

import java.util.Set;

/**
 * A listener for reacting to distribution triggers changes.
 *
 * {@code DistributedListener} are registered via {@code DefinitionsService}.
 *
 * On each node of the cluster, a {@code DistributedListener} is invoked with the {@code DistributedEvent}
 * represeting the changes on the triggers the node should load/unload.
 *
 * @author Jay Shaughnessy
 * @author Lucas Ponce
 */
public interface DistributedListener {

    /**
     * React to one or more distribution trigger changes sent to {@link DistributedListener}.
     * Multiple events may be received in one notification as result of a topology change.
     *
     * @param events distributed events triggering the notification.
     */
    void onChange(Set<DistributedEvent> events);
}
