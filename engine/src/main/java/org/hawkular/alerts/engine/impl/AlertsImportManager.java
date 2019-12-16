package org.hawkular.alerts.engine.impl;

import java.io.File;
import java.util.List;

import org.hawkular.alerts.api.model.action.ActionDefinition;
import org.hawkular.alerts.api.model.export.Definitions;
import org.hawkular.alerts.api.model.trigger.FullTrigger;
import org.hawkular.commons.log.MsgLogger;
import org.hawkular.commons.log.MsgLogging;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Read a json file with a list of full triggers and actions definitions.
 *
 * @author Jay Shaughnessy
 * @author Lucas Ponce
 */
public class AlertsImportManager {
    private static final MsgLogger log = MsgLogging.getMsgLogger(AlertsImportManager.class);
    private ObjectMapper objectMapper = new ObjectMapper();
    private Definitions definitions;

    /**
     * Read a json file and initialize the AlertsImportManager instance
     *
     * @param fAlerts json file to read
     * @throws Exception on any problem
     */
    public AlertsImportManager(File fAlerts) throws Exception {
        if (fAlerts == null) {
            throw new IllegalArgumentException("fAlerts must be not null");
        }
        if (!fAlerts.exists() || !fAlerts.isFile()) {
            throw new IllegalArgumentException(fAlerts.getName() + " file must exist");
        }

        definitions = objectMapper.readValue(fAlerts, Definitions.class);
        if (definitions != null) {
            log.debugf("File: %s imported in %s", fAlerts.toString(), definitions.toString());
        } else {
            log.debugf("File: %s imported is null", fAlerts.toString());
        }
    }

    public List<FullTrigger> getFullTriggers() {
        return (definitions != null ? definitions.getTriggers() : null);
    }

    public List<ActionDefinition> getActionDefinitions() {
        return (definitions != null ? definitions.getActions() : null);
    }

}
