package com.redhat.cloud.policies.engine;

import com.redhat.cloud.policies.engine.actions.plugins.notification.PoliciesAction;
import org.eclipse.microprofile.config.ConfigProvider;
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
import org.hawkular.alerts.api.services.LightweightEngine;
import org.hawkular.alerts.api.services.PoliciesHistoryService;
import org.jboss.logging.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
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
import java.util.concurrent.ConcurrentHashMap;

import static com.redhat.cloud.policies.engine.actions.plugins.NotificationActionPluginListener.WEBHOOK_CHANNEL;
import static com.redhat.cloud.policies.engine.actions.plugins.NotificationActionPluginListener.buildMessageWithId;
import static com.redhat.cloud.policies.engine.actions.plugins.NotificationActionPluginListener.serializeAction;
import static java.time.ZoneOffset.UTC;
import static org.eclipse.microprofile.reactive.messaging.OnOverflow.Strategy.UNBOUNDED_BUFFER;

@ApplicationScoped
public class LightweightEngineImpl implements LightweightEngine {

    public static final String LIGHTWEIGHT_ENGINE_CONFIG_KEY = "lightweight-engine.enabled";

    private static final Logger LOGGER = Logger.getLogger(LightweightEngineImpl.class);

    // The key of the outer map is the tenantId. The key of the inner map is the triggerId.
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
    @Metric(absolute = true, name = "engine.actions.notifications.processed")
    Counter alertsCounter;

    @Inject
    @Metric(absolute = true, name = "engine.actions.notifications.aggregated")
    Counter notificationsCounter;

    @PostConstruct
    void postConstruct() {
        boolean enabled = ConfigProvider.getConfig().getOptionalValue(LIGHTWEIGHT_ENGINE_CONFIG_KEY, Boolean.class).orElse(false);
        LOGGER.infof("Lightweight engine is %s", enabled ? "enabled" : "disabled");
    }

    @Override
    public void loadTrigger(FullTrigger fullTrigger) {
        LOGGER.debugf("Loading trigger %s", fullTrigger);
        provideDefaultDampening(fullTrigger);
        String tenantId = fullTrigger.getTrigger().getTenantId();
        triggersByTenant.computeIfAbsent(tenantId, unused -> new ConcurrentHashMap<>()).put(fullTrigger.getTrigger().getId(), fullTrigger);
    }

    @Override
    public void reloadTrigger(Trigger trigger, Collection<Condition> conditions) {
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
    }

    @Override
    public void removeTrigger(String tenantId, String triggerId) {
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
                    alerts.add(alert);
                } else {
                    LOGGER.debugf("Dampening not satisfied");
                }
                dampening.reset();
            }
            return alerts;
        }
    }

    /*
     * Java equivalent of the old Event rule.
     * See the ConditionMatch.drl file for more details about that rule.
     */
    private Optional<EventConditionEval> evaluateCondition(Trigger trigger, EventCondition eventCondition, Event event) {
        if (trigger.getMode() == eventCondition.getTriggerMode() && trigger.getSource().equals(event.getDataSource())
                && eventCondition.getDataId().equals(event.getDataId())) {
            EventConditionEval eval = new EventConditionEval(eventCondition, event);
            LOGGER.debugf("Event Eval: %s %s", eval.isMatch() ? "Match!" : "no match", eval.getDisplayString());
            return Optional.of(eval);
        } else {
            return Optional.empty();
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
