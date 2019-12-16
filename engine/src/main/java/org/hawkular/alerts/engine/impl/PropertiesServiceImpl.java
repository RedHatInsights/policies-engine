package org.hawkular.alerts.engine.impl;

import org.hawkular.alerts.api.services.PropertiesService;
import org.hawkular.commons.properties.HawkularProperties;

/**
 * A default implementation of PropertiesService.
 *
 *
 * @author Jay Shaughnessy
 * @author Lucas Ponce
 */
public class PropertiesServiceImpl implements PropertiesService {

    @Override
    public String getProperty(String key, String defaultValue) {
        return HawkularProperties.getProperty(key, defaultValue);
    }

    @Override
    public String getProperty(String key, String envKey, String defaultValue) {
        return HawkularProperties.getProperty(key, envKey, defaultValue);
    }
}
