package org.hawkular.alerts.rest

import io.quarkus.test.junit.QuarkusTest
import org.hawkular.alerts.api.model.action.ActionDefinition
import org.hawkular.alerts.api.model.condition.AvailabilityCondition
import org.hawkular.alerts.api.model.condition.Condition
import org.hawkular.alerts.api.model.condition.ThresholdCondition
import org.hawkular.alerts.api.model.data.Data
import org.hawkular.alerts.api.model.trigger.Mode
import org.hawkular.alerts.api.model.trigger.Trigger
import org.hawkular.alerts.api.model.trigger.TriggerAction
import org.hawkular.alerts.log.MsgLogger
import org.hawkular.alerts.log.MsgLogging
import org.junit.jupiter.api.Test

import static org.hawkular.alerts.api.model.event.Alert.Status

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertNotNull
import static org.junit.jupiter.api.Assertions.assertTrue

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

/**
 * Actions REST tests.
 *
 * @author Lucas Ponce
 */
@QuarkusTest
class ActionsITest extends AbstractQuarkusITestBase {

    static MsgLogger logger = MsgLogging.getMsgLogger(ActionsITest.class)

    @Test
    void findSupportedPlugins() {
        // Email plugin and webhook plugin should be installed by default
        def resp = client.get(path: "plugins")
        def data = resp.data
        assertEquals(200, resp.status)
        assertTrue(data.size() == 2)
        assertThat(resp.data, containsInAnyOrder("email", "webhook"))
    }

    @Test
    void createAction() {
        String actionPlugin = "webhook"
        String actionId = "test-action";

        Map<String, String> properties = new HashMap<>();
        properties.put("endpoint_id", "1");

        ActionDefinition actionDefinition = new ActionDefinition(null, actionPlugin, actionId, properties);

        def resp = client.post(path: "actions", body: actionDefinition)
        assertEquals(200, resp.status)

        resp = client.get(path: "actions/" + actionPlugin + "/" + actionId);
        assertEquals(200, resp.status)
        assertEquals("1", resp.data.properties.endpoint_id)

        actionDefinition.getProperties().put("endpoint_id", "2")
        resp = client.put(path: "actions", body: actionDefinition)
        assertEquals(200, resp.status)

        resp = client.get(path: "actions/" + actionPlugin + "/" + actionId)
        assertEquals(200, resp.status)
        assertEquals("2", resp.data.properties.endpoint_id)

        resp = client.delete(path: "actions/" + actionPlugin + "/" + actionId)
        assertEquals(200, resp.status)
    }

    @Test
    void failWithUnknownPropertyOnPlugin() {
        // CREATE the action definition
        String actionPlugin = "email"
        String actionId = "email-to-admin";

        Map<String, String> actionProperties = new HashMap<>();
        actionProperties.put("bad-property", "cc-developers@company.org");

        ActionDefinition actionDefinition = new ActionDefinition(null, actionPlugin, actionId, actionProperties);

        def resp = client.post(path: "actions", body: actionDefinition)
        assert(400 == resp.status)
    }

