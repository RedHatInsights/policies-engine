package org.hawkular.alerts.engine.impl;

import org.eclipse.microprofile.config.ConfigProvider;
import org.hawkular.alerts.api.model.condition.CompareCondition;
import org.hawkular.alerts.api.model.condition.Condition;
import org.hawkular.alerts.api.model.condition.ConditionEval;
import org.hawkular.alerts.api.model.condition.MissingCondition;
import org.hawkular.alerts.api.model.condition.MissingConditionEval;
import org.hawkular.alerts.api.model.dampening.Dampening;
import org.hawkular.alerts.api.model.data.Data;
import org.hawkular.alerts.api.model.event.Alert;
import org.hawkular.alerts.api.model.event.Event;
import org.hawkular.alerts.api.model.trigger.FullTrigger;
import org.hawkular.alerts.api.model.trigger.Trigger;
import org.hawkular.alerts.api.services.ActionsService;
import org.hawkular.alerts.api.services.AlertsService;
import org.hawkular.alerts.api.services.DataExtension;
import org.hawkular.alerts.api.services.DefinitionsService;
import org.hawkular.alerts.api.services.EventExtension;
import org.hawkular.alerts.api.services.ExtensionsService;
import org.hawkular.alerts.engine.impl.AlertsEngineCache.DataEntry;
import org.hawkular.alerts.engine.service.AlertsEngine;
import org.hawkular.alerts.engine.service.PartitionDataListener;
import org.hawkular.alerts.engine.service.PartitionManager;
import org.hawkular.alerts.engine.service.PartitionManager.Operation;
import org.hawkular.alerts.engine.service.PartitionTriggerListener;
import org.hawkular.alerts.engine.service.RulesEngine;
import org.hawkular.alerts.engine.util.MissingState;
import org.hawkular.alerts.log.AlertingLogger;
import org.hawkular.alerts.log.MsgLogging;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;

import static org.hawkular.alerts.api.util.Util.isEmpty;

/**
 * Implementation for {@link org.hawkular.alerts.api.services.AlertsService}.
 * This implementation processes data asynchronously using a buffer queue.
 *
 * The engine may run in single-node or distributed (i.e. multi-node/clustered) mode.
 * By design this class handles both and it should be transparent to callers which mode
 * is currently in use.
 *
 * @author Jay Shaughnessy
 * @author Lucas Ponce
 */
public class AlertsEngineImpl implements AlertsEngine, PartitionTriggerListener, PartitionDataListener {
    private final AlertingLogger log = MsgLogging.getMsgLogger(AlertingLogger.class, AlertsEngineImpl.class);

//    @ConfigProperty(name = "engine.alerts.engine-delay")
    int delay;

//    @ConfigProperty(name = "engine.alerts.engine-period")
    int period;

    private TreeSet<Data> pendingData;
    private final Object pendingDataSync = new Object();
    private TreeSet<Event> pendingEvents;
    private final Object pendingEventsSync = new Object();

    private final List<Alert> alerts;
    private final List<Event> events;
    private final Set<Dampening> pendingTimeouts;
    private final Map<Trigger, List<Set<ConditionEval>>> autoResolvedTriggers;
    private final Set<Condition> evaluatedConditions;
    private final Set<Trigger> disabledTriggers;
    private final Set<MissingState> missingStates;

    private final Timer wakeUpTimer;
    private TimerTask rulesTask;

    /*
        All incoming Data and Events go through front-line global filtering (via IncomingDataManager)
        and therefore, in a non-distributed env the global filtering is equivalent to node-specific
        filtering. As such we don't need to filter via nodeSpecificDataIdCache.
     */
    private AlertsEngineCache alertsEngineCache = null;
    boolean distributed = false;

//    @ConfigProperty(name = "engine.alerts.engine-extensions")
    boolean engineExtensions;

//    @ConfigProperty(name = "engine.alerts.condition-evaluation-time")
    boolean updateLastEvaluated;

    RulesEngine rules;

    DefinitionsService definitions;

    ActionsService actions;

    AlertsService alertsService;

    PartitionManager partitionManager;

    ExtensionsService extensionsService;

    private ExecutorService executor;

    private boolean triggersLifecycleDisabled;

