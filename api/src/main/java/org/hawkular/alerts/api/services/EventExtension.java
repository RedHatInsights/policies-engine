package org.hawkular.alerts.api.services;

import java.util.TreeSet;

import org.hawkular.alerts.api.model.event.Event;


/**
 * An extension that will process received Events.
 *
 * @author Jay Shaughnessy
 * @author Lucas Ponce
 */
public interface EventExtension {

    /**
     * The extension processes the supplied Events and returns Events to be forwarded, if any.
     *
     * @param events The Events to be processed by the extension.
     * @return The set of Events to be forwarded to the next extension, or core engine if this is the final extension.
     */
    TreeSet<Event> processEvents(TreeSet<Event> events);

}
