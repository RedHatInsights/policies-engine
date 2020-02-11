package org.hawkular.alerts.actions.api;

import javax.inject.Qualifier;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Define an alerts actions plugin implementation
 * Plugin must have a unique name that will be used at registration phase
 * Plugin must implement ActionPluginListener interface
 *
 * @author Lucas Ponce
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Qualifier
public @interface Plugin {
    String name();
}
