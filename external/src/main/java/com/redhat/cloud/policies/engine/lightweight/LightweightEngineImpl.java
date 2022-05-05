package com.redhat.cloud.policies.engine.lightweight;

import com.redhat.cloud.policies.api.model.condition.expression.ExprParser;
import com.redhat.cloud.policies.engine.actions.plugins.notification.PoliciesAction;
import io.quarkus.runtime.StartupEvent;
import org.eclipse.microprofile.metrics.Counter;
import org.eclipse.microprofile.metrics.annotation.Metric;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.OnOverflow;
import org.hawkular.alerts.api.model.condition.Condition;
import org.hawkular.alerts.api.model.condition.ConditionEval;
import org.hawkular.alerts.api.model.condition.EventCondition;
import org.hawkular.alerts.api.model.condition.EventConditionEval;
import org.hawkular.alerts.api.model.dampening.Dampening;
import org.hawkular.alerts.api.model.event.Alert;
import org.hawkular.alerts.api.model.event.Event;
import org.hawkular.alerts.api.model.trigger.FullTrigger;
import org.hawkular.alerts.api.model.trigger.Trigger;
import org.hawkular.alerts.api.model.trigger.TriggerAction;
import org.hawkular.alerts.api.services.LightweightEngine;
import org.hawkular.alerts.api.services.PoliciesHistoryService;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.redhat.cloud.policies.engine.actions.plugins.NotificationActionPluginListener.WEBHOOK_CHANNEL;
import static com.redhat.cloud.policies.engine.actions.plugins.NotificationActionPluginListener.buildMessageWithId;
import static com.redhat.cloud.policies.engine.actions.plugins.NotificationActionPluginListener.serializeAction;
import static java.time.ZoneOffset.UTC;
import static org.eclipse.microprofile.reactive.messaging.OnOverflow.Strategy.UNBOUNDED_BUFFER;

@ApplicationScoped
public class LightweightEngineImpl implements LightweightEngine {

    private static final Logger LOGGER = Logger.getLogger(LightweightEngineImpl.class);

    // The key of the outer map is the accountId. The key of the inner map is the triggerId.
    // TODO POL-649 Change the inner map key type to UUID when we're done migrating to the lightweight engine.
    // TODO POL-649 Introduce entries expiration.
    private final Map<String, Map<String, FullTrigger>> triggersByTenant = new ConcurrentHashMap<>();

    @Inject
    /*
     * This channel is already used in NotificationActionPluginListener. Because of that, the
     * mp.messaging.outgoing.webhook.merge config key is required in applications.properties.
     */
    @Channel(WEBHOOK_CHANNEL)
    @OnOverflow(UNBOUNDED_BUFFER)
    Emitter<String> emitter;

    @Inject
    PoliciesHistoryService policiesHistoryService;

    @Inject
    LightweightEngineConfig lightweightEngineConfig;

    @Inject
    TriggerRepository triggerRepository;

    @Inject
    @Metric(absolute = true, name = "engine.actions.notifications.processed")
    Counter alertsCounter;

    @Inject
    @Metric(absolute = true, name = "engine.actions.notifications.aggregated")
    Counter notificationsCounter;

    public void runAtStartup(@Observes StartupEvent event) {
        init();
    }

    @Override
    public void init() {
        if (lightweightEngineConfig.isRestApiEnabled()) {
            LOGGER.debug("Initializing the in-memory triggers cache");
            for (FullTrigger fullTrigger : triggerRepository.findAll()) {
                triggersByTenant.computeIfAbsent(
                        fullTrigger.getTrigger().getTenantId(),
                        unused -> new ConcurrentHashMap<>()
                ).put(fullTrigger.getTrigger().getId(), fullTrigger);
            }
            LOGGER.debug("Initialization done");
        } else {
            LOGGER.debug("Ignoring init call because the lightweight engine REST API is disabled");
        }
    }