    public AlertsEngineImpl() {
        pendingData = new TreeSet<>();
        pendingEvents = new TreeSet<>();
        alerts = new ArrayList<>();
        events = new ArrayList<>();
        pendingTimeouts = new HashSet<>();
        autoResolvedTriggers = new HashMap<>();
        evaluatedConditions = new HashSet<>();
        disabledTriggers = new HashSet<>();
        missingStates = new HashSet<>();

        delay = ConfigProvider.getConfig().getValue("engine.alerts.engine-delay", Integer.class);
        period = ConfigProvider.getConfig().getValue("engine.alerts.engine-period", Integer.class);
        engineExtensions = ConfigProvider.getConfig().getValue("engine.alerts.engine-extensions", Boolean.class);
        updateLastEvaluated = ConfigProvider.getConfig().getValue("engine.alerts.condition-evaluation-time", Boolean.class);
        wakeUpTimer = new Timer("AlertsEngineImpl-Timer");
    }

    public void disableTriggersLifecycle() {
        triggersLifecycleDisabled = true;
    }

    public RulesEngine getRules() {
        return rules;
    }

    public void setRules(RulesEngine rules) {
        this.rules = rules;
    }

    public void setDefinitions(DefinitionsService definitions) {
        this.definitions = definitions;
    }

    public void setActions(ActionsService actions) {
        this.actions = actions;
    }

    public void setAlertsService(AlertsService alertsService) {
        this.alertsService = alertsService;
    }

    public void setPartitionManager(PartitionManager partitionManager) {
        this.partitionManager = partitionManager;
    }

    public void setExtensionsService(ExtensionsService extensionsService) {
        this.extensionsService = extensionsService;
    }

    public void setExecutor(ExecutorService executor) {
        this.executor = executor;
    }

    public void initServices() {
        try {
            distributed = partitionManager.isDistributed();
            if (distributed) {
                log.debug("Registering PartitionManager listeners...");
                alertsEngineCache = new AlertsEngineCache();
                partitionManager.registerDataListener(this);
                partitionManager.registerTriggerListener(this);
            }
            executor.submit(() -> {
                /*
                    A reload() operation means that all triggers from the backend should be reloaded into
                    the AlertsEngine memory. In a distributed environment, the node that execute the reload()
                    operation loads the triggers assigned to it and notify other nodes to load rest of the triggers.
                 */
                reload();
            });
        } catch (Throwable t) {
            log.error("Can not initialize Alerts Service", t);
        }
    }

    public void shutdown() {
        rulesTask.cancel();
        wakeUpTimer.cancel();
    }

    @Override
    public void clear() {
        rulesTask.cancel();

        rules.clear();

        pendingData.clear();
        pendingEvents.clear();
        alerts.clear();
        events.clear();
        pendingTimeouts.clear();
        autoResolvedTriggers.clear();
        disabledTriggers.clear();
        missingStates.clear();
        evaluatedConditions.clear();

        rulesTask = new RulesInvoker();
        wakeUpTimer.schedule(rulesTask, delay, period);
    }

    @Override
    public void reload() {
        log.debug("Start a full reload of the AlertsEngine");
        rules.reset();
        if (distributed) {
            alertsEngineCache.clear();
        }
        if (rulesTask != null) {
            rulesTask.cancel();
        }

        Collection<Trigger> triggers = null;
        try {
            triggers = definitions.getAllTriggers();
        } catch (Exception e) {
            log.debug(e.getMessage(), e);
            log.errorDefinitionsService("Triggers", e.getMessage());
        }

        if (!isEmpty(triggers)) {

            triggers.stream().filter(Trigger::isLoadable).forEach(t -> {
                /*
                    In distributed scenario a reload should delegate into the PartitionManager to load the trigger on
                    the node which belongs
                 */
                if (distributed) {
                    partitionManager.notifyTrigger(Operation.UPDATE, t.getTenantId(), t.getId());
                } else {
                    reloadTrigger(t);
                }
            });
        }

        rules.addGlobal("log", log);
        rules.addGlobal("actions", actions);
        rules.addGlobal("alerts", alerts);
        rules.addGlobal("events", events);
        rules.addGlobal("pendingTimeouts", pendingTimeouts);
        rules.addGlobal("autoResolvedTriggers", autoResolvedTriggers);
        rules.addGlobal("evaluatedConditions", evaluatedConditions);
        rules.addGlobal("disabledTriggers", disabledTriggers);

        rulesTask = new RulesInvoker();
        wakeUpTimer.schedule(rulesTask, delay, period);
    }

