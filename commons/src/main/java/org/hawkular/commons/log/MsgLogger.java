package org.hawkular.commons.log;


import org.jboss.logging.BasicLogger;
import org.jboss.logging.annotations.MessageLogger;

/**
 * Default Logger.
 * Each project can extend it with personalized messages.
 *
 * @author Jay Shaughnessy
 * @author Lucas Ponce
 */
@MessageLogger(projectCode = "HAWKULAR")
public interface MsgLogger extends BasicLogger {
}
