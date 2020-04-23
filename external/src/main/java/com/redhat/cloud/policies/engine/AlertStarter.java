package com.redhat.cloud.policies.engine;

import com.redhat.cloud.policies.engine.actions.QuarkusActionPluginRegister;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import org.hawkular.alerts.AlertsStandalone;
import org.hawkular.alerts.api.services.ActionsService;
import org.hawkular.alerts.api.services.AlertsService;
import org.hawkular.alerts.api.services.DefinitionsService;
import org.hawkular.alerts.log.MsgLogger;
import org.hawkular.alerts.log.MsgLogging;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;

import static org.hawkular.alerts.api.util.Util.isEmpty;

@Singleton
public class AlertStarter {
    public static MsgLogger LOGGER = MsgLogging.getMsgLogger(AlertStarter.class);
    private final static String BUILD_COMMIT_ENV_NAME = "OPENSHIFT_BUILD_COMMIT";

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
        LOGGER.info("Application created, starting Policies Engine.");
        initialize();
    }

    void stopApp(@Observes ShutdownEvent shutdown) {
        LOGGER.info("Shutting down Policies Engine.");
        alerts.stop();
    }

//    @PostConstruct
    void initialize() {
        // PluginRegister is already using CDI, but we want it to initialize after Alerts has started
        pluginRegister.init();
        String commit = System.getenv(BUILD_COMMIT_ENV_NAME);
        if(!isEmpty(commit)) {
            LOGGER.infof("Started Policies Engine, build: %s", commit);
        } else {
            LOGGER.info("Started Policies Engine");
        }
    }
}
