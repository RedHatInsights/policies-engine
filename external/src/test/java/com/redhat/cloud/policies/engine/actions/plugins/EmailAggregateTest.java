package com.redhat.cloud.policies.engine.actions.plugins;

import com.redhat.cloud.policies.engine.process.Receiver;
import io.quarkus.test.junit.QuarkusTest;
import io.reactivex.subscribers.TestSubscriber;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.hawkular.alerts.actions.api.ActionMessage;
import org.hawkular.alerts.actions.api.model.StandaloneActionMessage;
import org.hawkular.alerts.api.model.action.Action;
import org.hawkular.alerts.api.model.condition.ConditionEval;
import org.hawkular.alerts.api.model.condition.EventConditionEval;
import org.hawkular.alerts.api.model.event.Event;
import org.hawkular.alerts.api.model.trigger.Trigger;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.reactivestreams.Publisher;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Disabled // Quarkus #7974
public class EmailAggregateTest {

    @Inject
    @Channel("email")
    Publisher<JsonObject> emailReceiver;

    @Inject
    EmailActionPluginListener emailPlugin;

    @Test
    public void testInsightsIdAggregation() throws Exception {
        String triggerName = "triggerName";
        Trigger trigger = new Trigger();
        trigger.setName(triggerName);

        Event event = new Event();
        Map<String, String> tagsMap = new HashMap<>();
        tagsMap.put(Receiver.DISPLAY_NAME_FIELD, "localhost");
        event.setTags(tagsMap);
        event.setTrigger(trigger);

        EventConditionEval evCond = new EventConditionEval();
        Map<String, String> contextMap = new HashMap<>();
        contextMap.put(Receiver.INSIGHT_ID_FIELD, "123456789");
        evCond.setContext(contextMap);
        evCond.setValue(event);

        Set<ConditionEval> evCondSet = Set.of(evCond);
        event.setEvalSets(List.of(evCondSet));

        String tenantId = "tenantId";
        Action action = new Action();
        action.setTenantId(tenantId);
        action.setEvent(event);

        ActionMessage actionMessage = new StandaloneActionMessage(action);

        emailPlugin.process(actionMessage);

        trigger.setName(triggerName + "2");
        emailPlugin.process(actionMessage);

        TestSubscriber<JsonObject> emailSubscriber = new TestSubscriber<>();
        emailReceiver.subscribe(emailSubscriber);

        emailPlugin.flush();

        emailSubscriber.awaitCount(1);
        JsonObject emailOutput = emailSubscriber.values().get(0);
        assertEquals(tenantId, emailOutput.getString("tenantId"));
        assertTrue(emailOutput.containsKey("tags"));
        assertTrue(emailOutput.containsKey("triggerNames"));

        JsonArray triggerNames = emailOutput.getJsonArray("triggerNames");
        assertEquals(2, triggerNames.size());

        emailSubscriber.assertValueCount(1);
        emailSubscriber.dispose();
    }

}
