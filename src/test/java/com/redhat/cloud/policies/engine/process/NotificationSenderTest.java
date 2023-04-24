package com.redhat.cloud.policies.engine.process;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.cloud.event.apps.policies.v1.Policy;
import com.redhat.cloud.event.apps.policies.v1.PolicyTriggered;
import com.redhat.cloud.event.apps.policies.v1.SystemClass;
import com.redhat.cloud.event.parser.ConsoleCloudEvent;
import com.redhat.cloud.event.parser.ConsoleCloudEventParser;
import com.redhat.cloud.event.parser.GenericConsoleCloudEvent;
import com.redhat.cloud.notifications.ingress.Action;
import com.redhat.cloud.notifications.ingress.Parser;
import com.redhat.cloud.notifications.ingress.Payload;
import com.redhat.cloud.policies.engine.TestLifecycleManager;
import com.redhat.cloud.policies.engine.config.FeatureFlipper;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.reactive.messaging.kafka.api.KafkaMessageMetadata;
import io.smallrye.reactive.messaging.providers.connectors.InMemoryConnector;
import io.smallrye.reactive.messaging.providers.connectors.InMemorySink;
import org.apache.kafka.common.header.Header;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Any;
import javax.inject.Inject;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static com.redhat.cloud.policies.engine.process.NotificationSender.APP_NAME;
import static com.redhat.cloud.policies.engine.process.NotificationSender.BUNDLE_NAME;
import static com.redhat.cloud.policies.engine.process.NotificationSender.EVENT_TYPE_NAME;
import static com.redhat.cloud.policies.engine.process.NotificationSender.MESSAGE_ID_HEADER;
import static com.redhat.cloud.policies.engine.process.NotificationSender.WEBHOOK_CHANNEL;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@QuarkusTest
@QuarkusTestResource(TestLifecycleManager.class)
public class NotificationSenderTest {

    @Inject
    NotificationSender notificationSender;

    @Inject
    @Any
    InMemoryConnector inMemoryConnector;

    InMemorySink<String> webhookChannel;

    @Inject
    ObjectMapper objectMapper;

    @Inject
    FeatureFlipper featureFlipper;

    @PostConstruct
    void postConstruct() {
        webhookChannel = inMemoryConnector.sink(WEBHOOK_CHANNEL);
    }

    @BeforeEach
    void resetWebhookChannel() {
        webhookChannel.clear();
    }