    @Override
    public void addTrigger(final String tenantId, final String triggerId) {
        if (isEmpty(tenantId)) {
            throw new IllegalArgumentException("TenantId must be not null");
        }
        if (isEmpty(triggerId)) {
            throw new IllegalArgumentException("TriggerId must be not null");
        }
        if (distributed) {
            Trigger trigger = null;
            try {
                trigger = definitions.getTrigger(tenantId, triggerId);
                if (log.isDebugEnabled()) {
                    log.debug("addTrigger(" + trigger + ")");
                }
            } catch (Exception e) {
                log.debug(e.getMessage(), e);
                log.errorDefinitionsService("Trigger", e.getMessage());
            }
            if (trigger != null && trigger.isLoadable()) {
                partitionManager.notifyTrigger(Operation.ADD, trigger.getTenantId(), trigger.getId());
            }
        }
    }

    @Override
    public void reloadTrigger(final String tenantId, final String triggerId) {
        if (isEmpty(tenantId)) {
            throw new IllegalArgumentException("TenantId must be not null");
        }
        if (isEmpty(triggerId)) {
            throw new IllegalArgumentException("TriggerId must be not null");
        }

        Trigger trigger = null;
        try {
            trigger = definitions.getTrigger(tenantId, triggerId);
        } catch (Exception e) {
            log.debug(e.getMessage(), e);
            log.errorDefinitionsService("Trigger", e.getMessage());
        }
        if (null == trigger) {
            if (log.isDebugEnabled()) {
                log.debug("Trigger not found for triggerId [" + triggerId + "], removing from rulebase if it exists");
            }
            Trigger doomedTrigger = new Trigger(tenantId, triggerId, "doomed");
            removeTrigger(doomedTrigger);
            return;
        }

        /*
            Leave now if this happens to be a group trigger, they do not get loaded into the rule base.
         */
        if (trigger.isGroup()) {
            if (log.isDebugEnabled()) {
                log.debug("Skipping reload of group trigger [" + trigger.getTenantId() + "/" + trigger.getId() + "]");
            }
            return;
        }
        /*
            In distributed scenario a reload should delegate into the PartitionManager to load the trigger on the node
            which belongs.
         */
        if (distributed) {
            partitionManager.notifyTrigger(Operation.UPDATE, trigger.getTenantId(), trigger.getId());
        } else {
            reloadTrigger(trigger);
        }
    }

    private void reloadTrigger(Trigger trigger) {
        if (null == trigger) {
            throw new IllegalArgumentException("Trigger must be not null");
        }
        if (log.isDebugEnabled()) {
            log.debug("Reloading " + trigger);
        }

        // Look for the Trigger in the rules engine, if it is there then remove everything about it
        // Note that removeTrigger relies only on tenantId+triggerId.
        removeTrigger(trigger);

        try {
            if (distributed) {
                trigger = definitions.getTrigger(trigger.getTenantId(), trigger.getId());
            }
            if (trigger != null && trigger.isLoadable()) {
                Collection<Condition> conditionSet = definitions.getTriggerConditions(trigger.getTenantId(),
                        trigger.getId(), null);
                Collection<Dampening> dampenings = definitions.getTriggerDampenings(trigger.getTenantId(),
                        trigger.getId(), null);

                /*
                    Cache dataId from conditions, Handle MissingCondition's MissingState
                 */
                for (Condition c : conditionSet) {
                    if (distributed) {
                        DataEntry entry = new DataEntry(c.getTenantId(), c.getTriggerId(), c.getDataId());
                        alertsEngineCache.add(entry);
                        if (Condition.Type.COMPARE == c.getType()) {
                            String data2Id = ((CompareCondition) c).getData2Id();
                            DataEntry entry2 = new DataEntry(c.getTenantId(), c.getTriggerId(), data2Id);
                            alertsEngineCache.add(entry2);
                        }
                    }
                    if (c instanceof MissingCondition) {
                        // MissingState keeps a reference to the Trigger fact to check active trigger mode
                        MissingState missingState = new MissingState(trigger, (MissingCondition) c);
                        // MissingStates are modified inside the rules engine
                        synchronized (missingStates) {
                            missingStates.remove(missingState);
                            missingStates.add(missingState);
                            rules.addFact(missingState);
                        }
                    }
                }

                rules.addFact(trigger);
                rules.addFacts(conditionSet);
                if (!dampenings.isEmpty()) {
                    rules.addFacts(dampenings);
                }
            }
        } catch (Exception e) {
            log.debug(e.getMessage(), e);
            log.errorDefinitionsService("Conditions/Dampening", e.getMessage());
        }
    }

