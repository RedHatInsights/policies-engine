package org.hawkular.alerts.engine.impl;

import static org.hawkular.alerts.api.util.Util.isEmpty;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;

import org.eclipse.microprofile.config.ConfigProvider;
import org.hawkular.alerts.api.model.data.Data;
import org.hawkular.alerts.api.model.event.Event;
import org.hawkular.alerts.api.services.DefinitionsService;
import org.hawkular.alerts.engine.service.AlertsEngine;
import org.hawkular.alerts.engine.service.IncomingDataManager;
import org.hawkular.alerts.engine.service.PartitionManager;
import org.hawkular.alerts.filter.CacheClient;
import org.hawkular.alerts.log.MsgLogger;
import org.hawkular.alerts.log.MsgLogging;

/**
 * @author Jay Shaughnessy
 * @author Lucas Ponce
 */
public class IncomingDataManagerImpl implements IncomingDataManager {
    private final MsgLogger log = MsgLogging.getMsgLogger(IncomingDataManagerImpl.class);

//    @ConfigProperty(name = "engine.rules.events.duplicate-filter-time")
    private int minReportingIntervalEvents;

//    @ConfigProperty(name = "engine.rules.data.duplicate-filter-time")
    private int minReportingIntervalData;

    private ExecutorService executor;

    DataDrivenGroupCacheManager dataDrivenGroupCacheManager;

    DefinitionsService definitionsService;

    PartitionManager partitionManager;

    AlertsEngine alertsEngine;

    CacheClient dataIdCache;

    public void setExecutor(ExecutorService executor) {
        this.executor = executor;
    }

    public void setDataDrivenGroupCacheManager(DataDrivenGroupCacheManager dataDrivenGroupCacheManager) {
        this.dataDrivenGroupCacheManager = dataDrivenGroupCacheManager;
    }

    public void setDefinitionsService(DefinitionsService definitionsService) {
        this.definitionsService = definitionsService;
    }

    public void setPartitionManager(PartitionManager partitionManager) {
        this.partitionManager = partitionManager;
    }

    public void setAlertsEngine(AlertsEngine alertsEngine) {
        this.alertsEngine = alertsEngine;
    }

    public void setDataIdCache(CacheClient dataIdCache) {
        this.dataIdCache = dataIdCache;
    }

    public void init() {
        minReportingIntervalEvents = ConfigProvider.getConfig().getValue("engine.rules.events.duplicate-filter-time", Integer.class);
        minReportingIntervalData = ConfigProvider.getConfig().getValue("engine.rules.data.duplicate-filter-time", Integer.class);
    }

    @Override
    public void bufferData(IncomingData incomingData) {
        executor.submit(() -> {
            processData(incomingData);
        });
    }

    @Override
    public void bufferEvents(IncomingEvents incomingEvents) {
        processEvents(incomingEvents);
    }

    private void processData(IncomingData incomingData) {
        log.debugf("Processing [%s] datums for AlertsEngine.", incomingData.incomingData.size());

        // remove data not needed by the defined triggers
        // remove duplicates and apply natural ordering
        TreeSet<Data> filteredData = new TreeSet<Data>(filterIncomingData(incomingData));

        // remove offenders of minReportingInterval. Note, this filters only this incoming batch, this is
        // performed again, downstream,after data has been "stitched together" for evaluation.
        enforceMinReportingInterval(filteredData);

        // check to see if any data can be used to generate data-driven group members
        checkDataDrivenGroupTriggers(filteredData);

        try {
            log.debugf("Sending [%s] datums to AlertsEngine.", filteredData.size());
            alertsEngine.sendData(filteredData);

        } catch (Exception e) {
            log.errorf("Failed to send [%s] datums:", filteredData.size(), e.getMessage());
        }
    }

