package org.hawkular.alerts.actions.api;

import org.hawkular.alerts.api.model.action.Action;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * A message sent to the plugin from the alerts engine
 * It has the alert payload as well as action properties
 *
 * @author Lucas Ponce
 */
public interface ActionMessage {

    @JsonInclude
    Action getAction();
}