    @Override
    public void loadTrigger(FullTrigger fullTrigger) {
        if(fullTrigger == null) {
            throw new IllegalArgumentException("FullTrigger must be not null");
        }
        if(distributed) {
            throw new IllegalArgumentException("This hotpath of loading fullTrigger to AlertsEngine can't be used in distributed mode");
        }
        Trigger trigger = fullTrigger.getTrigger();
        if (trigger != null && trigger.isLoadable()) {
            List<Condition> conditions = fullTrigger.getConditions();
            List<Dampening> dampenings = fullTrigger.getDampenings();
            rules.addFact(trigger);
            rules.addFacts(conditions);
            if (!dampenings.isEmpty()) {
                rules.addFacts(dampenings);
            }
        }
    }

    @Override
    public Trigger getLoadedTrigger(Trigger trigger) {
        if (null == trigger) {
            throw new IllegalArgumentException("Trigger must be not null");
        }

        Trigger loadedTrigger = null;
        try {
            loadedTrigger = (Trigger) rules.getFact(trigger);
        } catch (Exception e) {
            log.errorf("Failed to get Trigger from engine %s: %s", trigger, e);
        }
        return loadedTrigger;
    }

    @Override
    public void removeTrigger(String tenantId, String triggerId) {
        if (isEmpty(tenantId)) {
            throw new IllegalArgumentException("TenantId must be not null");
        }
        if (isEmpty(triggerId)) {
            throw new IllegalArgumentException("TriggerId must be not null");
        }

        Trigger triggerToRemove = new Trigger(tenantId, triggerId, "trigger-to-remove");
        if (distributed) {
            partitionManager.notifyTrigger(Operation.REMOVE, triggerToRemove.getTenantId(), triggerToRemove.getId());
        } else {
            removeTrigger(triggerToRemove);
        }
    }

    private void removeTrigger(Trigger trigger) {
        final String tenantId = trigger.getTenantId();
        final String triggerId = trigger.getId();

        // If necessary, clean up working memory
        if (null != rules.getFact(trigger)) {
            // Remove the Trigger fact
            rules.removeFact(trigger);

            // Remove related facts
            rules.removeFacts(f -> {
                if (f instanceof Dampening) {
                    return ((Dampening) f).getTenantId().equals(tenantId)
                            && ((Dampening) f).getTriggerId().equals(triggerId);
                } else if (f instanceof Condition) {
                    return ((Condition) f).getTenantId().equals(tenantId) &&
                            ((Condition) f).getTriggerId().equals(triggerId);
                } else if (f instanceof MissingState) {
                    return ((MissingState) f).getTenantId().equals(tenantId) &&
                            ((MissingState) f).getTriggerId().equals(triggerId);
                }
                return false;
            });
        } else {
            log.debugf("Trigger Fact not found. Nothing removed from rulebase %s", trigger.toString());
        }

        // Remove dataId associated from cache
        if (distributed) {
            alertsEngineCache.remove(trigger.getTenantId(), trigger.getId());
        }
        // Remove any MissingState being managed for the trigger
        synchronized (missingStates) {
            missingStates.removeIf(missingState -> missingState.getTenantId().equals(trigger.getTenantId()) &&
                    missingState.getTriggerId().equals(triggerId));
        }
    }

    // We allow concurrent threads to make this call in order to process distributed data in parallel. We
    // use synchronized blocks to protect pendingData.
    @Override
    public void sendData(TreeSet<Data> data) {
        if (data == null) {
            throw new IllegalArgumentException("Data must be not null");
        }
        if (data.isEmpty()) {
            return;
        }

        addData(data);

        if (distributed) {
            partitionManager.notifyData(new ArrayList<>(data));
        }
    }

