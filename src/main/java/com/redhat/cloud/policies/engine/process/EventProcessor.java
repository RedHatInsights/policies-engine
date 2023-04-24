package com.redhat.cloud.policies.engine.process;

import com.redhat.cloud.policies.engine.condition.ConditionParser;
import com.redhat.cloud.policies.engine.config.FeatureFlipper;
import com.redhat.cloud.policies.engine.db.repositories.PoliciesRepository;
import com.redhat.cloud.policies.engine.db.entities.Policy;
import com.redhat.cloud.policies.engine.db.repositories.PoliciesHistoryRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.quarkus.logging.Log;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
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

    @Inject
    FeatureFlipper featureFlipper;

    Counter firedPoliciesCounter;

    @ConfigProperty(name = "env.base.url", defaultValue = "/")
    String baseUrl;

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
            List<Policy> policiesWithNotifications = getFiredPoliciesWithNotificationAction(event, enabledPolicies);

            /*
             * If the policies action contains at least one event, then it means at least one policy was fired and that
             * policy has a notification action. We can send the policies action to the notifications app.
             */
            if (policiesWithNotifications.size() > 0) {
                if (featureFlipper.isNotificationsAsCloudEvents()) {
                    sendCloudEvent(event, policiesWithNotifications);
                } else {
                    sendOldNotification(event, policiesWithNotifications);
                }
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

    private List<Policy> getFiredPoliciesWithNotificationAction(Event event, List<Policy> enabledPolicies) {
        List<Policy> policiesWithNotification = new ArrayList<>();

        int firedPolicies = 0;
        for (Policy policy : enabledPolicies) {
            if (isFired(policy, event)) {
                firedPolicies++;
                policiesHistoryRepository.create(policy.id, event);
                if (hasNotificationAction(policy)) {
                    policiesWithNotification.add(policy);
                }
            }
        }

        firedPoliciesCounter.increment(firedPolicies);
        Log.debugf("%d policies fired from the event", firedPolicies);

        return policiesWithNotification;
    }

    private void sendCloudEvent(Event event, List<Policy> policies) {
        String inventoryId = event.getContext().get(INVENTORY_ID_FIELD);
        LocalDateTime systemCheckin = LocalDateTime.from(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(event.getContext().get(CHECK_IN_FIELD)));

        PolicyTriggeredCloudEvent.Builder builder = PolicyTriggeredCloudEvent.builder()
                // Event fields
                .setId(UUID.randomUUID())
                .setOrgId(event.getOrgId())
                .setAccount(event.getAccountId())
                .setTime(LocalDateTime.now(UTC))

                // -- System fields
                .setSystemInventoryId(inventoryId)
                .setSystemDisplayName(event.getTags(DISPLAY_NAME_FIELD).iterator().next().value)
                .setSystemCheckinTime(systemCheckin);

        event.getTags().forEach((key, value) -> value.forEach(tagContent -> {
            // Skip the synthetic tags for cloud events - these are not real tags.
            if (!tagContent.isSynthetic) {
                builder.addSystemTag(tagContent.namespace, key, tagContent.value);
            }
        }));

        policies.forEach(p -> builder.addPolicy(
                p.id, p.name, p.description, p.condition, baseUrl + "/insights/policies/policy/" + p.id
        ));

        notificationSender.send(builder.build());
    }

    private void sendOldNotification(Event event, List<Policy> policies) {
        PoliciesAction policiesAction = new PoliciesAction();
        policiesAction.setOrgId(event.getOrgId());
        policiesAction.setAccountId(event.getAccountId());
        policiesAction.setTimestamp(LocalDateTime.now(UTC));

        PoliciesAction.Context context = policiesAction.getContext();
        context.setSystemCheckIn(LocalDateTime.from(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(event.getContext().get(CHECK_IN_FIELD))));
        context.setInventoryId(event.getContext().get(INVENTORY_ID_FIELD));
        context.setDisplayName(event.getTags(DISPLAY_NAME_FIELD).iterator().next().value);

        for (Map.Entry<String, Set<Event.TagContent>> tagEntry : event.getTags().entrySet()) {
            for (Event.TagContent tagValue : tagEntry.getValue()) {
                String value = tagValue.value;
                if (value == null) {
                    // Same behavior as previously with JsonObjectNoNullSerializer with old hooks
                    value = "";
                }
                context.getTags().computeIfAbsent(tagEntry.getKey(), unused -> new HashSet<>()).add(value);
            }
        }

        for (Policy policy : policies) {
            // Each fired policy is added to the policies action as an event.
            PoliciesAction.Event policiesEvent = new PoliciesAction.Event();
            policiesEvent.getPayload().setPolicyCondition(policy.condition);
            policiesEvent.getPayload().setPolicyId(policy.id.toString());
            policiesEvent.getPayload().setPolicyName(policy.name);
            policiesEvent.getPayload().setPolicyDescription(policy.description);
            policiesAction.getEvents().add(policiesEvent);
        }

        notificationSender.send(policiesAction);
    }
}
