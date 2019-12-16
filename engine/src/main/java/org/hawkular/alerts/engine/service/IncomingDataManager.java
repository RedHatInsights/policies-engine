package org.hawkular.alerts.engine.service;

import org.hawkular.alerts.engine.impl.IncomingDataManagerImpl.IncomingData;
import org.hawkular.alerts.engine.impl.IncomingDataManagerImpl.IncomingEvents;

/**
 * Interface that allows handling of Incoming Data and Events.
 *
 * @author Jay Shaughnessy
 * @author Lucas Ponce
 */
public interface IncomingDataManager {

    void bufferData(IncomingData incomingData);

    void bufferEvents(IncomingEvents incomingEvents);

}