    private void addData(TreeSet<Data> data) {
        if (distributed) {
            data = filterIncomingDataForNode(data);
        }

        if (engineExtensions) {
            data = processDataExtensions(data);
        }

        synchronized (pendingDataSync) {
            log.debugf("Adding [%s] to pendingData [%s]", data, pendingData);
            pendingData.addAll(data);
        }
    }

    private TreeSet<Data> filterIncomingDataForNode(TreeSet<Data> data) {
        TreeSet<Data> filteredData = new TreeSet<>(data);
        filteredData.removeIf(d -> !alertsEngineCache.isDataIdActive(d.getTenantId(), d.getId()));
        return filteredData;
    }

    private TreeSet<Data> processDataExtensions(TreeSet<Data> data) {
        Set<DataExtension> extensions = extensionsService.getDataExtensions();
        if (!extensions.isEmpty()) {
            for (DataExtension extension : extensions) {
                data = extension.processData(data);
            }
        }
        return data;
    }

    // We allow concurrent threads to make this call in order to process distributed data in parallel. We
    // use synchronized blocks to protect pendingEvents.
    @Override
    public void sendEvents(TreeSet<Event> events) {
        if (events == null) {
            throw new IllegalArgumentException("Events must be not null");
        }
        if (events.isEmpty()) {
            return;
        }

        addEvents(events);

        if (distributed) {
            partitionManager.notifyEvents(new ArrayList<>(events));
        }
    }

    private void addEvents(TreeSet<Event> events) {
        if (distributed) {
            events = filterIncomingEventsForNode(events);
        }

        if (engineExtensions) {
            events = processEventsExtensions(events);
        }

        synchronized (pendingEventsSync) {
            log.debugf("Adding [%s] to pendingEvents [%s]", events, pendingEvents);
            pendingEvents.addAll(events);
        }
    }

    private TreeSet<Event> filterIncomingEventsForNode(TreeSet<Event> events) {
        TreeSet<Event> filteredEvents = new TreeSet<>(events);
        filteredEvents.removeIf(e -> !alertsEngineCache.isDataIdActive(e.getTenantId(), e.getDataId()));
        return filteredEvents;
    }

    private TreeSet<Event> processEventsExtensions(TreeSet<Event> events) {
        Set<EventExtension> extensions = extensionsService.getEventExtensions();
        for (EventExtension extension : extensions) {
            events = extension.processEvents(events);
        }
        return events;
    }

    private TreeSet<Data> getAndClearPendingData() {
        TreeSet<Data> result;
        synchronized (pendingDataSync) {
            result = pendingData;
            pendingData = new TreeSet<>();
        }
        return result;
    }

    private TreeSet<Event> getAndClearPendingEvents() {
        TreeSet<Event> result;
        synchronized (pendingEventsSync) {
            result = pendingEvents;
            pendingEvents = new TreeSet<>();
        }
        return result;
    }

    private class RulesInvoker extends TimerTask {
        @Override
        public void run() {
            int numTimeouts = checkPendingTimeouts();

            int numMissingEvals = checkMissingStates();

            if (!pendingData.isEmpty() || !pendingEvents.isEmpty() || numTimeouts > 0 || numMissingEvals > 0) {
                TreeSet<Data> newData = getAndClearPendingData();
                TreeSet<Event> newEvents = getAndClearPendingEvents();

                log.debugf("Executing rules engine on %s datums, %s events, %s dampening timeouts.", newData.size(),
                        newEvents.size(), numTimeouts);

                try {
                    if (newData.isEmpty() && newEvents.isEmpty()) {
                        rules.fireNoData();
                    } else {
                        if (!newData.isEmpty()) {
                            rules.addData(newData);
                        }
                        if (!newEvents.isEmpty()) {
                            rules.addEvents(newEvents);
                        }

                        // release to GC
                        newData = null;
                        newEvents = null;

                        rules.fire();
                    }

                    handleAlerts();
                    alertsService.persistEvents(events).await().indefinitely();
                    if (distributed && !events.isEmpty()) {
                        /*
                            Generated events on a node should be notified to other nodes for chained triggers
                         */
                        partitionManager.notifyEvents(new ArrayList<>(events));
                    }
                    events.clear();
                    handleDisabledTriggers();
                    handleAutoResolvedTriggers();
                    handleConditionEvaluationTimes();
                    actions.flush();
                } catch (Exception e) {
                    log.debug("Error on rules processing: %s",e);
                    log.errorProcessingRules(e.getMessage());
                } finally {
                    alerts.clear();
                    events.clear();
                }
            }
        }

