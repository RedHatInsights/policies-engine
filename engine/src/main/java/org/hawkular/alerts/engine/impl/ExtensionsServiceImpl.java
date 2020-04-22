package org.hawkular.alerts.engine.impl;

import java.util.HashSet;
import java.util.Set;

import org.hawkular.alerts.api.services.DataExtension;
import org.hawkular.alerts.api.services.EventExtension;
import org.hawkular.alerts.api.services.ExtensionsService;
import org.hawkular.alerts.log.MsgLogger;
import org.hawkular.alerts.log.MsgLogging;

/**
 * @author Jay Shaughnessy
 * @author Lucas Ponce
 */
public class ExtensionsServiceImpl implements ExtensionsService {
    private final MsgLogger log = MsgLogging.getMsgLogger(ExtensionsServiceImpl.class);

    Set<DataExtension> dataExtensions;
    Set<EventExtension> eventsExtensions;

    public void init() {
        dataExtensions = new HashSet<>();
        eventsExtensions = new HashSet<>();
    }

    @Override
    public void addExtension(DataExtension extension) {
        log.infof("Adding DataExtension %s", extension);
        dataExtensions.add(extension);
    }

    @Override
    public void addExtension(EventExtension extension) {
        log.infof("Adding EventExtension %s", extension);
        eventsExtensions.add(extension);
    }

    @Override
    public Set<DataExtension> getDataExtensions() {
        return dataExtensions;
    }

    @Override
    public Set<EventExtension> getEventExtensions() {
        return eventsExtensions;
    }
}