    @Override
    public void validateCondition(String condition) {
        if (lightweightEngineConfig.isRestApiEnabled()) {
            try {
                ExprParser.validate(condition);
            } catch (Exception e) {
                LOGGER.debugf(e, "Validation failed for condition %s", condition);
                throw new BadRequestException(e.getMessage());
            }
        } else {
            LOGGER.debug("Ignoring validateCondition call because the lightweight engine REST API is disabled");
        }
    }

    @Override
    public void reloadTriggers(String accountId, Set<UUID> triggerIds) {
        if (lightweightEngineConfig.isRestApiEnabled()) {
            Map<String, FullTrigger> inMemoryTriggers = triggersByTenant.getOrDefault(accountId, Collections.emptyMap());
            Map<UUID, FullTrigger> dbTriggers = triggerRepository.findByAccountAndIds(accountId, triggerIds);
            for (UUID triggerId : triggerIds) {
                if (inMemoryTriggers.containsKey(triggerId) && !dbTriggers.containsKey(triggerId)) {
                    // The trigger has been removed from the DB, it is also removed from the in-memory collection.
                    inMemoryTriggers.remove(triggerId);
                } else if (dbTriggers.containsKey(triggerId)) {
                    // The trigger is loaded or reloaded.
                    inMemoryTriggers.put(triggerId.toString(), dbTriggers.get(triggerId));
                }
            }
        } else {
            LOGGER.debug("Ignoring reloadTriggers call because the lightweight engine REST API is disabled");
        }
    }

    @Override
    public void loadTrigger(FullTrigger fullTrigger) {
        if (!lightweightEngineConfig.isRestApiEnabled()) {
            LOGGER.debugf("Loading trigger %s", fullTrigger);
            provideDefaultDampening(fullTrigger);
            String tenantId = fullTrigger.getTrigger().getTenantId();
            triggersByTenant.computeIfAbsent(tenantId, unused -> new ConcurrentHashMap<>()).put(fullTrigger.getTrigger().getId(), fullTrigger);
        } else {
            LOGGER.debug("Ignoring loadTrigger call because the lightweight engine REST API is enabled");
        }
    }

    @Override
    public void reloadTrigger(Trigger trigger, Collection<Condition> conditions) {
        if (!lightweightEngineConfig.isRestApiEnabled()) {
            LOGGER.debugf("Reloading trigger %s with conditions %s", trigger, conditions);
            boolean found = false;
            Map<String, FullTrigger> tenantTriggers = triggersByTenant.get(trigger.getTenantId());
            if (tenantTriggers != null) {
                FullTrigger fullTrigger = tenantTriggers.get(trigger.getId());
                if (fullTrigger != null) {
                    fullTrigger.setTrigger(trigger);
                    fullTrigger.setConditions(new ArrayList<>(conditions));
                    found = true;
                    LOGGER.debugf("Trigger reloaded");
                }
            }
            if (!found) {
                LOGGER.debugf("Trigger to reload not found, forwarding the call to the loadTrigger method");
                FullTrigger fullTrigger = new FullTrigger();
                fullTrigger.setTrigger(trigger);
                fullTrigger.setConditions(new ArrayList<>(conditions));
                loadTrigger(fullTrigger);
            }
        } else {
            LOGGER.debug("Ignoring reloadTrigger call because the lightweight engine REST API is enabled");
        }
    }

    @Override
    public void removeTrigger(String tenantId, String triggerId) {
        if (!lightweightEngineConfig.isRestApiEnabled()) {
            LOGGER.debugf("Removing trigger %s/%s", tenantId, triggerId);
            boolean found = false;
            Map<String, FullTrigger> tenantTriggers = triggersByTenant.get(tenantId);
            if (tenantTriggers != null) {
                found = tenantTriggers.remove(triggerId) != null;
                if (found) {
                    LOGGER.debugf("Trigger removed");
                }
            }
            if (!found) {
                LOGGER.debugf("Trigger to remove not found");
            }
        } else {
            LOGGER.debug("Ignoring removeTrigger call because the lightweight engine REST API is enabled");
        }
    }