    @Test
    void availabilityTest() {
        String start = String.valueOf(System.currentTimeMillis());

        // CREATE the action definition
        String actionPlugin = "webhook"
        String actionId = "webhook-to-admin";

        Map<String, String> actionProperties = new HashMap<>();
        actionProperties.put("endpoint_id", "1");

        ActionDefinition actionDefinition = new ActionDefinition(null, actionPlugin, actionId, actionProperties);

        def resp = client.post(path: "actions", body: actionDefinition)
        assert(200 == resp.status || 400 == resp.status)

        // CREATE the trigger
        resp = client.get(path: "")
        assert resp.status == 200 : resp.status

        Trigger testTrigger = new Trigger("test-webhook-availability", "http://www.mydemourl.com");

        // remove if it exists
        resp = client.delete(path: "triggers/test-webhook-availability")
        assert(200 == resp.status || 404 == resp.status)

        testTrigger.setAutoDisable(false);
        testTrigger.setAutoResolve(false);
        testTrigger.setAutoResolveAlerts(false);
        /*
            email-to-admin action is pre-created from demo data
         */
        testTrigger.addAction(new TriggerAction("webhook", "webhook-to-admin"));

        resp = client.post(path: "triggers", body: testTrigger)
        assertEquals(200, resp.status)

        // ADD Firing condition
        AvailabilityCondition firingCond = new AvailabilityCondition("test-webhook-availability",
                Mode.FIRING, "test-webhook-availability", AvailabilityCondition.Operator.NOT_UP);

        Collection<Condition> conditions = new ArrayList<>(1);
        conditions.add( firingCond );
        resp = client.put(path: "triggers/test-webhook-availability/conditions/firing", body: conditions)
        assertEquals(200, resp.status)
        assertEquals(1, resp.data.size())

        // ENABLE Trigger
        testTrigger.setEnabled(true);

        resp = client.put(path: "triggers/test-webhook-availability", body: testTrigger)
        assertEquals(200, resp.status)

        // FETCH trigger and make sure it's as expected
        resp = client.get(path: "triggers/test-webhook-availability");
        assertEquals(200, resp.status)
        assertEquals("http://www.mydemourl.com", resp.data.name)
        assertEquals(true, resp.data.enabled)
        assertEquals(false, resp.data.autoDisable);
        assertEquals(false, resp.data.autoResolve);
        assertEquals(false, resp.data.autoResolveAlerts);

        // FETCH recent alerts for trigger, should not be any
        resp = client.get(path: "", query: [startTime:start,triggerIds:"test-webhook-availability"] )
        assertEquals(200, resp.status)

        waitDefinitions()

        // Send in DOWN avail data to fire the trigger
        // Instead of going through the bus, in this test we'll use the alerts rest API directly to send data
        for (int i=0; i<5; i++) {
            Data avail = new Data("test-webhook-availability", System.currentTimeMillis() + (i*1000), "DOWN");
            Collection<Data> datums = new ArrayList<>();
            datums.add(avail);
            resp = client.post(path: "data", body: datums);
            assertEquals(200, resp.status)
        }

        // The alert processing happens async, so give it a little time before failing...
        for ( int i=0; i < 10; ++i ) {
            Thread.sleep(500);

            // FETCH recent alerts for trigger, there should be 5
            resp = client.get(path: "", query: [startTime:start,triggerIds:"test-webhook-availability"] )
            if ( resp.status == 200 && resp.data.size() == 5 ) {
                break;
            }
        }
        assertEquals(200, resp.status)
        assertEquals(5, resp.data.size())
        assertEquals("OPEN", resp.data[0].status)

        resp = client.delete(path: "triggers/test-webhook-availability");
        assertEquals(200, resp.status)

        resp = client.delete(path: "actions/" + actionPlugin + "/" + actionId)
        assertEquals(200, resp.status)
    }