    private void processEvents(IncomingEvents incomingEvents) {
        log.debugf("Processing [%s] events to AlertsEngine.", incomingEvents.incomingEvents.size());

        // remove events not needed by the defined triggers
        // remove duplicates and apply natural ordering
        TreeSet<Event> filteredEvents = new TreeSet<Event>(filterIncomingEvents(incomingEvents));

        // remove offenders of minReportingInterval. Note, this filters only this incoming batch, this is
        // performed again, downstream,after data has been "stitched together" for evaluation.
        enforceMinReportingIntervalEvents(filteredEvents);

        try {
            alertsEngine.sendEvents(filteredEvents);
        } catch (Exception e) {
            log.errorf("Failed sending [%s] events: %s", filteredEvents.size(), e.getMessage());
        }
    }

    private Collection<Data> filterIncomingData(IncomingData incomingData) {
        Collection<Data> data = incomingData.getIncomingData();
        data = incomingData.isRaw() ? dataIdCache.filterData(data) : data;

        return data;
    }

    private Collection<Event> filterIncomingEvents(IncomingEvents incomingEvents) {
        Collection<Event> events = incomingEvents.getIncomingEvents();
        events = incomingEvents.isRaw() ? dataIdCache.filterEvents(events) : events;

        return events;
    }

    private void enforceMinReportingInterval(TreeSet<Data> orderedData) {
        int beforeSize = orderedData.size();
        Data prev = null;
        for (Iterator<Data> i = orderedData.iterator(); i.hasNext();) {
            Data d = i.next();
            if (!d.same(prev)) {
                prev = d;
            } else {
                if ((d.getTimestamp() - prev.getTimestamp()) < minReportingIntervalData) {
                    log.tracef("MinReportingInterval violation, prev: %s, removed: %s", prev, d);
                    i.remove();
                }
            }
        }
        if (log.isDebugEnabled() && beforeSize != orderedData.size()) {
            log.debugf("MinReportingInterval Data violations: [%s]", beforeSize - orderedData.size());
        }
    }

    private void enforceMinReportingIntervalEvents(TreeSet<Event> orderedEvents) {
        int beforeSize = orderedEvents.size();
        Event prev = null;
        for (Iterator<Event> i = orderedEvents.iterator(); i.hasNext();) {
            Event e = i.next();
            if (!e.same(prev)) {
                prev = e;
            } else {
                if ((e.getCtime() - prev.getCtime()) < minReportingIntervalEvents) {
                    log.tracef("MinReportingInterval violation, prev: %s, removed: %s", prev, e);
                    i.remove();
                }
            }
        }
        if (log.isDebugEnabled() && beforeSize != orderedEvents.size()) {
            log.debugf("MinReportingInterval Events violations: [%s]", beforeSize - orderedEvents.size());
        }
    }

    private void checkDataDrivenGroupTriggers(Collection<Data> data) {
        if (!dataDrivenGroupCacheManager.isCacheActive()) {
            return;
        }

        for (Data d : data) {
            if (isEmpty(d.getSource())) {
                continue;
            }

            String tenantId = d.getTenantId();
            String dataId = d.getId();
            String dataSource = d.getSource();

            Set<String> groupTriggerIds = dataDrivenGroupCacheManager.needsSourceMember(tenantId, dataId, dataSource);

            // Add a trigger members for the source

            for (String groupTriggerId : groupTriggerIds) {
                try {
                    definitionsService.addDataDrivenMemberTrigger(tenantId, groupTriggerId, dataSource);

                } catch (Exception e) {
                    log.errorf("Failed to add Data-Driven Member Trigger for [%s:%s]: %s:", groupTriggerId, d,
                            e.getMessage());
                }
            }
        }
    }

    public static class IncomingData {
        private Collection<Data> incomingData;
        private boolean raw;

        public IncomingData(Collection<Data> incomingData, boolean raw) {
            super();
            this.incomingData = incomingData;
            this.raw = raw;
        }

        public Collection<Data> getIncomingData() {
            return incomingData;
        }

        public boolean isRaw() {
            return raw;
        }
    }

    public static class IncomingEvents {
        private Collection<Event> incomingEvents;
        private boolean raw;

        public IncomingEvents(Collection<Event> incomingEvents, boolean raw) {
            super();
            this.incomingEvents = incomingEvents;
            this.raw = raw;
        }

        public Collection<Event> getIncomingEvents() {
            return incomingEvents;
        }

        public boolean isRaw() {
            return raw;
        }
    }

}