    @Override
    public void process(Event event) {
        LOGGER.debugf("Processing event %s", event);
        Set<Alert> alerts = fireTriggers(event);
        if (alerts.isEmpty()) {
            LOGGER.debug("No alerts created from the event");
        } else {
            LOGGER.debugf("%d alerts created from the event", alerts.size());

            // In the old implementation, the time comes from the Action constructor which relies on System.currentTimeMillis().
            LocalDateTime nowUTC = LocalDateTime.ofInstant(Instant.ofEpochMilli(System.currentTimeMillis()), UTC);

            PoliciesAction policiesAction = new PoliciesAction();
            policiesAction.setAccountId(event.getTenantId());
            policiesAction.setTimestamp(nowUTC);

            PoliciesAction.Context context = policiesAction.getContext();

            for (Alert alert : alerts) {
                alertsCounter.inc();

                // Tags from all alerts are merged into the policies action context tags.
                for (Map.Entry<String, String> tagEntry : alert.getTags().entries()) {
                    String tagValue = tagEntry.getValue();
                    if (tagValue == null) {
                        // Same behavior as previously with JsonObjectNoNullSerializer with old hooks
                        tagValue = "";
                    }
                    context.getTags().computeIfAbsent(tagEntry.getKey(), unused -> new HashSet<>()).add(tagValue);
                }

                // Each alert is added to the policies action as an event.
                for (Set<ConditionEval> evalSet : alert.getEvalSets()) {
                    for (ConditionEval conditionEval : evalSet) {
                        if (conditionEval instanceof EventConditionEval) {
                            EventConditionEval eventEval = (EventConditionEval) conditionEval;

                            if (context.getSystemCheckIn() == null) {
                                context.setSystemCheckIn(LocalDateTime.from(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(eventEval.getContext().get("check_in"))));
                            }
                            if (context.getInventoryId() == null) {
                                context.setInventoryId(eventEval.getContext().get("inventory_id"));
                            }
                            if (context.getDisplayName() == null) {
                                context.setDisplayName(eventEval.getValue().getTags().get("display_name").iterator().next());
                            }

                            PoliciesAction.Event policiesEvent = new PoliciesAction.Event();
                            policiesEvent.getPayload().setPolicyCondition(eventEval.getCondition().getExpression());
                            policiesEvent.getPayload().setPolicyId(alert.getTrigger().getId());
                            policiesEvent.getPayload().setPolicyName(alert.getTrigger().getName());
                            policiesEvent.getPayload().setPolicyDescription(alert.getTrigger().getDescription());

                            policiesAction.getEvents().add(policiesEvent);
                            break;
                        } else {
                            LOGGER.warnf("Unsupported condition eval type %s", conditionEval);
                        }
                    }
                }
            }

            try {
                String payload = serializeAction(policiesAction);
                LOGGER.debugf("Sending Kafka payload %s", payload);
                emitter.send(buildMessageWithId(payload));
                notificationsCounter.inc();
            } catch (IOException e) {
                LOGGER.warnf("Failed to serialize action for accountId", policiesAction.getAccountId());
            }
        }
    }

    /*
     * Java equivalent of the old ProvideDefaultDampening rule.
     * See the ConditionMatch.drl file for more details about that rule.
     */
    private void provideDefaultDampening(FullTrigger fullTrigger) {
        if (fullTrigger.getDampenings().isEmpty()) {
            Trigger trigger = fullTrigger.getTrigger();
            LOGGER.debugf("Adding default %s dampening for trigger! %s", trigger.getMode(), trigger.getId());
            Dampening dampening = Dampening.forStrict(trigger.getTenantId(), trigger.getId(), trigger.getMode(), 1 );
            fullTrigger.getDampenings().add(dampening);
        } else if (fullTrigger.getDampenings().size() > 1) {
            // This should never be logged.
            LOGGER.warnf("Trigger with more than one dampening found: %s", fullTrigger);
        }
    }

