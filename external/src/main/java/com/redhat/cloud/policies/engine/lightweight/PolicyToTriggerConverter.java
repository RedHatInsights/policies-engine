package com.redhat.cloud.policies.engine.lightweight;

import org.hawkular.alerts.api.model.condition.Condition;
import org.hawkular.alerts.api.model.condition.EventCondition;
import org.hawkular.alerts.api.model.trigger.FullTrigger;
import org.hawkular.alerts.api.model.trigger.Trigger;

import java.util.List;

public class PolicyToTriggerConverter {

    private static final String NO_LONGER_USED = "";

    public static FullTrigger convert(Policy policy) {
        FullTrigger fullTrigger = new FullTrigger();
        fullTrigger.setTrigger(buildTrigger(policy));
        fullTrigger.setConditions(List.of(buildCondition(fullTrigger.getTrigger(), policy.condition)));
        return fullTrigger;
    }

    private static Trigger buildTrigger(Policy policy) {
        Trigger trigger = new Trigger();
        trigger.setTenantId(policy.accountId);
        trigger.setId(policy.id.toString());
        trigger.setName(policy.name);
        trigger.setDescription(policy.description);
        trigger.setEnabled(policy.enabled);
        return trigger;
    }

    private static Condition buildCondition(Trigger trigger, String expression) {
        return new EventCondition(trigger.getTenantId(), trigger.getId(), NO_LONGER_USED, expression);
    }
}