    @Test
    void thresholdTest() {
        String start = String.valueOf(System.currentTimeMillis());

        // CREATE the action definition
        String actionPlugin = "webhook"
        String actionId = "webhook-to-admin";

        Map<String, String> actionProperties = new HashMap<>();
        actionProperties.put("endpoint_id", "1");

        ActionDefinition actionDefinition = new ActionDefinition(null, actionPlugin, actionId, actionProperties);

        def resp = client.post(path: "actions", body: actionDefinition)
        assert(200 == resp.status || 400 == resp.status)

        // CREATE the trigger
        resp = client.get(path: "")
        assert resp.status == 200 : resp.status

        Trigger testTrigger = new Trigger("test-webhook-threshold", "http://www.mydemourl.com");

        // remove if it exists
        resp = client.delete(path: "triggers/test-webhook-threshold")
        assert(200 == resp.status || 404 == resp.status)

        testTrigger.setAutoDisable(false);
        testTrigger.setAutoResolve(false);
        testTrigger.setAutoResolveAlerts(false);
        /*
            email-to-admin action is pre-created from demo data
         */
        testTrigger.addAction(new TriggerAction(actionPlugin, actionId));

        resp = client.post(path: "triggers", body: testTrigger)
        assertEquals(200, resp.status)

        // ADD Firing condition
        ThresholdCondition firingCond = new ThresholdCondition("test-webhook-threshold",
                Mode.FIRING, "test-webhook-threshold", ThresholdCondition.Operator.GT, 300);

        Collection<Condition> conditions = new ArrayList<>(1);
        conditions.add( firingCond );
        resp = client.put(path: "triggers/test-webhook-threshold/conditions/firing", body: conditions)
        assertEquals(200, resp.status)
        assertEquals(1, resp.data.size())

        // ENABLE Trigger
        resp = client.put(path: "triggers/enabled", query:[triggerIds:"test-webhook-threshold",enabled:true] )
        assertEquals(200, resp.status)

        // FETCH trigger and make sure it's as expected
        resp = client.get(path: "triggers/test-webhook-threshold");
        assertEquals(200, resp.status)
        assertEquals("http://www.mydemourl.com", resp.data.name)
        assertEquals(true, resp.data.enabled)
        assertEquals(false, resp.data.autoDisable);
        assertEquals(false, resp.data.autoResolve);
        assertEquals(false, resp.data.autoResolveAlerts);

        // FETCH recent alerts for trigger, should not be any
        resp = client.get(path: "", query: [startTime:start,triggerIds:"test-webhook-threshold"] )
        assertEquals(200, resp.status)

        waitDefinitions()

        // Send in data to fire the trigger
        // Instead of going through the bus, in this test we'll use the alerts rest API directly to send data
        for (int i=0; i<5; i++) {
            Data threshold = new Data("test-webhook-threshold", System.currentTimeMillis() + (i*1000), String.valueOf(305.5 + i));
            Collection<Data> datums = new ArrayList<>();
            datums.add(threshold);
            resp = client.post(path: "data", body: datums);
            assertEquals(200, resp.status)
        }

        // The alert processing happens async, so give it a little time before failing...
        for ( int i=0; i < 10; ++i ) {
            Thread.sleep(500);

            // FETCH recent alerts for trigger, there should be 5
            resp = client.get(path: "", query: [startTime:start,triggerIds:"test-webhook-threshold"] )
            if ( resp.status == 200 && resp.data.size() == 5 ) {
                break;
            }
        }
        assertEquals(200, resp.status)
        assertEquals(5, resp.data.size())
        assertEquals("OPEN", resp.data[0].status)

        resp = client.delete(path: "triggers/test-webhook-threshold");
        assertEquals(200, resp.status)

        resp = client.delete(path: "actions/" + actionPlugin + "/" + actionId)
        assertEquals(200, resp.status)
    }