    @Test
    void testSendAction() {
        PoliciesAction policiesAction = buildPoliciesAction();
        notificationSender.send(policiesAction);
        await().until(() -> webhookChannel.received().size() == 1);
        Message<String> message = webhookChannel.received().get(0);
        checkMessageIdHeader(message);
        Action action = Parser.decode(message.getPayload());

        assertEquals(BUNDLE_NAME, action.getBundle());
        assertEquals(APP_NAME, action.getApplication());
        assertEquals(EVENT_TYPE_NAME, action.getEventType());
        assertEquals(policiesAction.getAccountId(), action.getAccountId());
        assertEquals(policiesAction.getOrgId(), action.getOrgId());
        assertEquals(policiesAction.getTimestamp(), action.getTimestamp());
        assertEquals(policiesAction.getContext().getInventoryId(), action.getContext().getAdditionalProperties().get("inventory_id"));
        assertEquals(policiesAction.getContext().getDisplayName(), action.getContext().getAdditionalProperties().get("display_name"));

        // The system_check_in field precision is different before and after the action serialization.
        LocalDateTime expectedSystemCheckIn = policiesAction.getContext().getSystemCheckIn();
        LocalDateTime actualSystemCheckIn = LocalDateTime.parse((String) action.getContext().getAdditionalProperties().get("system_check_in"), ISO_DATE_TIME);
        assertEquals(0, SECONDS.between(expectedSystemCheckIn, actualSystemCheckIn));

        // Actual tags: [{value=world, key=hello}, {value=intended, key=tags}, {value=as, key=tags}, {value=working, key=tags}, {value=are, key=tags}]
        JsonNode actualTags = objectMapper.valueToTree(action.getContext().getAdditionalProperties().get("tags"));
        for (Map.Entry<String, Set<String>> tagsEntry : policiesAction.getContext().getTags().entrySet()) {
            next:
            for (String tagValue : tagsEntry.getValue()) {
                for (JsonNode actualTag : actualTags) {
                    if (actualTag.get("key").asText().equals(tagsEntry.getKey()) && actualTag.get("value").asText().equals(tagValue)) {
                        System.out.println("Tag [key=" + tagsEntry.getKey() + ", value=" + tagValue + "] was found");
                        continue next;
                    }
                }
                fail("Tag [key=" + tagsEntry.getKey() + ", value=" + tagValue + "] was not found");
            }
        }

        for (PoliciesAction.Event policiesEvent : policiesAction.getEvents()) {
            PoliciesAction.Payload expectedPayload = policiesEvent.getPayload();
            assertTrue(action.getEvents().stream().anyMatch(event -> {
                Payload actualPayload = event.getPayload();
                return expectedPayload.getPolicyId().equals(actualPayload.getAdditionalProperties().get("policy_id")) &&
                        expectedPayload.getPolicyName().equals(actualPayload.getAdditionalProperties().get("policy_name")) &&
                        expectedPayload.getPolicyDescription().equals(actualPayload.getAdditionalProperties().get("policy_description")) &&
                        expectedPayload.getPolicyCondition().equals(actualPayload.getAdditionalProperties().get("policy_condition"));
            }), "Event not found in the Kafka message");
        }
    }

    @Test
    void testSendCloudEvent() {
        try {
            featureFlipper.setNotificationsAsCloudEvents(true);
            PolicyTriggeredCloudEvent buildCloudEvent = buildPoliciesCloudEvent();
            notificationSender.send(buildCloudEvent);
            await().until(() -> webhookChannel.received().size() == 1);
            Message<String> message = webhookChannel.received().get(0);

            ConsoleCloudEventParser parser = new ConsoleCloudEventParser();
            PolicyTriggeredCloudEvent receivedEvent = parser.fromJsonString(message.getPayload(), PolicyTriggeredCloudEvent.class);

            assertNotNull(receivedEvent);

            assertEquals(PolicyTriggeredCloudEvent.CLOUD_EVENT_TYPE, receivedEvent.getType());
            assertEquals(buildCloudEvent.getAccountId(), receivedEvent.getAccountId());
            assertEquals(buildCloudEvent.getOrgId(), receivedEvent.getOrgId());
            assertEquals(buildCloudEvent.getTime(), receivedEvent.getTime());
            assertEquals(buildCloudEvent.getSource(), receivedEvent.getSource());
            assertEquals(buildCloudEvent.getId(), receivedEvent.getId());
            assertEquals(buildCloudEvent.getDataSchema(), receivedEvent.getDataSchema());
            assertEquals(buildCloudEvent.getSubject(), receivedEvent.getSubject());

            assertEquals(buildCloudEvent.getData().getSystem().getInventoryID(), receivedEvent.getData().getSystem().getInventoryID());
            assertEquals(buildCloudEvent.getData().getSystem().getCheckIn(), receivedEvent.getData().getSystem().getCheckIn());
            assertEquals(buildCloudEvent.getData().getSystem().getTags().length, receivedEvent.getData().getSystem().getTags().length);

            assertEquals(buildCloudEvent.getData().getPolicies().length, receivedEvent.getData().getPolicies().length);

            for (int i = 0; i < buildCloudEvent.getData().getPolicies().length; ++i) {
                assertEquals(buildCloudEvent.getData().getPolicies()[i].getID(), receivedEvent.getData().getPolicies()[i].getID());
                assertEquals(buildCloudEvent.getData().getPolicies()[i].getName(), receivedEvent.getData().getPolicies()[i].getName());
                assertEquals(buildCloudEvent.getData().getPolicies()[i].getCondition(), receivedEvent.getData().getPolicies()[i].getCondition());
                assertEquals(buildCloudEvent.getData().getPolicies()[i].getDescription(), receivedEvent.getData().getPolicies()[i].getDescription());
                assertEquals(buildCloudEvent.getData().getPolicies()[i].getURL(), receivedEvent.getData().getPolicies()[i].getURL());
            }

            for (int i = 0; i < buildCloudEvent.getData().getSystem().getTags().length; ++i) {
                assertEquals(buildCloudEvent.getData().getSystem().getTags()[i].getNamespace(), receivedEvent.getData().getSystem().getTags()[i].getNamespace());
                assertEquals(buildCloudEvent.getData().getSystem().getTags()[i].getKey(), receivedEvent.getData().getSystem().getTags()[i].getKey());
                assertEquals(buildCloudEvent.getData().getSystem().getTags()[i].getValue(), receivedEvent.getData().getSystem().getTags()[i].getValue());
            }
        } finally {
            featureFlipper.setNotificationsAsCloudEvents(false);
        }
    }

