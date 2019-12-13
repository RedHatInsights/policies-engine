/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.redhat.cloud.custompolicies.engine;

import io.quarkus.runtime.StartupEvent;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.hawkular.alerts.AlertsStandalone;
import com.redhat.cloud.custompolicies.engine.actions.QuarkusActionPluginRegister;
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