    @Test
    void actionByStatusTest() {
        String start = String.valueOf(System.currentTimeMillis());

        // Check endpoint
        def resp = client.get(path: "")
        assert resp.status == 200 : resp.status

        // Create an action definition for admins
        String actionPlugin = "webhook"
        String actionId = "notify-to-admins";

        // Remove previous history
        client.put(path: "actions/history/delete", query: [actionPlugins:"webhook"])

        // Remove a previous action
        client.delete(path: "actions/" + actionPlugin + "/" + actionId)

        Map<String, String> actionProperties = new HashMap<>();
        actionProperties.put("endpoint_id", "1");

        ActionDefinition actionDefinition = new ActionDefinition(null, actionPlugin, actionId, actionProperties);

        resp = client.post(path: "actions", body: actionDefinition)
        assertEquals(200, resp.status)

        // Create an action definition for developers
        actionPlugin = "webhook"
        actionId = "notify-to-developers";

        // Remove a previous action
        client.delete(path: "actions/" + actionPlugin + "/" + actionId)

        actionProperties = new HashMap<>();
        actionProperties.put("endpoint_id", "2");

        actionDefinition = new ActionDefinition(null, actionPlugin, actionId, actionProperties);

        resp = client.post(path: "actions", body: actionDefinition)
        assertEquals(200, resp.status)

        // Create a trigger

        Trigger testTrigger = new Trigger("test-status-threshold", "http://www.mydemourl.com");

        // remove if it exists
        resp = client.delete(path: "triggers/test-status-threshold")
        assert(200 == resp.status || 404 == resp.status)

        testTrigger.setAutoDisable(false);
        testTrigger.setAutoResolve(false);
        testTrigger.setAutoResolveAlerts(false);

        TriggerAction notifyAdmins = new TriggerAction(actionPlugin, "notify-to-admins");
        notifyAdmins.addState(Status.OPEN.name());
        TriggerAction notifyDevelopers = new TriggerAction(actionPlugin, "notify-to-developers");
        notifyDevelopers.addState(Status.ACKNOWLEDGED.name());

        testTrigger.addAction(notifyAdmins);
        testTrigger.addAction(notifyDevelopers);

        resp = client.post(path: "triggers", body: testTrigger)
        assertEquals(200, resp.status)

        // ADD Firing condition
        ThresholdCondition firingCond = new ThresholdCondition("test-status-threshold",
                Mode.FIRING, "test-status-threshold", ThresholdCondition.Operator.GT, 300);

        Collection<Condition> conditions = new ArrayList<>(1);
        conditions.add( firingCond );
        resp = client.put(path: "triggers/test-status-threshold/conditions/firing", body: conditions)
        assertEquals(200, resp.status)
        assertEquals(1, resp.data.size())

        // ENABLE Trigger
        resp = client.put(path: "triggers/enabled", query: [triggerIds:"test-status-threshold",enabled:true] )
        assertEquals(200, resp.status)

        // FETCH trigger and make sure it's as expected
        resp = client.get(path: "triggers/test-status-threshold");
        assertEquals(200, resp.status)
        assertEquals("http://www.mydemourl.com", resp.data.name)
        assertEquals(true, resp.data.enabled)
        assertEquals(false, resp.data.autoDisable);
        assertEquals(false, resp.data.autoResolve);
        assertEquals(false, resp.data.autoResolveAlerts);

        waitDefinitions()

        // Send in data to fire the trigger
        // Instead of going through the bus, in this test we'll use the alerts rest API directly to send data
        for (int i=0; i<5; i++) {
            Data threshold = new Data("test-status-threshold", System.currentTimeMillis() + (i*1000), String.valueOf(305.5 + i));
            Collection<Data> datums = new ArrayList<>();
            datums.add(threshold);
            resp = client.post(path: "data", body: datums);
            assertEquals(200, resp.status)
        }

        // The alert processing happens async, so give it a little time before failing...
        for ( int i=0; i < 100; ++i ) {
            Thread.sleep(500);

            // FETCH recent alerts for trigger, there should be 5
            resp = client.get(path: "", query: [startTime:start,triggerIds:"test-status-threshold"] )
            if ( resp.status == 200 && resp.data.size() == 5 ) {
                break;
            }
        }
        assertEquals(200, resp.status)
        assertEquals(5, resp.data.size())
        assertEquals("OPEN", resp.data[0].status)

        def alertsToAck = resp.data;

        // Check actions generated
        // This used to fail randomly, therefore try several times before failing
        for ( int i=0; i < 20; ++i ) {
            resp = client.get(path: "actions/history", query: [startTime:start,actionPlugins:"webhook"])
            if ( resp.status == 200 && resp.data.size() == 5 ) {
                break;
            }
            Thread.sleep(500);
        }
        assertEquals(200, resp.status)
        assertEquals(5, resp.data.size())

        // Ack alerts generated
        def alertsToAckIds = "";
        for ( int i=0; i < alertsToAck.size(); i++ ) {
            alertsToAckIds += alertsToAck[i].id;
            if (i != 4) {
                alertsToAckIds += ",";
            }
        }

        // ACK Alerts generated
        client.put(path: "ack", query: [alertIds:alertsToAckIds,ackBy:"testUser",ackNotes:"testNotes"] )

        // Check if we have the actions for ACKNOWLEDGE
        for ( int i=0; i < 10; ++i ) {
            Thread.sleep(500);

            // FETCH recent alerts for trigger, there should be 5
            resp = client.get(path: "actions/history", query: [startTime:start,actionPlugins:"webhook"])
            if ( resp.status == 200 && resp.data.size() == 10 ) {
                break;
            }
        }

        assertEquals(200, resp.status)
        assertEquals(10, resp.data.size())

        resp = client.delete(path: "triggers/test-status-threshold");
        assertEquals(200, resp.status)

        resp = client.delete(path: "actions/webhook/notify-to-admins")
        assertEquals(200, resp.status)

        resp = client.delete(path: "actions/webhook/notify-to-developers")
        assertEquals(200, resp.status)
    }

