package org.hawkular.alerts.engine.service;

import java.util.Collection;

import org.hawkular.alerts.api.model.data.Data;
import org.hawkular.alerts.api.model.event.Event;

/**
 * A listener for reacting to partition events related to data and events.
 *
 * @author Jay Shaughnessy
 * @author Lucas Ponce
 */
public interface PartitionDataListener {

    /**
     * Invoked when a new collection of Data has been received into the partition.
     *
     * @param data the new data received
     */
    void onNewData(Collection<Data> data);

    /**
     * Invoked when a new collection of Events has been received into the partition.
     *
     * @param events the new events received
     */
    void onNewEvents(Collection<Event> events);
}
