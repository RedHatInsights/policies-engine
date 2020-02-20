package com.redhat.cloud.custompolicies.engine;

import io.quarkus.test.junit.QuarkusTest;
import io.reactivex.subscribers.TestSubscriber;
import io.smallrye.reactive.messaging.annotations.Channel;
import io.smallrye.reactive.messaging.annotations.Emitter;
import io.vertx.core.json.JsonObject;
import org.apache.commons.io.IOUtils;
import org.hawkular.alerts.api.model.action.ActionDefinition;
import org.hawkular.alerts.api.model.condition.Condition;
import org.hawkular.alerts.api.model.condition.EventCondition;
import org.hawkular.alerts.api.model.event.EventType;
import org.hawkular.alerts.api.model.trigger.FullTrigger;
import org.hawkular.alerts.api.model.trigger.Mode;
import org.hawkular.alerts.api.model.trigger.Trigger;
import org.hawkular.alerts.api.model.trigger.TriggerAction;
import org.hawkular.alerts.api.services.DefinitionsService;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.reactivestreams.Publisher;

import javax.inject.Inject;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


@QuarkusTest
@Tag("integration")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ReceiverTest {

    @Inject
    @Channel("kafka-hosts")
    Emitter<JsonObject> hostEmitter;

    @Inject
    @Channel("email")
    Publisher<JsonObject> emailReceiver;

    @Inject
    @Channel("webhook")
    Publisher<JsonObject> webhookReceiver;

    @Inject
    DefinitionsService definitionsService;

    @Test
    public void testReceiver() throws Exception {
        String tenantId = "integration-test";
        String actionPlugin = "email";
        String actionId =  "email-notif";
        String triggerId = "arch-trigger";
        
        /*
        Create trigger definitions, send to hostEmitter and wait for the trigger to send an alert to email
         */
        ActionDefinition actionDefinition = new ActionDefinition(tenantId, actionPlugin, actionId);
        Map<String, String> props = new HashMap<>();
        actionDefinition.setProperties(props);
        definitionsService.addActionDefinition(tenantId, actionDefinition);

        EventCondition evCond = new EventCondition();
        evCond.setExpression("facts.arch = 'string'");
        evCond.setTenantId(tenantId);
        List<Condition> conditions = Collections.singletonList(evCond);

        TriggerAction action = new TriggerAction(actionPlugin, actionId);
        Set<TriggerAction> actions = Collections.singleton(action);

        Trigger trigger = new Trigger(tenantId, triggerId, "Trigger from arch", null);
        trigger.setEventType(EventType.ALERT);
        trigger.setActions(actions);
        trigger.setMode(Mode.FIRING);
        trigger.setEnabled(true);

        FullTrigger fullTrigger = new FullTrigger(trigger, null, conditions);
        definitionsService.createFullTrigger(tenantId, fullTrigger);

        TestSubscriber<JsonObject> testSubscriber = new TestSubscriber<>();
        emailReceiver.subscribe(testSubscriber);

        // Read the input file and send it
        InputStream is = getClass().getClassLoader().getResourceAsStream("input/host.json");
        String inputJson = IOUtils.toString(is, StandardCharsets.UTF_8);
        JsonObject json = new JsonObject(inputJson);
        hostEmitter.send(json);

        // Wait for the async messaging to arrive
        testSubscriber.awaitCount(1);
        testSubscriber.assertValueCount(1);

        JsonObject emailOutput = testSubscriber.values().get(0);
        assertEquals(tenantId, emailOutput.getString("tenantId"));
        assertTrue(emailOutput.containsKey("tags"));
        assertTrue(emailOutput.containsKey("triggerNames"));
        
        // Delete what we created..
        definitionsService.removeTrigger(tenantId, triggerId);
        definitionsService.removeActionDefinition(tenantId, actionPlugin, actionId);
    }
}