        private int checkPendingTimeouts() {
            if (pendingTimeouts.isEmpty()) {
                return 0;
            }

            long now = System.currentTimeMillis();
            Set<Dampening> timeouts = null;
            for (Dampening d : pendingTimeouts) {
                if (now < d.getTrueEvalsStartTime() + d.getEvalTimeSetting()) {
                    continue;
                }

                d.setSatisfied(true);
                try {
                    log.debugf("Dampening Timeout Hit! %s", d.toString());
                    rules.updateFact(d);
                    if (null == timeouts) {
                        timeouts = new HashSet<>();
                    }
                    timeouts.add(d);
                } catch (Exception e) {
                    log.errorf(e, "Unable to update Dampening Fact on Timeout! %s", d.toString());
                }

            }

            if (null == timeouts) {
                return 0;
            }

            pendingTimeouts.removeAll(timeouts);
            return timeouts.size();
        }
    }

    private void handleAlerts() throws Exception {
        alertsService.addAlerts(alerts);
        if (!triggersLifecycleDisabled) {
            for (Alert alert : alerts) {
                definitions.addLifecycleToTrigger(alert.getTenantId(), alert.getTriggerId(), Trigger.TriggerLifecycle.ALERT_GENERATE);
            }
        }
        alerts.clear();
    }

    private void handleDisabledTriggers() {
        try {
            for (Trigger t : disabledTriggers) {
                try {
                    // TODO This will now generate "DISABLED" .. not AUTO_DISABLED
                    definitions.updateTriggerEnablement(t.getTenantId(), t.getId(), false, null);
                } catch (Exception e) {
                    log.errorf(e, "Failed to persist updated trigger. Could not autoDisable %s.", t);
                }
            }
        } finally {
            disabledTriggers.clear();
        }
    }

    private void handleAutoResolvedTriggers() {
        try {
            for (Entry<Trigger, List<Set<ConditionEval>>> entry : autoResolvedTriggers.entrySet()) {
                Trigger t = entry.getKey();
                boolean manualReload = !t.isAutoResolveAlerts();

                // calling resolveAlertsForTrigger will result in a trigger reload (unless it fails),
                // otherwise, manually reload the trigger back into the engine (in firing mode).
                if (t.isAutoResolveAlerts()) {
                    try {
                        alertsService.resolveAlertsForTrigger(t.getTenantId(), t.getId(), null,
                                null, entry.getValue());
                    } catch (Exception e) {
                        manualReload = true;
                        log.errorf("Failed to resolve Alerts. Could not AutoResolve alerts for trigger %s.", t);
                    }
                }

                if (manualReload) {
                    try {
                        reloadTrigger(t.getTenantId(), t.getId());
                    } catch (Exception e) {
                        log.errorf("Failed to reload AutoResolved Trigger: %s.", t);
                    }
                }
            }
        } finally {
            autoResolvedTriggers.clear();
        }
    }

    private static class ConditionKey {
        String tenantId;
        String triggerId;

        public ConditionKey(String tenantId, String triggerId) {
            this.tenantId = tenantId;
            this.triggerId = triggerId;
        }

        public String getTenantId() {
            return tenantId;
        }

        public String getTriggerId() {
            return triggerId;
        }
    }

    private void handleConditionEvaluationTimes() {
        if(updateLastEvaluated) {
            HashMap<ConditionKey, Set<Condition>> groupedConditionUpdates = new HashMap<>();
            for (Condition evaluatedCondition : evaluatedConditions) {
                ConditionKey condKey = new ConditionKey(evaluatedCondition.getTenantId(), evaluatedCondition.getTriggerId());
                Set<Condition> conditions = groupedConditionUpdates.get(condKey);
                if(conditions == null) {
                    conditions = new HashSet<>();
                }
                conditions.add(evaluatedCondition);
                groupedConditionUpdates.put(condKey, conditions);
            }

            for (Entry<ConditionKey, Set<Condition>> condEntry : groupedConditionUpdates.entrySet()) {
                definitions.updateConditions(condEntry.getKey().getTenantId(), condEntry.getKey().getTriggerId(), condEntry.getValue());
            }
        }

        evaluatedConditions.clear();
    }

