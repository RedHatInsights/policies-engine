package com.redhat.cloud.policies.engine.process;

import com.redhat.cloud.notifications.ingress.Action;
import com.redhat.cloud.policies.engine.TestLifecycleManager;
import com.redhat.cloud.policies.engine.history.PoliciesHistoryEntry;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.reactive.messaging.connectors.InMemoryConnector;
import io.smallrye.reactive.messaging.connectors.InMemorySink;
import io.smallrye.reactive.messaging.connectors.InMemorySource;
import io.smallrye.reactive.messaging.kafka.KafkaMessageMetadata;
import io.vertx.core.json.JsonObject;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.JsonDecoder;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.commons.io.IOUtils;
import org.apache.kafka.common.header.Header;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.hawkular.alerts.api.model.action.ActionDefinition;
import org.hawkular.alerts.api.model.condition.Condition;
import org.hawkular.alerts.api.model.condition.EventCondition;
import org.hawkular.alerts.api.model.event.Alert;
import org.hawkular.alerts.api.model.event.EventType;
import org.hawkular.alerts.api.model.paging.Page;
import org.hawkular.alerts.api.model.trigger.FullTrigger;
import org.hawkular.alerts.api.model.trigger.Mode;
import org.hawkular.alerts.api.model.trigger.Trigger;
import org.hawkular.alerts.api.model.trigger.TriggerAction;
import org.hawkular.alerts.api.services.AlertsCriteria;
import org.hawkular.alerts.api.services.AlertsService;
import org.hawkular.alerts.api.services.DefinitionsService;
import org.hawkular.alerts.api.services.StatusService;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Any;
import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static com.redhat.cloud.policies.engine.actions.plugins.NotificationActionPluginListener.MESSAGE_ID_HEADER;
import static com.redhat.cloud.policies.engine.actions.plugins.NotificationActionPluginListener.WEBHOOK_CHANNEL;
import static com.redhat.cloud.policies.engine.process.Receiver.EVENTS_CHANNEL;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@Tag("integration")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@QuarkusTestResource(TestLifecycleManager.class)
public class ReceiverTest {

    @Inject
    StatusService statusService;

    @BeforeAll
    void init() throws InterruptedException {
        System.setProperty("hawkular.data", "./target/hawkular.data");
        for(int i = 0; i < 100; i++) {
            if(statusService.isHealthy()) {
                break;
            }
            Thread.sleep(100);
        }
    }

    @Inject
    DefinitionsService definitionsService;

    @Inject
    AlertsService alertsService;

    @Inject
    MeterRegistry meterRegistry;

    @Inject
    @Any
    InMemoryConnector reactiveMessagingConnector;

    @Inject
    Session session;

    InMemorySource<String> hostEmitter;
    InMemorySink<String> webhookReceiver;

    @PostConstruct
    public void initReactiveMessagingChannels() {
        hostEmitter = reactiveMessagingConnector.source(EVENTS_CHANNEL);
        webhookReceiver = reactiveMessagingConnector.sink(WEBHOOK_CHANNEL);
    }

    private static final String TENANT_ID = "integration-test";
    private static final String ACTION_PLUGIN = "email";
    private static final String ACTION_ID = "email-notif";
    private static final String TRIGGER_ID = "arch-trigger";

    private final Counter errorCount = Counter.builder("engine.input.processed.errors").tags("queue", "host-egress").register(meterRegistry);

    private Action deserializeAction(String payload) {
        Action action = new Action();
        try {
            JsonDecoder jsonDecoder = DecoderFactory.get().jsonDecoder(Action.getClassSchema(), payload);
            DatumReader<Action> reader = new SpecificDatumReader<>(Action.class);
            reader.read(action, jsonDecoder);
        } catch (IOException e) {
            throw new IllegalArgumentException("Payload extraction failed", e);
        }
        return action;
    }

    private FullTrigger createTriggeringTrigger(String triggerId) {
        EventCondition evCond = new EventCondition();
//        evCond.setExpression("facts.arch = 'string'");
        evCond.setExpression("");
        evCond.setTenantId(TENANT_ID);
        evCond.setDataId(Receiver.INSIGHTS_REPORT_DATA_ID);
        List<Condition> conditions = Collections.singletonList(evCond);

        TriggerAction action = new TriggerAction(ACTION_PLUGIN, ACTION_ID);
        Set<TriggerAction> actions = Collections.singleton(action);

        Trigger trigger = new Trigger(TENANT_ID, triggerId, "Trigger from arch", null);
        trigger.setDescription("My description");
        trigger.setEventType(EventType.ALERT);
        trigger.setActions(actions);
        trigger.setMode(Mode.FIRING);
        trigger.setEnabled(true);

        return new FullTrigger(trigger, null, conditions);
    }

