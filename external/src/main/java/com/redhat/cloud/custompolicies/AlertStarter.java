package com.redhat.cloud.custompolicies;

import io.quarkus.runtime.StartupEvent;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.hawkular.alerts.AlertsStandalone;
import org.hawkular.alerts.actions.QuarkusActionPluginRegister;
import org.hawkular.alerts.api.services.ActionsService;
import org.hawkular.alerts.api.services.AlertsService;
import org.hawkular.alerts.api.services.DefinitionsService;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

@ApplicationScoped
public class AlertStarter {
    private static final Logger LOGGER = LoggerFactory.getLogger("AlertStarter");

    @Inject
    AlertsStandalone alerts;

    @Inject
    QuarkusActionPluginRegister pluginRegister;

    @Produces
    public AlertsService getAlertService() {
        System.out.println("Returning getAlertsService instance: " + alerts.toString());
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
        LOGGER.info("Application created, starting CustomPolicy Engine.");
        initialize();
    }

//    @PostConstruct
    void initialize() {
//        ExecutorService executor = Executors.newCachedThreadPool();

//        alerts = new AlertsStandalone();
//        StandaloneAlerts.setExecutor(executor);
//        StandaloneAlerts.start();
        // PluginRegister is already using CDI, but we want it to initialize after Alerts has started
        pluginRegister.init();
        LOGGER.info("Started Hawkular Alerts");
    }
}
