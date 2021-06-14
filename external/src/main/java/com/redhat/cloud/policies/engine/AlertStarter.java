package com.redhat.cloud.policies.engine;

import com.redhat.cloud.policies.engine.actions.QuarkusActionPluginRegister;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import org.eclipse.microprofile.config.ConfigProvider;
import org.hawkular.alerts.AlertsStandalone;
import org.hawkular.alerts.api.services.ActionsService;
import org.hawkular.alerts.api.services.AlertsService;
import org.hawkular.alerts.api.services.DefinitionsService;
import org.hawkular.alerts.api.services.StatusService;
import org.hawkular.alerts.engine.impl.StatusServiceImpl;
import org.hawkular.alerts.log.MsgLogger;
import org.hawkular.alerts.log.MsgLogging;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hawkular.alerts.api.util.Util.isEmpty;

@Singleton
public class AlertStarter {
    public static MsgLogger LOGGER = MsgLogging.getMsgLogger(AlertStarter.class);
    private final static String BUILD_COMMIT_ENV_NAME = "OPENSHIFT_BUILD_COMMIT";

    @Inject
    AlertsStandalone alerts;

    @Inject
    QuarkusActionPluginRegister pluginRegister;

    @Inject
    StatusService statusService;

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
        printEnvVars();
        String commit = System.getenv(BUILD_COMMIT_ENV_NAME);
        if(!isEmpty(commit)) {
            LOGGER.infof("Starting Policies Engine, build: %s", commit);
        } else {
            LOGGER.info("Starting Policies Engine.");
        }
        initialize();
    }

    private void printEnvVars() {
        LOGGER.info("=== ENVIRONMENT VARIABLES - START ===");
        for (Map.Entry<String, String> var : System.getenv().entrySet()) {
            String value = isWhitelisted(var.getKey()) ? var.getValue() : "NOT_WHITELISTED";
            LOGGER.info(var.getKey() + "=" + value);
        }
        LOGGER.info("=== ENVIRONMENT VARIABLES - END ===");
        LOGGER.info("=== QUARKUS CONFIG - START ===");
        for (String key : ConfigProvider.getConfig().getPropertyNames()) {
            if (key.startsWith("engine")) {
                ConfigProvider.getConfig().getOptionalValue(key, String.class).ifPresent(value -> {
                    LOGGER.info(key + "=" + value);
                });
            }
        }
        LOGGER.info("=== QUARKUS CONFIG - END ===");
    }

    private static final List<String> PREFIX_WHITELIST = List.of(
            "QUARKUS_LOG_CLOUDWATCH_LOG_STREAM_NAME",
            "ENGINE",
            "HOSTNAME"
    );

    private boolean isWhitelisted(String key) {
        for (String prefix : PREFIX_WHITELIST) {
            if (key.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    void stopApp(@Observes ShutdownEvent shutdown) {
        LOGGER.info("Shutting down Policies Engine.");
        alerts.stop();
    }

    void initialize() {
        // PluginRegister is already using CDI, but we want it to initialize after Alerts has started
        alerts.init()
                .whenComplete((empty, error) -> {
                    if(error != null) {
                        LOGGER.error("Engine start failed", error);
                        alerts.stop();
                    } else {
                        pluginRegister.init();
                        ((StatusServiceImpl) statusService).setStarted(true);
                        LOGGER.info("Started Policies Engine");
                    }
        }).join();
    }
}