    @BeforeEach
    public void resetWebhookReceiver() {
        webhookReceiver.clear();
        clearPoliciesHistory();
    }

    @Test
    public void testReceiver() throws Exception {
        /*
        Create trigger definitions, send to hostEmitter and wait for the trigger to send an alert to email
         */
        ActionDefinition actionDefinition = new ActionDefinition(TENANT_ID, ACTION_PLUGIN, ACTION_ID);
        Map<String, String> props = new HashMap<>();
        actionDefinition.setProperties(props);
        definitionsService.addActionDefinition(TENANT_ID, actionDefinition);

        FullTrigger fullTrigger = createTriggeringTrigger(TRIGGER_ID);
        definitionsService.createFullTrigger(TENANT_ID, fullTrigger);

        // Create second trigger
        FullTrigger fullTrigger2 = createTriggeringTrigger(TRIGGER_ID + "2");
        definitionsService.createFullTrigger(TENANT_ID, fullTrigger2);

        // Read the input file and send it
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("input/host.json");
        String inputJson = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        hostEmitter.send(inputJson);

        // Wait for the async messaging to arrive
        // It's aggregated into one message
        await().until(() -> webhookReceiver.received().size() == 1);
        checkMessageIdHeader(webhookReceiver.received().get(0));
        checkPoliciesHistoryEntries(fullTrigger, 1);
        checkPoliciesHistoryEntries(fullTrigger2, 1);

        Action action = deserializeAction(webhookReceiver.received().get(0).getPayload());

        assertEquals(TENANT_ID, action.getAccountId());
        assertTrue(action.getContext().containsKey("inventory_id"));
        assertEquals(2, action.getEvents().size());

        // Now send broken data and then working and expect things to still work
        String brokenJson = "{ \"json\": ";
        hostEmitter.send(brokenJson);
        hostEmitter.send(inputJson);

        // Wait for the async messaging to arrive
        await().until(() -> webhookReceiver.received().size() == 2);
        checkMessageIdHeader(webhookReceiver.received().get(1));
        checkPoliciesHistoryEntries(fullTrigger, 2);
        checkPoliciesHistoryEntries(fullTrigger2, 2);

        Counter hostEgressProcessingErrors = meterRegistry.find(errorCount.getId().getName()).counter();
        assertEquals(1.0, hostEgressProcessingErrors.count());

        // Verify the alert includes the tags from the event
        AlertsCriteria criteria = new AlertsCriteria();
        criteria.setTagQuery("tags.display_name = 'VM'");
        Page<Alert> alerts = alertsService.getAlerts(TENANT_ID, criteria, null);

        // 4, because we have two triggers and we send the correct input twice
        assertEquals(4, alerts.size());

        definitionsService.removeTrigger(TENANT_ID, TRIGGER_ID + "2");
    }

    @Test
    void testMoreComplexInput() throws Exception {
        // Read the input file and send it
        InputStream is = getClass().getClassLoader().getResourceAsStream("input/thomas-host.json");
        String inputJson = IOUtils.toString(is, StandardCharsets.UTF_8);
        hostEmitter.send(inputJson);

        // Wait for the async messaging to arrive (there's two identical triggers..)
        await().until(() -> webhookReceiver.received().size() == 1);
        checkMessageIdHeader(webhookReceiver.received().get(0));

        Action action = deserializeAction(webhookReceiver.received().get(0).getPayload());
        assertEquals(1, action.getEvents().size());

        List<Map<String, String>> tags = (List<Map<String, String>>) action.getContext().get("tags");
        boolean foundNeuchatel = false;
        boolean foundCharmey = false;
        for(Map<String, String> tag : tags) {
            if (tag.get("key").equals("location")) {
                if (tag.get("value").equals("Neuchatel")) {
                    foundNeuchatel = true;
                } else if (tag.get("value").equals("Charmey")) {
                    foundCharmey =  true;
                }
            }
        }
        assertTrue(foundNeuchatel);
        assertTrue(foundCharmey);
    }

