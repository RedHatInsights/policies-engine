package org.hawkular.commons.log;

import org.jboss.logging.Logger;

/**
 *
 * Utility class to simplify logger lookup.
 *
 *
 * @author Thomas Segismont
 * @author Lucas Ponce
 */
public class MsgLogging {
    public static <T> T getMsgLogger(Class<T> loggerClass, Class<?> loggedClass) {
        return Logger.getMessageLogger(loggerClass, loggedClass.getName());
    }

    public static MsgLogger getMsgLogger(Class<?> loggedClass) {
        return Logger.getMessageLogger(MsgLogger.class, loggedClass.getName());
    }

    private MsgLogging() {
    }
}
