package com.redhat.cloud.custompolicies.engine.actions;

import org.hawkular.alerts.actions.api.ActionPluginListener;
import org.hawkular.alerts.actions.api.Plugin;
import org.hawkular.alerts.api.services.ActionsService;
import org.hawkular.alerts.api.services.DefinitionsService;
import org.hawkular.alerts.log.AlertingLogger;
import org.hawkular.commons.log.MsgLogging;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static org.hawkular.alerts.api.util.Util.isEmpty;

@ApplicationScoped
public class QuarkusActionPluginRegister {

    private static final AlertingLogger log = MsgLogging.getMsgLogger(AlertingLogger.class, QuarkusActionPluginRegister.class);

    @Inject
    DefinitionsService definitions;

    @Inject
    ActionsService actions;

    @Inject
    QuarkusActionPluginListener actionListener;

    @Inject @Any
    Instance<ActionPluginListener> pluginListeners;

    private void addPlugin(String actionPlugin, ActionPluginListener actionPluginListener) {
        Collection<String> existingPlugins = Collections.EMPTY_LIST;
        try {
             existingPlugins = definitions.getActionPlugins();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!existingPlugins.contains(actionPlugin)) {
            Set<String> properties = actionPluginListener.getProperties();
            Map<String, String> defaultProperties = actionPluginListener.getDefaultProperties();
            try {
                if (!isEmpty(defaultProperties)) {
                    definitions.addActionPlugin(actionPlugin, defaultProperties);
                } else {
                    definitions.addActionPlugin(actionPlugin, properties);
                }
            } catch (Exception e) {
                log.errorCannotRegisterPlugin(actionPlugin, e.toString());
            }
        } else {
            log.infof("Plugin [%s] is already registered", actionPlugin);
        }

        actionListener.addPlugin(actionPlugin, actionPluginListener);
        log.infof("Registered plugin %s implemented by %s", actionPlugin, actionPluginListener.getClass());
    }

    public void init() {
        for (ActionPluginListener pluginListener : pluginListeners) {
            Class<? extends ActionPluginListener> aClass = pluginListener.getClass();
            Plugin annotation = aClass.getAnnotation(Plugin.class);
            for (Annotation declaredAnnotation : pluginListener.getClass().getDeclaredAnnotations()) {
                if(declaredAnnotation instanceof Plugin) {
                    Plugin pluginAnnotation = (Plugin) declaredAnnotation;
                    addPlugin(pluginAnnotation.name(), pluginListener);
                    log.infof(" of: %s", pluginListener);
                }
            }
        }

        actions.addListener(actionListener);
        log.info("Quarkus Actions Plugins registration finished");
    }
}
