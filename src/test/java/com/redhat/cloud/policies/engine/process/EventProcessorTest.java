package com.redhat.cloud.policies.engine.process;

import com.redhat.cloud.policies.engine.TestLifecycleManager;
import com.redhat.cloud.policies.engine.db.entities.Policy;
import com.redhat.cloud.policies.engine.db.repositories.PoliciesHistoryRepository;
import com.redhat.cloud.policies.engine.db.repositories.PoliciesRepository;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import javax.inject.Inject;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.redhat.cloud.policies.engine.process.PayloadParser.CHECK_IN_FIELD;
import static com.redhat.cloud.policies.engine.process.PayloadParser.DISPLAY_NAME_FIELD;
import static com.redhat.cloud.policies.engine.process.PayloadParser.INVENTORY_ID_FIELD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
@QuarkusTestResource(TestLifecycleManager.class)
public class EventProcessorTest {

    private static final String ACCOUNT_ID = "account-id";
    private static final String ORG_ID = "org-id";
    private static final String DEFAULT_NAMESPACE = "insights-client";

    @Inject
    EventProcessor eventProcessor;

    @InjectMock
    PoliciesRepository policiesRepository;

    @InjectMock
    PoliciesHistoryRepository policiesHistoryRepository;

    @InjectMock
    NotificationSender notificationSender;

    @Test
    void testNoEnabledPoliciesFound() {
        Event event = buildEvent();

        when(policiesRepository.getEnabledPolicies(eq(event.getOrgId()))).thenReturn(Collections.emptyList());

        eventProcessor.process(event);

        verify(policiesRepository, times(1)).getEnabledPolicies(eq(event.getOrgId()));
        verify(policiesHistoryRepository, never()).create(any(UUID.class), any(Event.class));
        verify(notificationSender, never()).send(any(PoliciesAction.class));
    }

    @Test
    void testEnabledPoliciesFoundAndAllPoliciesFiredAndSent() {
        Event event = buildEvent();

        Policy policy1 = buildPolicy("policy-1", "Policy 1", "facts.arch = 'x86_64'", "email");
        Policy policy2 = buildPolicy("policy-2", "Policy 2", "facts.arch = 'x86_64'", "notification");
        when(policiesRepository.getEnabledPolicies(eq(event.getOrgId()))).thenReturn(List.of(policy1, policy2));

        eventProcessor.process(event);

        verify(policiesRepository, times(1)).getEnabledPolicies(eq(event.getOrgId()));
        // The following verify shows that the policy was fired.
        verify(policiesHistoryRepository, times(1)).create(eq(policy1.id), eq(event));
        // The following verify shows that the policy was fired.
        verify(policiesHistoryRepository, times(1)).create(eq(policy2.id), eq(event));

        ArgumentCaptor<PoliciesAction> argumentCaptor = ArgumentCaptor.forClass(PoliciesAction.class);
        verify(notificationSender, times(1)).send(argumentCaptor.capture());
        PoliciesAction policiesAction = argumentCaptor.getValue();

        assertEquals(event.getAccountId(), policiesAction.getAccountId());
        assertEquals(event.getOrgId(), policiesAction.getOrgId());
        assertNotNull(policiesAction.getTimestamp());
        assertNotNull(policiesAction.getContext().getSystemCheckIn());
        assertEquals(event.getContext().get(INVENTORY_ID_FIELD), policiesAction.getContext().getInventoryId());

        assertEquals(event.getTags(DISPLAY_NAME_FIELD).iterator().next().value, policiesAction.getContext().getDisplayName());
        assertTrue(event.getTags(DISPLAY_NAME_FIELD).iterator().next().isSynthetic);

        for (Map.Entry<String, Set<Event.TagContent>> expectedTags : event.getTags().entrySet()) {
            Set<String> actualTags = policiesAction.getContext().getTags().get(expectedTags.getKey());
            if (actualTags == null) {
                fail("Tag key " + expectedTags.getKey() + " was not found");
            } else {
                assertTrue(actualTags.containsAll(expectedTags.getValue().stream().map(c -> c.value).collect(Collectors.toList())), "Tag values didn't match for key " + expectedTags.getKey());
            }
        }

        assertPolicyIncludedInPoliciesAction(policy1, policiesAction);
        assertPolicyIncludedInPoliciesAction(policy2, policiesAction);
    }

    @Test
    void testEnabledPoliciesFoundAndOnePolicyFiredButNotSent() {
        Event event = buildEvent();

        // This policy won't be fired because its condition won't be satisfied by the event.
        Policy policy1 = buildPolicy("policy-1", "Policy 1", "facts.arch = 'x86_32'", "notification");
        // This policy will be fired but won't be included into the policies action because it has no actions.
        Policy policy2 = buildPolicy("policy-2", "Policy 2", "facts.arch = 'x86_64'", null);
        when(policiesRepository.getEnabledPolicies(eq(event.getOrgId()))).thenReturn(List.of(policy1, policy2));

        eventProcessor.process(event);

        verify(policiesRepository, times(1)).getEnabledPolicies(eq(event.getOrgId()));
        // The following verify shows that the policy was NOT fired.
        verify(policiesHistoryRepository, never()).create(eq(policy1.id), eq(event));
        // The following verify shows that the policy was fired.
        verify(policiesHistoryRepository, times(1)).create(eq(policy2.id), eq(event));
        verify(notificationSender, never()).send(any(PoliciesAction.class));
    }

    private static Event buildEvent() {
        Event event = new Event();
        event.setAccountId(ACCOUNT_ID);
        event.setOrgId(ORG_ID);
        event.getContext().put(CHECK_IN_FIELD, OffsetDateTime.now().toString());
        event.getContext().put(INVENTORY_ID_FIELD, "inventory-id");
        event.setFacts(Map.of("arch", "x86_64"));
        event.addTag("policies", DISPLAY_NAME_FIELD, "display-name", true);
        event.addTag(DEFAULT_NAMESPACE, "Contact", "spam@redhat.com");
        event.addTag(DEFAULT_NAMESPACE, "Location", "Neuchatel");
        event.addTag(DEFAULT_NAMESPACE, "Location", "Charmey");
        return event;
    }

    private static Policy buildPolicy(String name, String description, String condition, String actions) {
        Policy policy = new Policy();
        policy.id = UUID.randomUUID();
        policy.accountId = ACCOUNT_ID;
        policy.orgId = ORG_ID;
        policy.name = name;
        policy.description = description;
        policy.condition = condition;
        policy.actions = actions;
        return policy;
    }

    private static void assertPolicyIncludedInPoliciesAction(Policy policy, PoliciesAction policiesAction) {
        assertTrue(policiesAction.getEvents().stream().anyMatch(event -> {
            PoliciesAction.Payload payload = event.getPayload();
            return policy.id.toString().equals(payload.getPolicyId()) &&
                    policy.name.equals(payload.getPolicyName()) &&
                    policy.description.equals(payload.getPolicyDescription()) &&
                    policy.condition.equals(payload.getPolicyCondition());
        }));
    }
}
