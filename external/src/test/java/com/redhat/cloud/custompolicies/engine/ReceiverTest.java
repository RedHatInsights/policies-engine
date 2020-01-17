package com.redhat.cloud.custompolicies.engine;

import io.quarkus.test.junit.QuarkusTest;
import io.reactivex.subscribers.TestSubscriber;
import io.smallrye.reactive.messaging.annotations.Channel;
import io.smallrye.reactive.messaging.annotations.Emitter;
import io.vertx.core.json.JsonObject;
import org.apache.commons.io.IOUtils;
import org.hawkular.alerts.api.model.action.Action;
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
import org.reactivestreams.Publisher;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

@QuarkusTest
@Tag("integration")
public class ReceiverTest {

    @Inject
    @Channel("kafka-hosts")
    Emitter<JsonObject> hostEmitter;

    @Inject
    @Channel("email")
    Publisher<String> emailReceiver;

    @Inject
    DefinitionsService definitionsService;

    @Test
    public void testReceiver() throws Exception {
        /*
        Create trigger definitions, send to hostEmitter and wait for the trigger to send an alert to email
         */
        ActionDefinition actionDefinition = new ActionDefinition("integration-test", "email", "email-notif");
        Map<String, String> props = new HashMap<>();
        props.put("to", "my@destination.com");
        actionDefinition.setProperties(props);
        definitionsService.addActionDefinition("integration-test", actionDefinition);

        EventCondition evCond = new EventCondition();
        evCond.setExpression("facts.arch = 'string'");
        evCond.setTenantId("integration-test");
        List<Condition> conditions = Collections.singletonList(evCond);

        TriggerAction action = new TriggerAction("email", "email-notif");
        Set<TriggerAction> actions = Collections.singleton(action);

        Trigger trigger = new Trigger("integration-test", "arch-trigger", "Trigger from arch", null);
        trigger.setEventType(EventType.ALERT);
        trigger.setActions(actions);
        trigger.setMode(Mode.FIRING);
        trigger.setEnabled(true);


        FullTrigger fullTrigger = new FullTrigger(trigger, null, conditions);
        definitionsService.createFullTrigger("integration-test", fullTrigger);

        TestSubscriber<String> testSubscriber = new TestSubscriber<>();
        emailReceiver.subscribe(testSubscriber);

        // Read the input file and send it
        InputStream is = getClass().getClassLoader().getResourceAsStream("input/host.json");
        String inputJson = IOUtils.toString(is, StandardCharsets.UTF_8);
        JsonObject json = new JsonObject(inputJson);
        hostEmitter.send(json);

        // Wait for the async messaging to arrive
        testSubscriber.await(10, TimeUnit.SECONDS);
        testSubscriber.assertValueCount(1);
    }
}
