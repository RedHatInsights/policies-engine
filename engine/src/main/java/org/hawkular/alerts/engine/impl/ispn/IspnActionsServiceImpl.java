package org.hawkular.alerts.engine.impl.ispn;

import org.eclipse.microprofile.config.ConfigProvider;
import org.hawkular.alerts.api.model.action.Action;
import org.hawkular.alerts.api.model.action.ActionDefinition;
import org.hawkular.alerts.api.model.event.Event;
import org.hawkular.alerts.api.model.paging.ActionComparator;
import org.hawkular.alerts.api.model.paging.ActionComparator.Field;
import org.hawkular.alerts.api.model.paging.Order;
import org.hawkular.alerts.api.model.paging.Page;
import org.hawkular.alerts.api.model.paging.Pager;
import org.hawkular.alerts.api.model.trigger.Trigger;
import org.hawkular.alerts.api.model.trigger.TriggerAction;
import org.hawkular.alerts.api.services.ActionListener;
import org.hawkular.alerts.api.services.ActionsCriteria;
import org.hawkular.alerts.api.services.ActionsService;
import org.hawkular.alerts.api.services.DefinitionsService;
import org.hawkular.alerts.engine.cache.ActionsCacheManager;
import org.hawkular.alerts.engine.cache.IspnCacheManager;
import org.hawkular.alerts.engine.impl.AlertsContext;
import org.hawkular.alerts.engine.impl.ispn.model.IspnAction;
import org.hawkular.alerts.engine.util.ActionsValidator;
import org.hawkular.alerts.log.AlertingLogger;
import org.hawkular.alerts.log.MsgLogging;
import org.infinispan.Cache;
import org.infinispan.query.Search;
import org.infinispan.query.dsl.QueryFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.hawkular.alerts.api.util.Util.isEmpty;
import static org.infinispan.context.Flag.IGNORE_RETURN_VALUES;

/**
 * Infinispan implementation of {@link org.hawkular.alerts.api.services.ActionsService}.
 *
 * @author Jay Shaughnessy
 * @author Lucas Ponce
 */
public class IspnActionsServiceImpl implements ActionsService {
    private final AlertingLogger log = MsgLogging.getMsgLogger(AlertingLogger.class, IspnActionsServiceImpl.class);

    private static final String WAITING_RESULT = "WAITING";
    private static final String UNKNOWN_RESULT = "UNKNOWN";

    AlertsContext alertsContext;

    DefinitionsService definitions;

    ActionsCacheManager actionsCacheManager;

    private boolean fastActionsStore = false;

    Cache<String, Object> actionsStore;

    QueryFactory queryFactory;

    /**
     * Set the TTL of actions to the same as TTL of Alerts
     */
    long alertsLifespanInHours;

    boolean actionsStoreDisabled;

    public void init() {
        fastActionsStore = ConfigProvider.getConfig().getValue("engine.backend.ispn.actions-ephemeral", Boolean.class);
        actionsStoreDisabled = ConfigProvider.getConfig().getOptionalValue("engine.actions-store.disabled", Boolean.class).orElse(false);
        if (actionsStoreDisabled) {
            log.info("The actions store is disabled");
        }
        if(fastActionsStore) {
            actionsStore = IspnCacheManager.getCacheManager().getCache("actions");
        } else {
            // Persistent caching option
            actionsStore = IspnCacheManager.getCacheManager().getCache("backend");
        }
        if (actionsStore == null) {
            log.error("Ispn backend / actions cache not found. Check configuration.");
            throw new RuntimeException("backend cache not found");
        }
        queryFactory = Search.getQueryFactory(actionsStore);
        alertsLifespanInHours = ConfigProvider.getConfig().getValue("engine.backend.ispn.alerts-lifespan", Long.class);
    }

    public void setAlertsContext(AlertsContext alertsContext) {
        this.alertsContext = alertsContext;
    }

    public void setDefinitions(DefinitionsService definitions) {
        this.definitions = definitions;
    }

    public void setActionsCacheManager(ActionsCacheManager actionsCacheManager) {
        this.actionsCacheManager = actionsCacheManager;
    }

