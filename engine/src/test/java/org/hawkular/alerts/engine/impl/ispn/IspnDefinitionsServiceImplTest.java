package org.hawkular.alerts.engine.impl.ispn;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.quarkus.runtime.configuration.ConfigUtils;
import io.quarkus.runtime.configuration.QuarkusConfigFactory;
import io.smallrye.config.SmallRyeConfig;
import org.hawkular.alerts.api.exception.FoundException;
import org.hawkular.alerts.api.exception.NotFoundException;
import org.hawkular.alerts.api.model.Lifecycle;
import org.hawkular.alerts.api.model.action.ActionDefinition;
import org.hawkular.alerts.api.model.condition.AvailabilityCondition;
import org.hawkular.alerts.api.model.condition.AvailabilityCondition.Operator;
import org.hawkular.alerts.api.model.condition.Condition;
import org.hawkular.alerts.api.model.dampening.Dampening;
import org.hawkular.alerts.api.model.event.Alert;
import org.hawkular.alerts.api.model.paging.AlertComparator;
import org.hawkular.alerts.api.model.paging.Order;
import org.hawkular.alerts.api.model.paging.Page;
import org.hawkular.alerts.api.model.paging.Pager;
import org.hawkular.alerts.api.model.paging.TriggerComparator;
import org.hawkular.alerts.api.model.trigger.*;
import org.hawkular.alerts.api.services.AlertsCriteria;
import org.hawkular.alerts.api.services.TriggersCriteria;
import org.hawkular.alerts.log.MsgLogger;
import org.hawkular.alerts.log.MsgLogging;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Jay Shaughnessy
 * @author Lucas Ponce
 */
public class IspnDefinitionsServiceImplTest extends IspnBaseServiceImplTest {
    static final MsgLogger log = MsgLogging.getMsgLogger(IspnDefinitionsServiceImplTest.class);
    static final String TENANT = "testTenant";

    @BeforeClass
    public static void init() {
        SmallRyeConfig config = ConfigUtils.configBuilder(true).build();
        QuarkusConfigFactory.setConfig(config);
        System.setProperty("hawkular.data", "./target/ispn");
        definitions = new IspnDefinitionsServiceImpl();
        definitions.init();
    }

    @Test
    public void addGetUpdateRemoveActionPluginTest() throws Exception {
        Set<String> props = new HashSet<>();
        props.add("prop1");
        props.add("prop2");
        props.add("prop3");
        definitions.addActionPlugin("plugin1", props);
        assertNotNull(definitions.getActionPlugin("plugin1"));

        try {
            definitions.addActionPlugin("plugin1", props);
            fail("It should throw a FoundException");
        } catch (FoundException e) {
            // Expected
        }

        Set<String> updated = new HashSet<>();
        updated.add("prop4");
        updated.add("prop5");
        updated.add("prop6");
        definitions.updateActionPlugin("plugin1", updated);
        assertEquals(updated, definitions.getActionPlugin("plugin1"));

        try {
            definitions.updateActionPlugin("pluginX", updated);
            fail("It should throw a NotFoundException");
        } catch (NotFoundException e) {
            // Expected
        }

        definitions.removeActionPlugin("plugin1");
        assertNull(definitions.getActionPlugin("plugin1"));
    }

