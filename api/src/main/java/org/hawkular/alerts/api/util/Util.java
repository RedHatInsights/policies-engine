package org.hawkular.alerts.api.util;

import java.util.Collection;
import java.util.Map;

import com.google.common.collect.Multimap;
import org.hawkular.alerts.api.model.action.ActionDefinition;
import org.hawkular.alerts.api.model.dampening.Dampening;
import org.hawkular.alerts.api.model.trigger.Trigger;

/**
 * Unify some utility methods used in several components related to API model or generic validations
 *
 * @author Jay Shaughnessy
 * @author Lucas Ponce
 */
public class Util {

    public static boolean isEmpty(String s) {
        return null == s || s.trim().isEmpty();
    }

    public static boolean isEmpty(Collection c) {
        return null == c || c.isEmpty();
    }

    public static boolean isEmpty(Map m) {
        return m == null || m.isEmpty();
    }

    public static boolean isEmpty(Multimap m) {
        return m == null || m.isEmpty();
    }

    public static boolean isEmpty(String... strings) {
        for (String s : strings) {
            if (null == s || s.trim().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public static boolean isEmpty(ActionDefinition a) {
        return a == null || isEmpty(a.getActionPlugin()) || isEmpty(a.getActionId());
    }

    public static boolean isEmpty(Dampening dampening) {
        return dampening == null || isEmpty(dampening.getTriggerId()) || isEmpty(dampening.getDampeningId());
    }

    public static boolean isEmpty(Trigger trigger) {
        return trigger == null || isEmpty(trigger.getId());
    }

}