    /*
     * Returns alerts created from the fired triggers.
     * This is kind of the Java equivalent of the old AlertOnSatisfiedDampening rule.
     * See the ConditionMatch.drl file for more details about that rule.
     */
    private Set<Alert> fireTriggers(Event event) {
        Map<String, FullTrigger> tenantTriggers = triggersByTenant.get(event.getTenantId());
        if (tenantTriggers == null) {
            LOGGER.debugf("No triggers found for tenant %s", event.getTenantId());
            return Collections.emptySet();
        } else {
            LOGGER.debugf("Found %d triggers for tenant %s", tenantTriggers.size(), event.getTenantId());
            Set<Alert> alerts = new HashSet<>();
            for (FullTrigger fullTrigger : tenantTriggers.values()) {
                if (fullTrigger.getTrigger().isEnabled()) {
                    LOGGER.debugf("Starting the trigger conditions evaluation");
                    Set<ConditionEval> conditionEvals = new HashSet<>();
                    for (Condition condition : fullTrigger.getConditions()) {
                        switch (condition.getType()) {
                            case EVENT:
                                evaluateCondition(fullTrigger.getTrigger(), (EventCondition) condition, event).ifPresent(conditionEvals::add);
                                break;
                            default:
                                LOGGER.warnf("Unsupported condition type %s", condition);
                                break;
                        }
                    }
                    // The trigger should always have exactly one dampening.
                    Dampening dampening = fullTrigger.getDampenings().get(0);
                    performDampening(dampening, fullTrigger.getTrigger(), conditionEvals);
                    if (dampening.isSatisfied()) {
                        LOGGER.debugf("Dampening satisfied");
                        Alert alert = new Alert(event.getTenantId(), fullTrigger.getTrigger(), dampening, dampening.getSatisfyingEvals());
                        policiesHistoryService.put(alert);
                        if (shouldSendNotification(fullTrigger)) {
                            alerts.add(alert);
                        }
                    } else {
                        LOGGER.debugf("Dampening not satisfied");
                    }
                    dampening.reset();
                }
            }
            return alerts;
        }
    }

    private boolean shouldSendNotification(FullTrigger fullTrigger) {
        if (lightweightEngineConfig.isRestApiEnabled()) {
            return true;
        } else {
            if (fullTrigger.getTrigger().getActions() != null) {
                for (TriggerAction triggerAction : fullTrigger.getTrigger().getActions()) {
                    if (triggerAction.getActionPlugin().equals("email") || triggerAction.getActionPlugin().equals("notification")) {
                        return true;
                    }
                }
            }
            LOGGER.debug("Notification trigger action not found in trigger definition");
            return false;
        }
    }

    /*
     * Java equivalent of the old Event rule.
     * See the ConditionMatch.drl file for more details about that rule.
     */
    private Optional<EventConditionEval> evaluateCondition(Trigger trigger, EventCondition eventCondition, Event event) {
        if (lightweightEngineConfig.isRestApiEnabled()) {
            EventConditionEval eval = new EventConditionEval(eventCondition, event);
            LOGGER.debugf("Event Eval: %s %s", eval.isMatch() ? "Match!" : "no match", eval.getDisplayString());
            return Optional.of(eval);
        } else {
            if (trigger.getMode() == eventCondition.getTriggerMode() && trigger.getSource().equals(event.getDataSource())
                    && eventCondition.getDataId().equals(event.getDataId())) {
                EventConditionEval eval = new EventConditionEval(eventCondition, event);
                LOGGER.debugf("Event Eval: %s %s", eval.isMatch() ? "Match!" : "no match", eval.getDisplayString());
                return Optional.of(eval);
            } else {
                return Optional.empty();
            }
        }
    }

    /*
     * Java equivalent of the old DampenTrigger rule.
     * See the ConditionMatch.drl file for more details about that rule.
     */
    private void performDampening(Dampening dampening, Trigger trigger, Set<ConditionEval> conditionEvals) {
        LOGGER.debugf("Performing dampening of trigger %s", trigger);
        dampening.perform(trigger.getMatch(), conditionEvals);
    }
}