    private int checkMissingStates() {
        if (missingStates.isEmpty()) {
            return 0;
        }

        int numMatchingEvals = 0;
        for (MissingState missingState : missingStates) {
            if (missingState.getTriggerMode() != missingState.getTrigger().getMode()) {
                continue;
            }

            long now = System.currentTimeMillis();
            rules.removeFact(missingState);
            missingState.setTime(now);
            if (missingState.getCondition().match(missingState.getPreviousTime(), now)) {
                MissingConditionEval eval = new MissingConditionEval(missingState.getCondition(),
                        missingState.getPreviousTime(),
                        now);
                missingState.setPreviousTime(now);
                rules.addFact(eval);
                numMatchingEvals++;
            }
            rules.addFact(missingState);
        }

        return numMatchingEvals;
    }

    /*
        Data incoming from a different node.  This has already been globally filtered but not locally filtered.
        It does not need to be re-propagated.

        We allow concurrent threads to make this call in order to process distributed data in parallel. We
        use synchronized blocks to protect pendingData.
     */
    @Override
    public void onNewData(Collection<Data> data) {
        addData(new TreeSet<>(data));
    }

    /*
        Events incoming from a different node.  This has already been globally filtered but not locally filtered.
        It does not need to be re-propagated.

        We allow concurrent threads to make this call in order to process distributed data in parallel. We
        use synchronized blocks to protect pendingEvents.
     */
    @Override
    public void onNewEvents(Collection<Event> events) {
        addEvents(new TreeSet<>(events));
    }

    /*
        This listener method is invoked on distributed scenarios.
        When a trigger is modified, PartitionManager detects which node holds the trigger and send the event.
        Local node is responsible to remove/reload the trigger from AlertsEngine memory.
     */
    @Override
    public void onTriggerChange(Operation operation, String tenantId, String triggerId) {
        log.debugf("Executing: %s tenantId: %s triggerId: %s", operation, tenantId, triggerId);
        switch (operation) {
            case ADD:
            case UPDATE:
                Trigger reloadTrigger = new Trigger(tenantId, triggerId, "reload-trigger");
                reloadTrigger(reloadTrigger);
                break;
            case REMOVE:
                Trigger removeTrigger = new Trigger(tenantId, triggerId, "remove-trigger");
                removeTrigger(removeTrigger);
                break;
        }
    }

    /*
        This listener method is invoked on distributed scenarios.
        When topology changes, new nodes added or removed, PartitionManager recalculate global triggers partition.
        On each node, PartitionManager invokes this method to indicate triggers should hold, and the "delta" of
        additions/removals.
        With this delta, the process of remove/reload triggers across cluster is minimized.
     */
    @Override
    public void onPartitionChange(Map<String, List<String>> partition, Map<String, List<String>> removed,
            Map<String, List<String>> added) {
        log.debug("Executing: PartitionChange ");
        log.debugf("Local partition: %s", partition);
        log.debugf("Removed: %s", removed);
        log.debugf("Added: %s", added);

        if (!pendingData.isEmpty() || !pendingEvents.isEmpty()) {
            if (!pendingData.isEmpty()) {
                log.warnf("Pending Data onPartitionChange: %s.", pendingData);
            }
            if (pendingEvents.isEmpty()) {
                log.warnf("Pending Events onPartitionChange: %s.", pendingData);
            }
        }

        /*
            Removing old triggers for this node
         */
        for (Entry<String, List<String>> entry : removed.entrySet()) {
            String tenantId = entry.getKey();
            entry.getValue().stream().forEach(triggerId -> {
                Trigger removeTrigger = new Trigger(tenantId, triggerId, "to-remove-from-alerts-engine");
                removeTrigger(removeTrigger);
            });
        }

        /*
            Reloading new triggers for this node
         */
        for (Entry<String, List<String>> entry : added.entrySet()) {
            String tenantId = entry.getKey();
            entry.getValue().stream().forEach(triggerId -> {
                reloadTrigger(tenantId, triggerId);
            });
        }
    }
}