    @Test
    void testWebhookAvroOutput() throws Exception {
        String tenantId = TENANT_ID + "2";
        FullTrigger fullTrigger = createTriggeringTrigger(TRIGGER_ID + "3");

        TriggerAction triggerAction = new TriggerAction();
        triggerAction.setActionPlugin("notification");
        Set<TriggerAction> actions = Collections.singleton(triggerAction);

        fullTrigger.getTrigger().setActions(actions);
        definitionsService.createFullTrigger(tenantId, fullTrigger);

        InputStream is = getClass().getClassLoader().getResourceAsStream("input/thomas-host.json");
        JsonObject pushJson = new JsonObject(IOUtils.toString(is, StandardCharsets.UTF_8));

        pushJson.getJsonObject("host").put("account", tenantId);
        hostEmitter.send(pushJson.encode());

        // Wait for the async messaging to arrive (there's two identical triggers..)
        await().until(() -> webhookReceiver.received().size() == 1);
        checkMessageIdHeader(webhookReceiver.received().get(0));
        checkPoliciesHistoryEntries(fullTrigger, 1);

        Action action = deserializeAction(webhookReceiver.received().get(0).getPayload());
        assertEquals("rhel", action.getBundle());
        assertEquals("policies", action.getApplication());
        assertEquals("policy-triggered", action.getEventType());
        assertEquals(tenantId, action.getAccountId());

        // The trigger must be removed to prevent side-effects on other tests.
        definitionsService.removeTrigger(tenantId, fullTrigger.getTrigger().getId());
    }

    @Test
    void testTakeSystemCheckInFromUpdate() throws Exception {
        String tenantId = TENANT_ID + "2";
        FullTrigger fullTrigger = createTriggeringTrigger(TRIGGER_ID + "4");

        TriggerAction triggerAction = new TriggerAction();
        triggerAction.setActionPlugin("notification");
        Set<TriggerAction> actions = Collections.singleton(triggerAction);

        fullTrigger.getTrigger().setActions(actions);
        definitionsService.createFullTrigger(tenantId, fullTrigger);

        InputStream is = getClass().getClassLoader().getResourceAsStream("input/thomas-host.json");
        JsonObject pushJson = new JsonObject(IOUtils.toString(is, StandardCharsets.UTF_8));

        pushJson.getJsonObject("host").put("account", tenantId);
        hostEmitter.send(pushJson.encode());

        // Wait for the async messaging to arrive (there's two identical triggers..)
        await().until(() -> webhookReceiver.received().size() == 1);
        checkMessageIdHeader(webhookReceiver.received().get(0));
        checkPoliciesHistoryEntries(fullTrigger, 1);

        Action action = deserializeAction(webhookReceiver.received().get(0).getPayload());
        assertEquals("rhel", action.getBundle());
        assertEquals("policies", action.getApplication());
        assertEquals("policy-triggered", action.getEventType());
        assertEquals(TENANT_ID + "2", action.getAccountId());

        assertNotNull(action.getTimestamp());
        assertEquals("2020-04-16T16:10:42.199046", action.getContext().get("system_check_in"));

        // The trigger must be removed to prevent side-effects on other tests.
        definitionsService.removeTrigger(tenantId, fullTrigger.getTrigger().getId());
    }

    @AfterAll
    void cleanup() throws Exception {
        // Delete what we created..
        definitionsService.removeTrigger(TENANT_ID, TRIGGER_ID);
        definitionsService.removeActionDefinition(TENANT_ID, ACTION_PLUGIN, ACTION_ID);
    }

    private void clearPoliciesHistory() {
        Transaction transaction = session.beginTransaction();
        session.createQuery("DELETE FROM PoliciesHistoryEntry").executeUpdate();
        transaction.commit();
    }

    private void checkPoliciesHistoryEntries(FullTrigger trigger, int expectedSize) {
        String query = "SELECT e FROM PoliciesHistoryEntry e WHERE e.tenantId = :tenantId AND e.policyId = :policyId";
        List<PoliciesHistoryEntry> history = session.createQuery(query, PoliciesHistoryEntry.class)
                .setParameter("tenantId", trigger.getTrigger().getTenantId())
                .setParameter("policyId", trigger.getTrigger().getId())
                .getResultList();
        assertEquals(expectedSize, history.size());
    }

    private void checkMessageIdHeader(Message<String> message) {
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
