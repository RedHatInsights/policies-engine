/*
 * Copyright 2015-2017 Red Hat, Inc. and/or its affiliates
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
package org.hawkular.alerts.actions;

import org.hawkular.alerts.actions.api.ActionMessage;
import org.hawkular.alerts.actions.api.ActionPluginListener;
import org.hawkular.alerts.actions.api.model.StandaloneActionMessage;
import org.hawkular.alerts.api.model.action.Action;
import org.hawkular.alerts.api.services.ActionListener;
import org.hawkular.alerts.api.services.DefinitionsService;
import org.hawkular.alerts.log.AlertingLogger;
import org.hawkular.commons.log.MsgLogging;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@ApplicationScoped
public class QuarkusActionPluginListener implements ActionListener {
    private static final AlertingLogger log = MsgLogging.getMsgLogger(AlertingLogger.class, QuarkusActionPluginListener.class);

    @Inject
    DefinitionsService definitions;

    ExecutorService executorService;

    private Map<String, ActionPluginListener> plugins;

    public QuarkusActionPluginListener() {
        this.plugins = new HashMap<>();
        this.executorService = Executors.newCachedThreadPool();
    }

    public void addPlugin(String pluginKey, ActionPluginListener listener) {
        this.plugins.put(pluginKey, listener);
    }

    @Override
    public void process(Action action) {
        try {
            if (plugins.isEmpty()) {
                log.warnNoPluginsFound();
                return;
            }
            if (action == null || action.getActionPlugin() == null) {
                log.warnMessageReceivedWithoutPluginInfo();
                return;
            }
            String actionPlugin = action.getActionPlugin();
            final ActionPluginListener plugin = plugins.get(actionPlugin);
            if (plugin == null) {
                if (log.isDebugEnabled()) {
                    log.debugf("Received action [%s] but no ActionPluginListener found on this deployment", actionPlugin);
                }
                return;
            }

            ActionMessage pluginMessage = new StandaloneActionMessage(action);
            try {
                plugin.process(pluginMessage);
            } catch (Exception e) {
                log.debugf("Error processing action: %s", action.getActionPlugin(), e);
                log.errorProcessingAction(e.getMessage());
            }
        } catch (Exception e) {
            log.debugf("Error processing action: %s", action.getActionPlugin(), e);
            log.errorProcessingAction(e.getMessage());
        }
    }

    public void close() {
        if (executorService != null) {
            executorService.shutdown();
        }
    }

    @Override
    public String toString() {
        return new StringBuilder("StandaloneActionPluginListener - [")
                .append(String.join(",", plugins.keySet()))
                .append("] plugins")
                .toString();
    }
}
