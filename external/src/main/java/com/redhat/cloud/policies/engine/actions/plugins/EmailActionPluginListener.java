package com.redhat.cloud.policies.engine.actions.plugins;

import org.hawkular.alerts.actions.api.ActionMessage;
import org.hawkular.alerts.actions.api.ActionPluginListener;
import org.hawkular.alerts.actions.api.Plugin;

import javax.enterprise.context.Dependent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Dummy PluginListener, QuarkusActionPluginListener reroutes calls to this plugin to "notification"
 */
@Plugin(name = "email")
@Dependent
public class EmailActionPluginListener implements ActionPluginListener {

    @Override
    public void process(ActionMessage actionMessage) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void flush() {
        throw new UnsupportedOperationException();
    }


    @Override
    public Set<String> getProperties() {
        return new HashSet<>();
    }

    @Override
    public Map<String, String> getDefaultProperties() {
        Map<String, String> defaultProperties = new HashMap<>();
        defaultProperties.put("_managed", "true");
        return defaultProperties;
    }
}
