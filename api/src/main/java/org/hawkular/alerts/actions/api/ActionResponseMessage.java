package org.hawkular.alerts.actions.api;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * A message sent to the alerts engine from the plugin
 * It defines a code of operation and generic payload.
 *
 * Payload is represented as a generic map of strings.
 *
 * @author Lucas Ponce
 */
public interface ActionResponseMessage {

    enum Operation {
        RESULT
    }

    @JsonInclude
    Operation getOperation();

    @JsonInclude
    Map<String, String> getPayload();
}
