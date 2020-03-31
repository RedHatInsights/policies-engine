package com.redhat.cloud.policies.engine.process;

import io.quarkus.test.junit.QuarkusTest;
import io.reactivex.subscribers.TestSubscriber;
import io.smallrye.reactive.messaging.annotations.Channel;
import io.smallrye.reactive.messaging.annotations.Emitter;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.apache.commons.io.IOUtils;
import org.eclipse.microprofile.metrics.*;
import org.eclipse.microprofile.metrics.annotation.RegistryType;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.hawkular.alerts.api.model.action.ActionDefinition;
import org.hawkular.alerts.api.model.condition.Condition;
import org.hawkular.alerts.api.model.condition.EventCondition;
import org.hawkular.alerts.api.model.event.EventType;
import org.hawkular.alerts.api.model.trigger.FullTrigger;
import org.hawkular.alerts.api.model.trigger.Mode;
import org.hawkular.alerts.api.model.trigger.Trigger;
import org.hawkular.alerts.api.model.trigger.TriggerAction;
import org.hawkular.alerts.api.services.DefinitionsService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Tag;
import org.reactivestreams.Publisher;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletionStage;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@Tag("integration")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ReceiverTest {

    @BeforeAll
    void init() {
        System.setProperty("hawkular.data", "./target/hawkular.data");
    }

    @Inject
    @Channel("host-egress")
    Emitter<String> hostEmitter;

    @Inject
    @Channel("email")
    Publisher<JsonObject> emailReceiver;

    @Inject
    DefinitionsService definitionsService;

    @Inject
    @RegistryType(type=MetricRegistry.Type.APPLICATION)
    MetricRegistry metricRegistry;

    private static final String TENANT_ID = "integration-test";
    private static final String ACTION_PLUGIN = "email";
    private static final String ACTION_ID = "email-notif";
    private static final String TRIGGER_ID = "arch-trigger";

    private final MetricID errorCount = new MetricID("engine.input.processed.errors", new org.eclipse.microprofile.metrics.Tag("queue", "host-egress"));

    @Test
    public void testReceiver() throws Exception {
        /*
        Create trigger definitions, send to hostEmitter and wait for the trigger to send an alert to email
         */
        ActionDefinition actionDefinition = new ActionDefinition(TENANT_ID, ACTION_PLUGIN, ACTION_ID);
        Map<String, String> props = new HashMap<>();
        actionDefinition.setProperties(props);
        definitionsService.addActionDefinition(TENANT_ID, actionDefinition);

        EventCondition evCond = new EventCondition();
        evCond.setExpression("facts.arch = 'string'");
        evCond.setTenantId(TENANT_ID);
        evCond.setDataId(Receiver.INSIGHTS_REPORT_DATA_ID);
        List<Condition> conditions = Collections.singletonList(evCond);

        TriggerAction action = new TriggerAction(ACTION_PLUGIN, ACTION_ID);
        Set<TriggerAction> actions = Collections.singleton(action);

        Trigger trigger = new Trigger(TENANT_ID, TRIGGER_ID, "Trigger from arch", null);
        trigger.setEventType(EventType.ALERT);
        trigger.setActions(actions);
        trigger.setMode(Mode.FIRING);
        trigger.setEnabled(true);

        FullTrigger fullTrigger = new FullTrigger(trigger, null, conditions);
        definitionsService.createFullTrigger(TENANT_ID, fullTrigger);

        // Create second trigger
        fullTrigger.getTrigger().setName("Trigger from past");
        fullTrigger.getTrigger().setId(TRIGGER_ID + "2");

        definitionsService.createFullTrigger(TENANT_ID, fullTrigger);

        TestSubscriber<JsonObject> testSubscriber = new TestSubscriber<>();
        emailReceiver.subscribe(testSubscriber);

        // Read the input file and send it
        InputStream is = getClass().getClassLoader().getResourceAsStream("input/host.json");
        String inputJson = IOUtils.toString(is, StandardCharsets.UTF_8);
        hostEmitter.send(inputJson);

        // Wait for the async messaging to arrive
        testSubscriber.awaitCount(1);
        testSubscriber.assertValueCount(1);

        JsonObject emailOutput = testSubscriber.values().get(0);
        assertEquals(TENANT_ID, emailOutput.getString("tenantId"));
        assertTrue(emailOutput.containsKey("tags"));
        assertTrue(emailOutput.containsKey("insightId"));
        assertTrue(emailOutput.containsKey("triggerNames"));
        JsonArray triggerNames = emailOutput.getJsonArray("triggerNames");
        assertEquals(2, triggerNames.size());

        // Now send broken data and then working and expect things to still work
        String brokenJson = "{ \"json\": ";
        hostEmitter.send(brokenJson);
        hostEmitter.send(inputJson);

        // Wait for the async messaging to arrive
        testSubscriber.awaitCount(2);
        testSubscriber.assertValueCount(2);

        Counter hostEgressProcessingErrors = metricRegistry.getCounters().get(errorCount);
        assertEquals(1, hostEgressProcessingErrors.getCount());
        testSubscriber.dispose(); // In current smallrye-messaging, can't resubscribe even after dispose.
    }

    @AfterAll
    void cleanup() throws Exception {
        // Delete what we created..
        definitionsService.removeTrigger(TENANT_ID, TRIGGER_ID);
        definitionsService.removeActionDefinition(TENANT_ID, ACTION_PLUGIN, ACTION_ID);
    }
}
