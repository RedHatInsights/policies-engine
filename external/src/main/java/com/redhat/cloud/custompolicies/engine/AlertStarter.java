package com.redhat.cloud.custompolicies.engine;

import com.redhat.cloud.custompolicies.engine.actions.QuarkusActionPluginRegister;
import io.quarkus.runtime.StartupEvent;
import org.hawkular.alerts.AlertsStandalone;
import org.hawkular.alerts.api.services.ActionsService;
import org.hawkular.alerts.api.services.AlertsService;
import org.hawkular.alerts.api.services.DefinitionsService;
import org.hawkular.commons.log.MsgLogger;
import org.hawkular.commons.log.MsgLogging;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

@ApplicationScoped
public class AlertStarter {
    public static MsgLogger LOGGER = MsgLogging.getMsgLogger(AlertStarter.class);

    @Inject
    AlertsStandalone alerts;

    @Inject
    QuarkusActionPluginRegister pluginRegister;

    @Produces
    public AlertsService getAlertService() {
        return alerts.getAlertsService();
    }

    @Produces
    public DefinitionsService getDefinitionsService() {
        return alerts.getDefinitionsService();
    }

    @Produces
    public ActionsService getActionsService() {
        return alerts.getActionsService();
    }

    void startApp(@Observes StartupEvent startup) {
        LOGGER.info("Application created, starting Custom Policies Engine.");
        initialize();
    }

//    @PostConstruct
    void initialize() {
        // PluginRegister is already using CDI, but we want it to initialize after Alerts has started
        pluginRegister.init();
        LOGGER.info("Started Custom Policies Engine");
    }
}