    private static PoliciesAction buildPoliciesAction() {
        PoliciesAction action = new PoliciesAction();
        action.setAccountId("account-id");
        action.setOrgId("org-id");
        action.setTimestamp(LocalDateTime.now());
        action.getContext().setInventoryId("inventory-id");
        action.getContext().setDisplayName("display-name");
        action.getContext().setSystemCheckIn(LocalDateTime.now().minusDays(1L));
        action.setEvents(Set.of(buildPoliciesEvent(), buildPoliciesEvent()));
        action.getContext().setTags(buildTags());
        return action;
    }

    private static PolicyTriggeredCloudEvent buildPoliciesCloudEvent() {
        return PolicyTriggeredCloudEvent
                .builder()
                .setId(UUID.randomUUID())
                .setAccount("account-id")
                .setOrgId("org-id")
                .setTime(LocalDateTime.now())
                .setSystemInventoryId(UUID.randomUUID().toString())
                .setSystemCheckinTime(LocalDateTime.now())
                .setSystemDisplayName("my system name")
                .addSystemTag("ns-1", "key-1", "val-1")
                .addSystemTag("ns-1", "key-2", "val-2")
                .addSystemTag("ns-2", "key-3", "val-3")
                .addPolicy(
                        UUID.randomUUID(),
                        "Policy1",
                        "Policy with description",
                        "arch = 'x86_64'",
                        "https://some-url-using-https"
                )
                .addPolicy(
                        UUID.randomUUID(),
                        "Policy2",
                        "",
                        "arch = 'x86_64'",
                        "http://some-url-using-http"
                )
                .build();
    }

    private static PoliciesAction.Event buildPoliciesEvent() {
        PoliciesAction.Event event = new PoliciesAction.Event();
        event.getPayload().setPolicyId("policy-id");
        event.getPayload().setPolicyName("policy-name");
        event.getPayload().setPolicyDescription("policy-description");
        event.getPayload().setPolicyCondition("policy-condition");
        return event;
    }

    private static HashMap<String, Set<String>> buildTags() {
        HashMap<String, Set<String>> tags = new HashMap<>();
        tags.put("hello", Set.of("world"));
        tags.put("tags", Set.of("are", "working", "as", "intended"));
        return tags;
    }

    private static void checkMessageIdHeader(Message<String> message) {
        // The message should contain a "rh-message-id" header and its value should be a valid UUID version 4.
        Optional<KafkaMessageMetadata> messageMetadata = message.getMetadata(KafkaMessageMetadata.class);
        Iterator<Header> headers = messageMetadata.get().getHeaders().headers(MESSAGE_ID_HEADER).iterator();
        String headerValue = new String(headers.next().value(), UTF_8);
        // Is the header value a valid UUID? The following line will throw an exception otherwise.
        UUID.fromString(headerValue);
        // If the UUID version is 4, then its 15th character has to be "4".
        assertEquals("4", headerValue.substring(14, 15));
    }
}
