package com.redhat.cloud.policies.engine.lightweight;

import org.hawkular.alerts.api.model.condition.Condition;
import org.hawkular.alerts.api.model.condition.EventCondition;
import org.hawkular.alerts.api.model.trigger.FullTrigger;
import org.hawkular.alerts.api.model.trigger.Trigger;
import org.hawkular.alerts.api.model.trigger.TriggerAction;

import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

public class PolicyToTriggerConverter {

    private static final String NO_LONGER_USED = "";
    private static final Pattern ACTIONS_SPLIT_PATTERN = Pattern.compile(";");

    public static FullTrigger convert(Policy policy, boolean useOrgId) {
        FullTrigger fullTrigger = new FullTrigger();
        fullTrigger.setTrigger(buildTrigger(policy, useOrgId));
        fullTrigger.setConditions(List.of(buildCondition(fullTrigger.getTrigger(), policy.condition)));
        return fullTrigger;
    }

    private static Trigger buildTrigger(Policy policy, boolean useOrgId) {
        Trigger trigger = new Trigger();

        if (useOrgId) {
            trigger.setTenantId(policy.orgId);
        } else {
            trigger.setTenantId(policy.accountId);
        }

        trigger.setId(policy.id.toString());
        trigger.setName(policy.name);
        trigger.setDescription(policy.description);
        trigger.setEnabled(policy.enabled);
        if (policy.actions != null) {
            String[] actionPlugins = ACTIONS_SPLIT_PATTERN.split(policy.actions);
            if (actionPlugins.length > 0) {
                trigger.setActions(new HashSet<>());
                for (String actionPlugin : actionPlugins) {
                    String plugin = actionPlugin.trim();
                    if (!plugin.isEmpty()) {
                        TriggerAction triggerAction = new TriggerAction();
                        triggerAction.setActionPlugin(plugin);
                        trigger.getActions().add(triggerAction);
                    }
                }
            }
        }
        return trigger;
    }

    private static Condition buildCondition(Trigger trigger, String expression) {
        return new EventCondition(trigger.getTenantId(), trigger.getId(), NO_LONGER_USED, expression);
    }
}