    @Override
    public void send(Trigger trigger, Event event) {
        if (trigger == null) {
            throw new IllegalArgumentException("Trigger must be not null");
        }

        if (!isEmpty(trigger.getActions())) {
            for (TriggerAction triggerAction : trigger.getActions()) {
                send(triggerAction, event);
            }
        }

        if (actionsCacheManager.hasGlobalActions()) {
            Collection<ActionDefinition> globalActions = actionsCacheManager.getGlobalActions(trigger.getTenantId());
            for (ActionDefinition globalAction : globalActions) {
                send(globalAction, event);
            }
        }
    }

    @Override
    public void updateResult(Action action) {
        if (action == null || isEmpty(action.getActionPlugin()) || isEmpty(action.getActionId())
                || isEmpty(action.getEventId())) {
            throw new IllegalArgumentException("Action or pk field must be not null");
        }
        if (action.getEvent() == null) {
            throw new IllegalArgumentException("Action event must not be null");
        }
        if (action.getResult() == null) {
            action.setResult(UNKNOWN_RESULT);
        }

        try {
            String pk = IspnPk.pk(action);
            IspnAction IspnAction = (IspnAction) actionsStore.get(pk);
            if (IspnAction == null) {
                insertAction(action);
                log.debugf("No existing action found for %s, inserting %s", pk, action);
                return;
            }
            Action existingAction = IspnAction.getAction();
            existingAction.setResult(action.getResult());
            actionsStore.getAdvancedCache().withFlags(IGNORE_RETURN_VALUES).put(pk, new IspnAction(existingAction));
        } catch (Exception e) {
            log.errorDatabaseException(e.getMessage());
            throw e;
        }
    }

    @Override
    public void flush() {
        for (ActionListener listener : alertsContext.getActionsListeners()) {
            listener.flush();
        }
    }

    @Override
    public Page<Action> getActions(String tenantId, ActionsCriteria criteria, Pager pager) throws Exception {
        if (isEmpty(tenantId)) {
            throw new IllegalArgumentException("TenantId must be not null");
        }
        boolean filter = (null != criteria && criteria.hasCriteria());
        if (filter) {
            log.debugf("getActions criteria: %s", criteria);
        }

        StringBuilder query = new StringBuilder("from org.hawkular.alerts.engine.impl.ispn.model.IspnAction where");
        query.append(String.format(" tenantId = '%s'", tenantId));

        if (filter) {
            if (criteria.hasCTimeCriteria()) {
                if (criteria.hasRangeCriteria()) {
                    query.append(String.format(" and ctime between %d and %d", criteria.getStartTime(),
                            criteria.getEndTime()));
                } else if (criteria.hasStartCriteria()) {
                    query.append(String.format(" and ctime >= %d", criteria.getStartTime()));
                } else {
                    query.append(String.format(" and ctime <= %d", criteria.getEndTime()));
                }
            }
            if (criteria.hasActionIdCriteria()) {
                Set<String> actionIds = new HashSet<>();
                if (null != criteria.getActionIds()) {
                    actionIds.addAll(criteria.getActionIds());
                }
                if (null != criteria.getActionId()) {
                    actionIds.add(criteria.getActionId());
                }

                query.append(inClause("actionId", actionIds));
            }
            if (criteria.hasActionPluginCriteria()) {
                Set<String> actionPlugins = new HashSet<>();
                if (null != criteria.getActionPlugins()) {
                    actionPlugins.addAll(criteria.getActionPlugins());
                }
                if (null != criteria.getActionPlugin()) {
                    actionPlugins.add(criteria.getActionPlugin());
                }
                query.append(inClause("actionPlugin", actionPlugins));
            }
            if (criteria.hasEventIdCriteria()) {
                Set<String> eventIds = new HashSet<>();
                if (null != criteria.getEventIds()) {
                    eventIds.addAll(criteria.getEventIds());
                }
                if (null != criteria.getEventId()) {
                    eventIds.add(criteria.getEventId());
                }
                query.append(inClause("eventId", eventIds));
            }
            if (criteria.hasResultCriteria()) {
                Set<String> results = new HashSet<>();
                if (null != criteria.getResults()) {
                    results.addAll(criteria.getResults());
                }
                if (null != criteria.getResult()) {
                    results.add(criteria.getResult());
                }
                query.append(inClause("result", results));
            }
        }

        List<IspnAction> ispnActions = queryFactory.create(query.toString()).list();
        return prepareActionsPage(ispnActions.stream().map(ispnAction -> {
            if (criteria != null && criteria.isThin()) {
                Action action = new Action(ispnAction.getAction());
                action.setEvent(null);
                return action;
            }
            return ispnAction.getAction();
        }).collect(Collectors.toList()), pager);
    }

