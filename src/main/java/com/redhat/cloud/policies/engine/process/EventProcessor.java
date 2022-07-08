package com.redhat.cloud.policies.engine.process;

import com.redhat.cloud.policies.engine.condition.ConditionParser;
import com.redhat.cloud.policies.engine.db.repositories.PoliciesRepository;
import com.redhat.cloud.policies.engine.db.entities.Policy;
import com.redhat.cloud.policies.engine.db.repositories.PoliciesHistoryRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.quarkus.logging.Log;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static com.redhat.cloud.policies.engine.process.PayloadParser.CHECK_IN_FIELD;
import static com.redhat.cloud.policies.engine.process.PayloadParser.DISPLAY_NAME_FIELD;
import static com.redhat.cloud.policies.engine.process.PayloadParser.INVENTORY_ID_FIELD;
import static java.time.ZoneOffset.UTC;

@ApplicationScoped
public class EventProcessor {

    private static final Pattern ACTIONS_SPLIT_PATTERN = Pattern.compile(";");
    private static final List<String> NOTIFICATIONS_ACTIONS = List.of("email", "notification");

    @Inject
    PoliciesRepository policiesRepository;

    @Inject
    PoliciesHistoryRepository policiesHistoryRepository;

    @Inject
    NotificationSender notificationSender;

    @Inject
    MeterRegistry meterRegistry;

    Counter firedPoliciesCounter;

    @PostConstruct
    void postConstruct() {
        firedPoliciesCounter = meterRegistry.counter("engine.actions.notifications.processed");
    }

    /**
     * Processes an {@link Event}, evaluating conditions of all policies owned by the orgId of the event. If the
     * condition of at least one policy is satisfied, then a Kafka message is sent to the notifications topic. If the
     * conditions of multiple policies are satisfied, one Kafka message is sent and it contains one event for each
     * policy that was fired. In that case, tags from each fired policy are also merged into a single collection and
     * added to the Kafka payload. Each fired policy also results in a new record in the policies history DB table.
     *
     * @param event the event
     */
    public void process(Event event) {
        Log.debugf("Processing %s", event);

        List<Policy> enabledPolicies = policiesRepository.getEnabledPolicies(event.getOrgId());
        if (enabledPolicies.isEmpty()) {
            Log.debugf("No enabled policies found for orgId %s", event.getOrgId());
        } else {
            Log.debugf("Found %d enabled policies for orgId %s", enabledPolicies.size(), event.getOrgId());

            PoliciesAction policiesAction = new PoliciesAction();
            policiesAction.setOrgId(event.getOrgId());
            policiesAction.setAccountId(event.getAccountId());
            policiesAction.setTimestamp(LocalDateTime.now(UTC));

            PoliciesAction.Context context = policiesAction.getContext();
            context.setSystemCheckIn(LocalDateTime.from(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(event.getContext().get(CHECK_IN_FIELD))));
            context.setInventoryId(event.getContext().get(INVENTORY_ID_FIELD));
            context.setDisplayName(event.getTags(DISPLAY_NAME_FIELD).iterator().next());

            for (Map.Entry<String, Set<String>> tagEntry : event.getTags().entrySet()) {
                for (String tagValue : tagEntry.getValue()) {
                    if (tagValue == null) {
                        // Same behavior as previously with JsonObjectNoNullSerializer with old hooks
                        tagValue = "";
                    }
                    context.getTags().computeIfAbsent(tagEntry.getKey(), unused -> new HashSet<>()).add(tagValue);
                }
            }

            int firedPolicies = 0;
            for (Policy policy : enabledPolicies) {
                if (isFired(policy, event)) {
                    firedPolicies++;
                    policiesHistoryRepository.create(policy.id, event);
                    if (hasNotificationAction(policy)) {
                        // Each fired policy is added to the policies action as an event.
                        PoliciesAction.Event policiesEvent = new PoliciesAction.Event();
                        policiesEvent.getPayload().setPolicyCondition(policy.condition);
                        policiesEvent.getPayload().setPolicyId(policy.id.toString());
                        policiesEvent.getPayload().setPolicyName(policy.name);
                        policiesEvent.getPayload().setPolicyDescription(policy.description);
                        policiesAction.getEvents().add(policiesEvent);
                    }
                }
            }
            firedPoliciesCounter.increment(firedPolicies);
            Log.debugf("%d policies fired from the event", firedPolicies);

            /*
             * If the policies action contains at least one event, then it means at least one policy was fired and that
             * policy has a notification action. We can send the policies action to the notifications app.
             */
            if (!policiesAction.getEvents().isEmpty()) {
                notificationSender.send(policiesAction);
            }
        }
    }

    private static boolean isFired(Policy policy, Event event) {
        String condition = policy.condition;
        boolean result = condition == null || condition.isBlank() || ConditionParser.evaluate(event, condition);
        Log.debugf("Condition [%s] was %s", condition, result ? "satisfied" : " not satisfied");
        return result;
    }

    private static boolean hasNotificationAction(Policy policy) {
        if (policy.actions != null) {
            for (String action : ACTIONS_SPLIT_PATTERN.split(policy.actions)) {
                if (NOTIFICATIONS_ACTIONS.contains(action.trim())) {
                    return true;
                }
            }
        }
        return false;
    }
}