    @Test
    void globalActionsTest() {
        String start = String.valueOf(System.currentTimeMillis());

        // Check endpoint
        def resp = client.get(path: "")
        assert resp.status == 200 : resp.status

        // Create an action definition for admins
        String actionPlugin = "webhook"
        String actionId = "global-action-notify-to-admins";

        // Remove previous history
        client.put(path: "actions/history/delete", query: [actionPlugins:"webhook"])

        // Remove a previous action
        client.delete(path: "actions/" + actionPlugin + "/" + actionId)

        Map<String, String> actionProperties = new HashMap<>();
        actionProperties.put("endpoint_id", "1");

        ActionDefinition actionDefinition = new ActionDefinition(null, actionPlugin, actionId, actionProperties);
        actionDefinition.setGlobal(true);

        resp = client.post(path: "actions", body: actionDefinition)
        assertEquals(200, resp.status)

        // Create an action definition for developers
        actionPlugin = "webhook"
        actionId = "global-action-notify-to-developers";

        // Remove a previous action
        client.delete(path: "actions/" + actionPlugin + "/" + actionId)

        actionProperties = new HashMap<>();
        actionProperties.put("endpoint_id", "2");

        actionDefinition = new ActionDefinition(null, actionPlugin, actionId, actionProperties);
        actionDefinition.setGlobal(true);

        resp = client.post(path: "actions", body: actionDefinition)
        assertEquals(200, resp.status)

        // Create a trigger

        Trigger testTrigger = new Trigger("test-global-status-threshold", "http://www.mydemourl.com");

        // remove if it exists
        resp = client.delete(path: "triggers/test-global-status-threshold")
        assert(200 == resp.status || 404 == resp.status)

        testTrigger.setAutoDisable(false);
        testTrigger.setAutoResolve(false);
        testTrigger.setAutoResolveAlerts(false);

        resp = client.post(path: "triggers", body: testTrigger)
        assertEquals(200, resp.status)

        // ADD Firing condition
        ThresholdCondition firingCond = new ThresholdCondition("test-global-status-threshold",
                Mode.FIRING, "test-global-status-threshold", ThresholdCondition.Operator.GT, 300);

        Collection<Condition> conditions = new ArrayList<>(1);
        conditions.add( firingCond );
        resp = client.put(path: "triggers/test-global-status-threshold/conditions/firing", body: conditions)
        assertEquals(200, resp.status)
        assertEquals(1, resp.data.size())

        // ENABLE Trigger
        resp = client.put(path: "triggers/enabled", query:[triggerIds:"test-global-status-threshold",enabled:true] )
        assertEquals(200, resp.status)

        // FETCH trigger and make sure it's as expected
        resp = client.get(path: "triggers/test-global-status-threshold");
        assertEquals(200, resp.status)
        assertEquals("http://www.mydemourl.com", resp.data.name)
        assertEquals(true, resp.data.enabled)
        assertEquals(false, resp.data.autoDisable);
        assertEquals(false, resp.data.autoResolve);
        assertEquals(false, resp.data.autoResolveAlerts);

        waitDefinitions()

        // Send in data to fire the trigger
        // Instead of going through the bus, in this test we'll use the alerts rest API directly to send data
        for (int i=0; i<5; i++) {
            Data threshold = new Data("test-global-status-threshold", System.currentTimeMillis() + (i*1000),
                    String.valueOf(305.5 + i));
            Collection<Data> datums = new ArrayList<>();
            datums.add(threshold);
            resp = client.post(path: "data", body: datums);
            assertEquals(200, resp.status)
        }

        // The alert processing happens async, so give it a little time before failing...
        for ( int i=0; i < 100; ++i ) {
            Thread.sleep(500);

            // FETCH recent alerts for trigger, there should be 5
            resp = client.get(path: "", query: [startTime:start,triggerIds:"test-global-status-threshold"] )
            if ( resp.status == 200 && resp.data.size() == 5 ) {
                break;
            }
        }
        assertEquals(200, resp.status)
        assertEquals(5, resp.data.size())
        assertEquals("OPEN", resp.data[0].status)

        // Check actions generated
        // This used to fail randomly, therefore try several times before failing
        for ( int i=0; i < 30; ++i ) {
            resp = client.get(path: "actions/history",
                    query: [startTime:start,actionPlugins:"webhook",
                            actionIds:"global-action-notify-to-admins,global-action-notify-to-developers"])
            if ( resp.status == 200 && resp.data.size() == 10 ) {
                break;
            }
            Thread.sleep(500);
        }

        assertEquals(200, resp.status)
        assertEquals(10, resp.data.size())

        resp = client.delete(path: "triggers/test-global-status-threshold");
        assertEquals(200, resp.status)

        resp = client.delete(path: "actions/webhook/global-action-notify-to-admins")
        assertEquals(200, resp.status)

        resp = client.delete(path: "actions/webhook/global-action-notify-to-developers")
        assertEquals(200, resp.status)
    }
}
