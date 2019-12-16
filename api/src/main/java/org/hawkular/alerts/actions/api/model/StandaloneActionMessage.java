package org.hawkular.alerts.actions.api.model;

import org.hawkular.alerts.actions.api.ActionMessage;
import org.hawkular.alerts.api.model.action.Action;

/**
 * @author Lucas Ponce
 */
public class StandaloneActionMessage implements ActionMessage {

    Action action;

    public StandaloneActionMessage(Action action) {
        this.action = action;
    }

    @Override
    public Action getAction() {
        return action;
    }

    @Override
    public String toString() {
        return "StandaloneActionMessage" + '[' +
                "action=" + action +
                ']';
    }
}
