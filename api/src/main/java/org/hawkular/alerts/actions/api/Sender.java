package org.hawkular.alerts.actions.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark an ActionPluginSender to be injected inside an ActionPluginListener.
 * Only one injected ActionPluginSender instance is permitted by ActionPluginListener class.
 *
 * @author Jay Shaughnessy
 * @author Lucas Ponce
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Sender {
}
