package org.hawkular.alerts.api.services;

/**
 * A interface used to share alerts properties across several components.
 *
 * @author Jay Shaughnessy
 * @author Lucas Ponce
 */
public interface PropertiesService {

    /**
     * @param key of the property to retrieve
     * @param defaultValue default value to return in case the property has not a value defined
     * @return the value of the property
     */
    String getProperty(String key, String defaultValue);

    /**
     * @param key of the property to retrieve
     * @param envKey name of the environment variable used as an alternative way to define a property
     * @param defaultValue default value to return in case the property has not a value defined
     * @return the value of the property
     */
    String getProperty(String key, String envKey, String defaultValue);
}
