package org.hawkular.alerts.api.services;

import java.util.Set;

/**
 * An interface used to register engine extensions into the system.
 *
 * Engine extensions are listeners that can operate on Data or Events received before the engine process them.
 *
 * Extensions can implement use cases where transformation or filtering of incoming Data or Events might be necessary.
 *
 * Engine extensions are executed in a pipeline ordered by registration time.
 *
 * @author Jay Shaughnessy
 * @author Lucas Ponce
 */
public interface ExtensionsService {

    /**
     * Register an extension that will process Data.
     *
     * @param listener the data listener
     */
    void addExtension(DataExtension listener);

    /**
     * Register an extension that will process Events.
     *
     * @param extension the events extension
     */
    void addExtension(EventExtension extension);

    /*
        TODO    In the future we might want to add an addExtension(priority, extension).
        TODO    But for now, having more than an extension could be a cornercase.
     */

    /**
     * @return the set of data listeners registered
     */
    Set<DataExtension> getDataExtensions();

    /**
     * @return the set of events listeners registered
     */
    Set<EventExtension> getEventExtensions();
}