    // An exploded "in" clause because the actual one seems not to work
    private String inClause(String field, Set<String> values) {
        String separator = "";
        StringBuffer sb = new StringBuffer(" and (");
        for (String v : values) {
            sb.append(separator);
            sb.append(String.format("%s = '%s'", field, v));
            separator = " or ";
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public int deleteActions(String tenantId, ActionsCriteria criteria) throws Exception {
        if (isEmpty(tenantId)) {
            throw new IllegalArgumentException("TenantId must be not null");
        }

        List<Action> actionsToDelete = getActions(tenantId, criteria, null);
        if (actionsToDelete == null || actionsToDelete.isEmpty()) {
            return 0;
        }

        try {
            if(!fastActionsStore) {
                actionsStore.startBatch();
            }
            actionsToDelete
                    .forEach(a -> actionsStore.remove(IspnPk.pk(a)));
            if(!fastActionsStore) {
               actionsStore.endBatch(true);
            }
            return actionsToDelete.size();

        } catch (Exception e) {
            try {
                if(!fastActionsStore) {
                    actionsStore.endBatch(false);
                }
            } catch (Exception e2) {
                log.errorDatabaseException(e2.getMessage());
            }
            log.errorDatabaseException(e.getMessage());
            throw e;
        }
    }

    @Override
    public void addListener(ActionListener listener) {
        alertsContext.registerActionListener(listener);
    }

    private Page<Action> prepareActionsPage(List<Action> actions, Pager pager) {
        if (pager != null) {
            if (pager.getOrder() != null
                    && !pager.getOrder().isEmpty()
                    && pager.getOrder().get(0).getField() == null) {
                pager = Pager.builder()
                        .withPageSize(pager.getPageSize())
                        .withStartPage(pager.getPageNumber())
                        .orderBy(Field.ALERT_ID.getText(), Order.Direction.DESCENDING).build();
            }
            List<Action> ordered = actions;
            if (pager.getOrder() != null) {
                pager.getOrder().stream().filter(o -> o.getField() != null && o.getDirection() != null)
                        .forEach(o -> {
                            ActionComparator comparator = new ActionComparator(
                                    Field.getField(o.getField()),
                                    o.getDirection());
                            Collections.sort(ordered, comparator);
                        });
            }
            if (!pager.isLimited() || ordered.size() < pager.getStart()) {
                pager = new Pager(0, ordered.size(), pager.getOrder());
                return new Page<>(ordered, pager, ordered.size());
            }
            if (pager.getEnd() >= ordered.size()) {
                return new Page<>(ordered.subList(pager.getStart(), ordered.size()), pager, ordered.size());
            }
            return new Page<>(ordered.subList(pager.getStart(), pager.getEnd()), pager, ordered.size());
        } else {
            pager = Pager.builder().withPageSize(actions.size()).orderBy(Field.ALERT_ID.getText(),
                    Order.Direction.ASCENDING).build();
            return new Page<>(actions, pager, actions.size());
        }
    }

    private void send(final TriggerAction triggerAction, final Event event) {
        if (triggerAction == null || isEmpty(triggerAction.getTenantId()) ||
                isEmpty(triggerAction.getActionPlugin()) || isEmpty(triggerAction.getActionId())) {
            throw new IllegalArgumentException("TriggerAction must be not null");
        }
        if (event == null || isEmpty(event.getTenantId())) {
            throw new IllegalArgumentException("Event must be not null");
        }
        if (!triggerAction.getTenantId().equals(event.getTenantId())) {
            throw new IllegalArgumentException("TriggerAction and Event must have same tenantId");
        }
        Action action = new Action(triggerAction.getTenantId(), triggerAction.getActionPlugin(),
                triggerAction.getActionId(), event);
        try {
            ActionDefinition actionDefinition = definitions.getActionDefinition(triggerAction.getTenantId(),
                    triggerAction.getActionPlugin(), triggerAction.getActionId());
            Map<String, String> defaultProperties = definitions
                    .getDefaultActionPlugin(triggerAction.getActionPlugin());
            if (actionDefinition != null && defaultProperties != null) {
                Map<String, String> mixedProps = mixProperties(actionDefinition.getProperties(),
                        defaultProperties);
                action.setProperties(mixedProps);
            } else {
                log.debugf("Action %s has not an ActionDefinition", action);
            }
            //  If no constraints defined at TriggerAction level, ActionDefinition constraints are used.
            if (isEmpty(triggerAction.getStates()) && triggerAction.getCalendar() == null) {
                triggerAction.setStates(actionDefinition.getStates());
                triggerAction.setCalendar(actionDefinition.getCalendar());
                log.debugf("Using ActionDefinition constraints: %s", actionDefinition);
            }
            if (ActionsValidator.validate(triggerAction, event)) {
                for (ActionListener listener : alertsContext.getActionsListeners()) {
                    listener.process(action);
                }
                insertAction(action);
            }
        } catch (Exception e) {
            log.debug(e.getMessage(), e);
            log.errorCannotUpdateAction(e.getMessage());
        }
    }

    private void send(final ActionDefinition globalActionDefinition, final Event event) {
        if (globalActionDefinition == null || isEmpty(globalActionDefinition.getTenantId()) ||
                isEmpty(globalActionDefinition.getActionPlugin()) || isEmpty(globalActionDefinition.getActionId())) {
            throw new IllegalArgumentException("ActionDefinition must be not null");
        }
        if (event == null || isEmpty(event.getTenantId())) {
            throw new IllegalArgumentException("Event must be not null");
        }
        if (!globalActionDefinition.getTenantId().equals(event.getTenantId())) {
            throw new IllegalArgumentException("ActionDefinition and Event must have same tenantId");
        }

        TriggerAction globalTriggerAction = new TriggerAction(globalActionDefinition.getTenantId(),
                globalActionDefinition.getActionPlugin(), globalActionDefinition.getActionId());
        Action action = new Action(globalTriggerAction.getTenantId(), globalTriggerAction.getActionPlugin(),
                globalTriggerAction.getActionId(), event);
        try {
            Map<String, String> defaultProperties = definitions
                    .getDefaultActionPlugin(globalTriggerAction.getActionPlugin());
            if (defaultProperties != null) {
                Map<String, String> mixedProps = mixProperties(globalActionDefinition.getProperties(),
                        defaultProperties);
                action.setProperties(mixedProps);
            }
            globalTriggerAction.setStates(globalActionDefinition.getStates());
            globalTriggerAction.setCalendar(globalActionDefinition.getCalendar());
            if (ActionsValidator.validate(globalTriggerAction, event)) {
                for (ActionListener listener : alertsContext.getActionsListeners()) {
                    listener.process(action);
                }
                insertAction(action);
            }
        } catch (Exception e) {
            log.debug(e.getMessage(), e);
            log.errorCannotUpdateAction(e.getMessage());
        }
    }

    private Map<String, String> mixProperties(Map<String, String> props, Map<String, String> defProps) {
        Map<String, String> mixed = new HashMap<>();
        if (props != null) {
            mixed.putAll(props);
        }
        if (defProps != null) {
            for (String defKey : defProps.keySet()) {
                mixed.putIfAbsent(defKey, defProps.get(defKey));
            }
        }
        return mixed;
    }

    private void insertAction(Action action) {
        if (action.getResult() == null) {
            action.setResult(WAITING_RESULT);
        }
        if (!actionsStoreDisabled) {
            try {
                if(alertsLifespanInHours < 0) {
                    actionsStore.getAdvancedCache().withFlags(IGNORE_RETURN_VALUES).put(IspnPk.pk(action), new IspnAction(action));
                } else {
                    actionsStore.getAdvancedCache().withFlags(IGNORE_RETURN_VALUES).put(IspnPk.pk(action), new IspnAction(action), alertsLifespanInHours, TimeUnit.HOURS);
                }
            } catch (Exception e) {
                log.errorDatabaseException(e.getMessage());
            }
        }
    }
}
