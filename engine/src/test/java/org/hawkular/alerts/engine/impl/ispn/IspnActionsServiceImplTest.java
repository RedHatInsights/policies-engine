package org.hawkular.alerts.engine.impl.ispn;

import org.hawkular.alerts.api.model.action.Action;
import org.hawkular.alerts.api.model.event.Alert;
import org.hawkular.alerts.api.model.trigger.Trigger;
import org.hawkular.alerts.api.model.trigger.TriggerAction;
import org.hawkular.alerts.api.services.ActionsCriteria;
import org.hawkular.alerts.api.services.AlertsCriteria;
import org.hawkular.alerts.engine.cache.ActionsCacheManager;
import org.hawkular.alerts.engine.cache.IspnCacheManager;
import org.hawkular.alerts.engine.impl.AlertsContext;
import org.hawkular.alerts.log.MsgLogger;
import org.hawkular.alerts.log.MsgLogging;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Jay Shaughnessy
 * @author Lucas Ponce
 */
public class IspnActionsServiceImplTest extends IspnBaseServiceImplTest {
    static final MsgLogger log = MsgLogging.getMsgLogger(IspnActionsServiceImplTest.class);

    @BeforeClass
    public static void init() {
        try {
            System.setProperty("hawkular.data", "./target/ispn");

            AlertsContext alertsContext = new AlertsContext();

            definitions = new IspnDefinitionsServiceImpl();
            definitions.setAlertsContext(alertsContext);
            definitions.init();

            alerts = new IspnAlertsServiceImpl();
            alerts.init();

            actions = new IspnActionsServiceImpl();
            actions.init();

            ActionsCacheManager actionsCacheManager = new ActionsCacheManager();
            actionsCacheManager.setDefinitions(definitions);
            actionsCacheManager.setGlobalActionsCache(IspnCacheManager.getCacheManager().getCache("globalActions"));

            actions.setActionsCacheManager(actionsCacheManager);
            actions.setAlertsContext(alertsContext);
            actions.setDefinitions(definitions);

            alertsContext.init();
            actionsCacheManager.init();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void actionsTest() throws Exception {
        int numTenants = 1;
        int numPlugins = 1;
        int numActions = 2;
        int numTriggers = 1;
        int numAlerts = 1;

        try {
            createTestPluginsAndActions(numTenants, numPlugins, numActions);
            createTestTriggers(numTenants, numTriggers);
            long startTime = createTestAlerts(numTenants, numTriggers, numAlerts);

            Trigger trigger = definitions.getTrigger("tenant0", "trigger0");

            TriggerAction triggerAction0 = new TriggerAction("tenant0", "plugin0", "action0");
            trigger.addAction(triggerAction0);
            TriggerAction triggerAction1 = new TriggerAction("tenant0", "plugin0", "action1");
            trigger.addAction(triggerAction1);
            definitions.updateTrigger("tenant0", trigger, true);

            Trigger updated = definitions.getTrigger("tenant0", "trigger0");
            assertTrue(trigger != updated);
            assertEquals(2, updated.getActions().size());
            List<TriggerAction> tas = new ArrayList<>(updated.getActions());
            if ("action0".equals(tas.get(0).getActionId())) {
                assertEquals(triggerAction0, tas.get(0));
                assertEquals(triggerAction1, tas.get(1));
            } else {
                assertEquals(triggerAction0, tas.get(1));
                assertEquals(triggerAction1, tas.get(0));
            }

            AlertsCriteria alertsCriteria = new AlertsCriteria();
            alertsCriteria.setThin(true);
            List<Alert> existingAlerts = alerts.getAlerts("tenant0", alertsCriteria, null);
            assertNotNull(existingAlerts);
            assertEquals(1, existingAlerts.size());

            actions.send(updated, existingAlerts.get(0));

            List<Action> existingActions = actions.getActions("tenant0", null, null);
            assertNotNull(existingActions);
            assertEquals(2, existingActions.size());

            ActionsCriteria actionsCriteria = new ActionsCriteria();

            actionsCriteria.setActionPlugin("xxx");
            existingActions = actions.getActions("tenant0", actionsCriteria, null);
            assertNotNull(existingActions);
            assertEquals(0, existingActions.size());

            actionsCriteria.setActionPlugin("plugin0");
            existingActions = actions.getActions("tenant0", actionsCriteria, null);
            assertNotNull(existingActions);
            assertEquals(2, existingActions.size());

            actionsCriteria.setActionId("xxx");
            existingActions = actions.getActions("tenant0", actionsCriteria, null);
            assertNotNull(existingActions);
            assertEquals(0, existingActions.size());

            actionsCriteria.setActionId("action0");
            existingActions = actions.getActions("tenant0", actionsCriteria, null);
            assertNotNull(existingActions);
            assertEquals(1, existingActions.size());

            actionsCriteria.setEventId("xxx");
            existingActions = actions.getActions("tenant0", actionsCriteria, null);
            assertNotNull(existingActions);
            assertEquals(0, existingActions.size());

            actionsCriteria.setEventId(existingAlerts.get(0).getAlertId());
            existingActions = actions.getActions("tenant0", actionsCriteria, null);
            assertNotNull(existingActions);
            assertEquals(1, existingActions.size());

            actionsCriteria.setEndTime(startTime);
            actionsCriteria.setStartTime(startTime);
            existingActions = actions.getActions("tenant0", actionsCriteria, null);
            assertNotNull(existingActions);
            assertEquals(0, existingActions.size());

            actionsCriteria.setEndTime(System.currentTimeMillis());
            actionsCriteria.setStartTime(startTime);
            existingActions = actions.getActions("tenant0", actionsCriteria, null);
            assertNotNull(existingActions);
            assertEquals(1, existingActions.size());

            actionsCriteria.setResult("xxx");
            existingActions = actions.getActions("tenant0", actionsCriteria, null);
            assertNotNull(existingActions);
            assertEquals(0, existingActions.size());

            actionsCriteria.setResult("WAITING");
            existingActions = actions.getActions("tenant0", actionsCriteria, null);
            assertNotNull(existingActions);
            assertEquals(1, existingActions.size());

            Action existingAction = existingActions.get(0);
            existingAction.setResult("SUCCESS");
            actions.updateResult(existingAction);

            actionsCriteria.setResult("WAITING");
            existingActions = actions.getActions("tenant0", actionsCriteria, null);
            assertNotNull(existingActions);
            assertEquals(0, existingActions.size());

            actionsCriteria.setResult("SUCCESS");
            existingActions = actions.getActions("tenant0", actionsCriteria, null);
            assertNotNull(existingActions);
            assertEquals(1, existingActions.size());

            actions.deleteActions("tenant0", actionsCriteria);
            existingActions = actions.getActions("tenant0", actionsCriteria, null);
            assertNotNull(existingActions);
            assertEquals(0, existingActions.size());

            existingActions = actions.getActions("tenant0", null, null);
            assertNotNull(existingActions);
            assertEquals(1, existingActions.size());

            actions.deleteActions("tenant0", null);
            existingActions = actions.getActions("tenant0", null, null);
            assertNotNull(existingActions);
            assertEquals(0, existingActions.size());

            deleteTestAlerts(1);
            deleteTestTriggers(1, 1);
            deleteTestPluginsAndActions(1, 1, 2);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

}