    @Test
    public void addGetUpdateRemoveActionDefinitionTest() throws Exception {
        Set<String> props = new HashSet<>();
        props.add("prop1");
        props.add("prop2");
        props.add("prop3");
        definitions.addActionPlugin("plugin2", props);

        ActionDefinition actionDefinition = new ActionDefinition();
        actionDefinition.setTenantId(TENANT);
        actionDefinition.setActionPlugin("plugin2");
        actionDefinition.setActionId("action1");
        actionDefinition.setProperties(new HashMap<>());
        actionDefinition.getProperties().put("prop1", "value1");
        actionDefinition.getProperties().put("prop2", "value2");
        actionDefinition.getProperties().put("prop3", "value3");

        definitions.addActionDefinition(TENANT, actionDefinition);
        assertEquals(actionDefinition, definitions.getActionDefinition(TENANT, "plugin2", "action1"));

        try {
            definitions.addActionDefinition(TENANT, actionDefinition);
            fail("It should throw a FoundException");
        } catch (FoundException e) {
            // Expected
        }

        Map<String, String> updated = new HashMap<>();
        updated.put("prop1", "value1-updated");
        updated.put("prop2", "value2-updated");
        updated.put("prop3", "value-updated");
        actionDefinition.setProperties(updated);
        definitions.updateActionDefinition(TENANT, actionDefinition);

        assertEquals(updated, definitions.getActionDefinition(TENANT, "plugin2", "action1").getProperties());

        Map<String, String> wrong = new HashMap<>();
        wrong.put("prop4", "prop4 doesnt exist in plugin1");
        actionDefinition.setProperties(wrong);

        try {
            definitions.updateActionDefinition(TENANT, actionDefinition);
            fail("It should throw a IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }

        try {
            actionDefinition.setActionId("action2");
            actionDefinition.setProperties(updated);
            definitions.updateActionDefinition(TENANT, actionDefinition);
            fail("It should throw a NotFoundException");
        } catch (NotFoundException e) {
            // Expected
        }

        definitions.removeActionDefinition(TENANT, "plugin2", "action1");
        assertNull(definitions.getActionDefinition(TENANT, "plugin2", "action1"));

        definitions.removeActionPlugin("plugin2");
        assertNull(definitions.getActionPlugin("plugin2"));
    }

    @Test
    public void getActionDefinitions() throws Exception {
        int numTenants = 2;
        int numPlugins = 2;
        int numActions = 4;
        createTestPluginsAndActions(numTenants, numPlugins, numActions);

        assertEquals(2 * 2 * 4, definitions.getAllActionDefinitions().size());

        Map<String, Map<String, Set<String>>> actionIds = definitions.getAllActionDefinitionIds();
        assertEquals(numTenants, actionIds.keySet().size());
        assertEquals(numPlugins, actionIds.get("tenant0").keySet().size());
        assertEquals(numActions, actionIds.get("tenant0").get("plugin0").size());

        Map<String, Set<String>> actionIdsByTenant = definitions.getActionDefinitionIds("tenant1");
        assertEquals(numPlugins, actionIdsByTenant.keySet().size());

        Collection<String> actionIdsByTenantAndPlugin = definitions.getActionDefinitionIds("tenant1", "plugin1");
        assertEquals(numActions, actionIdsByTenantAndPlugin.size());

        deleteTestPluginsAndActions(numTenants, numPlugins, numActions);
    }

    @Test
    public void addGetUpdateRemoveTrigger() throws Exception {
        Trigger trigger = new Trigger("trigger1", "Trigger1 Test");
        definitions.addTrigger(TENANT, trigger);

        assertEquals(trigger, definitions.getTrigger(TENANT, "trigger1"));

        try {
            definitions.addTrigger(TENANT, trigger);
            fail("It should throw a FoundException");
        } catch (FoundException e) {
            // Expected
        }

        trigger.setDescription("This is a long description for Trigger1 Test");
        trigger.addTag("tag1", "value1");
        definitions.updateTrigger(TENANT, trigger, true);

        Trigger updated = definitions.getTrigger(TENANT, "trigger1");
        assertEquals(trigger.getDescription(), updated.getDescription());
        assertEquals(trigger.getTags(), updated.getTags());

        try {
            trigger.setId("trigger2");
            definitions.updateTrigger(TENANT, trigger, true);
        } catch (NotFoundException e) {
            // Expected
        }

        definitions.removeTrigger(TENANT, "trigger1");
        try {
            definitions.getTrigger(TENANT, "trigger1");
            fail("IT should throw a NotFoundException");
        } catch (NotFoundException e) {
            // Expected
        }
    }

    @Test
    public void getTriggers() throws Exception {
        int numTenants = 4;
        int numTriggers = 10;
        createTestTriggers(numTenants, numTriggers);

        assertEquals(4 * 10, definitions.getAllTriggers().size());
//        assertEquals(4 * 10 / 2, definitions.getAllTriggersByTag("tag0", "*").size());
//        assertEquals(4 * 10 / 4, definitions.getAllTriggersByTag("tag0", "value0").size());
        assertEquals(10, definitions.getTriggers("tenant0", null, null).size());
        assertEquals(10, definitions.getTriggers("tenant3", null, null).size());

        TriggersCriteria criteria = new TriggersCriteria();
        criteria.addTriggerIdFilter("trigger0");
        assertEquals(1, definitions.getTriggers("tenant0", criteria, null).size());

        criteria.addTriggerIdFilter(null);
        criteria.setTriggerIds(Arrays.asList("trigger0", "trigger1", "trigger2", "trigger3"));

        assertEquals(4, definitions.getTriggers("tenant0", criteria, null).size());

        // Test enabled query
        criteria.setTriggerIds(null);
        criteria.setQuery("enabled = 'false'");
        Page<Trigger> t0Triggers = definitions.getTriggers("tenant0", criteria, null);
        assertEquals(10, t0Triggers.size());

        for (Trigger t0Trigger : t0Triggers) {
            t0Trigger.setEnabled(true);
            t0Trigger = definitions.updateTrigger(t0Trigger.getTenantId(), t0Trigger, true);
            assertTrue(t0Trigger.isEnabled());
        }
        criteria.setQuery("enabled = 'true'");
        t0Triggers = definitions.getTriggers("tenant0", criteria, null);
        assertEquals(10, t0Triggers.size());

        criteria.setQuery("enabled");
        t0Triggers = definitions.getTriggers("tenant0", criteria, null);
        assertEquals(10, t0Triggers.size());

        criteria.setQuery("!enabled");
        t0Triggers = definitions.getTriggers("tenant0", criteria, null);
        assertEquals(0, t0Triggers.size());

        criteria.setQuery("tags.tag0 = 'value0' OR tags.tag1 = 'value1'");
        assertEquals(6, definitions.getTriggers("tenant0", criteria, null).size());

        criteria.setQuery("tags.tag0 matches '*'");
        assertEquals(5, definitions.getTriggers("tenant0", criteria, null).size());
        deleteTestTriggers(numTenants, numTriggers);
    }

    @Test
    public void conditionsTest() throws Exception {
        int numTenants = 1;
        int numTriggers = 1;
        createTestTriggers(numTenants, numTriggers);

        Condition fc = new AvailabilityCondition("trigger-0", Mode.FIRING, "firing-cond", Operator.NOT_UP);
        Condition rc = new AvailabilityCondition("trigger-0", Mode.AUTORESOLVE, "resolve-cond", Operator.UP);

        Set<Condition> conditions = new HashSet<>(Arrays.asList(fc, rc));

        conditions = new HashSet<>(definitions.setAllConditions("tenant0", "trigger0", conditions));

        // get All Conditions
        Set<Condition> fetchedConditions = new HashSet<>(definitions.getAllConditions());
        assertEquals(conditions, fetchedConditions);

        // get tenant Conditions
        fetchedConditions = new HashSet<>(definitions.getConditions("tenant0"));
        assertEquals(conditions, fetchedConditions);

        fetchedConditions = new HashSet<>(definitions.getConditions("tenantX"));
        assertEquals(0, fetchedConditions.size());

        // get trigger conditions
        fetchedConditions = new HashSet<>(
                definitions.getTriggerConditions("tenant0", "trigger0", null));
        assertEquals(conditions, fetchedConditions);

        // get firing conditions
        fetchedConditions = new HashSet<>(
                definitions.getTriggerConditions("tenant0", "trigger0", Mode.FIRING));
        assertEquals(Collections.singleton(fc), fetchedConditions);

        // get autoresolve conditions
        fetchedConditions = new HashSet<>(
                definitions.getTriggerConditions("tenant0", "trigger0", Mode.AUTORESOLVE));
        assertEquals(Collections.singleton(rc), fetchedConditions);

        // verify updated conditions

        fetchedConditions = new HashSet<>(
                definitions.getTriggerConditions("tenant0", "trigger0", Mode.FIRING));

        for (Condition fetchedCondition : fetchedConditions) {
            fetchedCondition.updateLastEvaluation();
        }

        definitions.updateConditions("tenant0", "trigger0", fetchedConditions);

        Collection<Condition> triggerConditions = definitions.getTriggerConditions("tenant0", "trigger0", Mode.FIRING);
        assertEquals(1, triggerConditions.size());
        for (Condition triggerCondition : triggerConditions) {
            assertTrue(triggerCondition.getLastEvaluation() > 0);
        }

        triggerConditions = definitions.getTriggerConditions("tenant0", "trigger0", Mode.AUTORESOLVE);
        assertEquals(1, triggerConditions.size());
        for (Condition triggerCondition : triggerConditions) {
            assertEquals(0, triggerCondition.getLastEvaluation());
        }

        deleteTestTriggers(numTenants, numTriggers);
    }

    @Test
    public void dampeningTest() throws Exception {
        int numTenants = 1;
        int numTriggers = 1;
        createTestTriggers(numTenants, numTriggers);

        Dampening fd = Dampening.forStrict("tenant0", "trigger0", Mode.FIRING, 3);
        Dampening ad = Dampening.forRelaxedCount("tenant0", "trigger0", Mode.AUTORESOLVE, 3, 5);

        definitions.addDampening("tenant0", fd);
        definitions.addDampening("tenant0", ad);

        // get dampening
        Dampening fetchedDampening = definitions.getDampening("tenant0", fd.getDampeningId());
        assertEquals(fd, fetchedDampening);

        fetchedDampening = definitions.getDampening("tenant0", ad.getDampeningId());
        assertEquals(ad, fetchedDampening);

        // get All Dampening
        Set<Dampening> expectedDampenings = new HashSet<>(Arrays.asList(fd, ad));

        Set<Dampening> fetchedDampenings = new HashSet<>(definitions.getAllDampenings());
        assertEquals(expectedDampenings, fetchedDampenings);

        // get tenant Dampenings
        fetchedDampenings = new HashSet<>(definitions.getDampenings("tenant0"));
        assertEquals(expectedDampenings, fetchedDampenings);

        fetchedDampenings = new HashSet<>(definitions.getDampenings("tenantX"));
        assertEquals(0, fetchedDampenings.size());

        // get trigger Dampenings
        fetchedDampenings = new HashSet<>(definitions.getTriggerDampenings("tenant0", "trigger0", null));
        assertEquals(expectedDampenings, fetchedDampenings);

        // get firing conditions
        fetchedDampenings = new HashSet<>(definitions.getTriggerDampenings("tenant0", "trigger0", Mode.FIRING));
        assertEquals(Collections.singleton(fd), fetchedDampenings);

        // get autoresolve conditions
        fetchedDampenings = new HashSet<>(definitions.getTriggerDampenings("tenant0", "trigger0", Mode.AUTORESOLVE));
        assertEquals(Collections.singleton(ad), fetchedDampenings);

        // update dampening
        ad.setEvalTotalSetting(10);
        definitions.updateDampening("tenant0", ad);
        fetchedDampening = definitions.getDampening("tenant0", ad.getDampeningId());
        assertEquals(ad, fetchedDampening);

        deleteTestTriggers(numTenants, numTriggers);
    }

    @Test
    public void groupTest() throws Exception {
        Trigger groupTrigger = new Trigger("groupTrigger0", "groupTrigger0");
        groupTrigger.setType(TriggerType.GROUP);
        definitions.addGroupTrigger("tenant0", groupTrigger);
        Condition fc = new AvailabilityCondition("group-trigger", Mode.FIRING, "avail", Operator.NOT_UP);
        definitions.setGroupConditions("tenant0", "groupTrigger0", Mode.FIRING, Collections.singleton(fc), null);
        definitions.addMemberTrigger("tenant0", "groupTrigger0", "member0", "member0", "member0", null, null,
                Collections.singletonMap("avail", "avail0"));
        assertEquals(1, definitions.getMemberTriggers("tenant0", "groupTrigger0", true).size());
        definitions.removeGroupTrigger("tenant0", "groupTrigger0", false, false);
    }

    @Test
    public void testManagedFullTriggerCreation() throws Exception {
        Map<String, String> props = new HashMap<>();
        props.put("_managed", "true");
        props.put("id", "");
        definitions.addActionPlugin("pluginM", props);

        TriggerAction action = new TriggerAction();
        action.setActionPlugin("pluginM");
        Map<String, String> actionProps = new HashMap<>();
        actionProps.put("id", "abcdefghjikl");
        action.setProperties(actionProps);

        FullTrigger fullTrigger = new FullTrigger();
        Trigger trigger = new Trigger();
        Set<TriggerAction> actionSet = new HashSet<>();
        actionSet.add(action);
        trigger.setActions(actionSet);
        trigger.setId("trigger1");

        fullTrigger.setTrigger(trigger);
        definitions.createFullTrigger(TENANT, fullTrigger);

        assertNotNull(fullTrigger.getTrigger().getActions());
        String actionId = fullTrigger.getTrigger().getActions().iterator().next().getActionId();
        assertTrue(actionId.startsWith("_managed"));

        trigger.setId("trigger2");
        action.setActionId(null);

        definitions.createFullTrigger(TENANT, fullTrigger);
        String actionId2 = fullTrigger.getTrigger().getActions().iterator().next().getActionId();
        assertEquals(actionId, actionId2);

        // Test updating with original data - ensure that the managed id is really recreated
        for (TriggerAction triggerAction : fullTrigger.getTrigger().getActions()) {
            triggerAction.setActionId(null);
        }
        definitions.updateFullTrigger(TENANT, fullTrigger);
        assertEquals(actionId, fullTrigger.getTrigger().getActions().iterator().next().getActionId());

        definitions.removeTrigger(TENANT, "trigger1");
        definitions.removeTrigger(TENANT, "trigger2");
        definitions.removeActionDefinition(TENANT, "pluginM", actionId);
        definitions.removeActionPlugin("pluginM");
    }

    @Test
    public void testLifecycleHistory() throws Exception {
        Map<String, String> props = new HashMap<>();
        props.put("_managed", "true");
        props.put("id", "");
        definitions.addActionPlugin("pluginM", props);

        TriggerAction action = new TriggerAction();
        action.setActionPlugin("pluginM");
        Map<String, String> actionProps = new HashMap<>();
        actionProps.put("id", "abcdefghjikl");
        action.setProperties(actionProps);

        FullTrigger fullTrigger = new FullTrigger();
        Trigger trigger = new Trigger();
        Set<TriggerAction> actionSet = new HashSet<>();
        actionSet.add(action);
        trigger.setActions(actionSet);
        trigger.setId("trigger1");
        trigger.addLifecycle(Trigger.TriggerLifecycle.CREATED, 0, null);

        fullTrigger.setTrigger(trigger);
        definitions.createFullTrigger(TENANT, fullTrigger);

        // Try to update first with null trigger (should get NotFoundException)
        fullTrigger.setTrigger(null);

        try {
            definitions.updateFullTrigger(TENANT, fullTrigger);
            fail("Should have gotten IllegalArgumentException");
        } catch(IllegalArgumentException e) { }

        fullTrigger = definitions.getFullTrigger(TENANT, "trigger1");
        // Created only
        assertEquals(1, fullTrigger.getTrigger().getLifecycle().size());

        fullTrigger.getTrigger().setEnabled(true);
        fullTrigger.getTrigger().getLifecycle().clear();
        fullTrigger.getTrigger().addLifecycle(Trigger.TriggerLifecycle.MODIFIED, 0, null);
        definitions.updateFullTrigger(TENANT, fullTrigger);

        FullTrigger fT2 = definitions.getFullTrigger(TENANT, fullTrigger.getTrigger().getId());
        List<Lifecycle> lifecycle = fT2.getTrigger().getLifecycle();
        // Created and Modified
        assertEquals(2, lifecycle.size());

        String actionId = fullTrigger.getTrigger().getActions().iterator().next().getActionId();
        definitions.removeTrigger(TENANT, "trigger1");
        definitions.removeActionDefinition(TENANT, "pluginM", actionId);
        definitions.removeActionPlugin("pluginM");
    }

    @Test
    public void getTriggersWithPaging() throws Exception {
        int numTenants = 1;
        int numTriggers = 1000;
        createTestTriggers(numTenants, numTriggers);

        String tenantIds = "tenant0";

        TriggersCriteria criteria = new TriggersCriteria();
        Pager pager = Pager.builder()
                .orderBy(Order.by(TriggerComparator.Field.ID.getName(), Order.Direction.DESCENDING))
                .withPageSize(50)
                .build();

        Page<Trigger> triggerPage = definitions.getTriggers(tenantIds, criteria, pager);
        assertEquals(50, triggerPage.size());
        assertEquals(1000, triggerPage.getTotalSize());

        // Take last full page
        pager = Pager.builder()
                .orderBy(Order.by(TriggerComparator.Field.ID.getName(), Order.Direction.DESCENDING))
                .withPageSize(50)
                .withStartPage(19)
                .build();

        triggerPage = definitions.getTriggers(tenantIds, criteria, pager);
        assertEquals(50, triggerPage.size());
        assertEquals(1000, triggerPage.getTotalSize());

        Trigger alert = triggerPage.get(49);
        assertEquals("trigger0", alert.getId());

        pager = Pager.builder()
                .orderBy(Order.by(TriggerComparator.Field.ID.getName(), Order.Direction.DESCENDING))
                .withPageSize(50)
                .withStartPage(20)
                .build();

        triggerPage = definitions.getTriggers(tenantIds, criteria, pager);
        assertEquals(0, triggerPage.size());
        assertEquals(1000, triggerPage.getTotalSize());

        deleteTestTriggers(numTenants, numTriggers);

        // Test with 101 and 99 alerts (near the page boundary)

        createTestTriggers(1,  99);
        pager = Pager.builder()
                .orderBy(Order.by(TriggerComparator.Field.ID.getName(), Order.Direction.DESCENDING))
                .withPageSize(50)
                .withStartPage(0)
                .build();

        triggerPage = definitions.getTriggers(tenantIds, criteria, pager);
        assertEquals(50, triggerPage.size());
        assertEquals("trigger98", triggerPage.get(0).getId());
        assertEquals(99, triggerPage.getTotalSize());

        pager = Pager.builder()
                .orderBy(Order.by(TriggerComparator.Field.ID.getName(), Order.Direction.DESCENDING))
                .withPageSize(50)
                .withStartPage(1)
                .build();
        triggerPage = definitions.getTriggers(tenantIds, criteria, pager);

        assertEquals(49, triggerPage.size());
        assertEquals("trigger0", triggerPage.get(48).getId());
        assertEquals(99, triggerPage.getTotalSize());

        deleteTestTriggers(1, 99);

        // 101

        createTestTriggers(1, 101);
        pager = Pager.builder()
                .orderBy(Order.by(TriggerComparator.Field.ID.getName(), Order.Direction.DESCENDING))
                .withPageSize(50)
                .withStartPage(0)
                .build();

        triggerPage = definitions.getTriggers(tenantIds, criteria, pager);
        assertEquals(50, triggerPage.size());
        assertEquals(101, triggerPage.getTotalSize());

        pager = Pager.builder()
                .orderBy(Order.by(TriggerComparator.Field.ID.getName(), Order.Direction.DESCENDING))
                .withPageSize(50)
                .withStartPage(1)
                .build();

        triggerPage = definitions.getTriggers(tenantIds, criteria, pager);
        assertEquals(50, triggerPage.size());
        assertEquals(101, triggerPage.getTotalSize());

        pager = Pager.builder()
                .orderBy(Order.by(TriggerComparator.Field.ID.getName(), Order.Direction.DESCENDING))
                .withPageSize(50)
                .withStartPage(2)
                .build();

        triggerPage = definitions.getTriggers(tenantIds, criteria, pager);
        assertEquals(1, triggerPage.size());
        assertEquals("trigger0", triggerPage.get(0).getId());
        assertEquals(101, triggerPage.getTotalSize());

        deleteTestTriggers(1, 101);
    }
}
