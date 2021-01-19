package org.hawkular.alerts.engine.cache;

import org.hawkular.alerts.log.MsgLogger;
import org.hawkular.alerts.log.MsgLogging;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Load the DefaultCacheManager from infinispan
 *
 * @author Jay Shaughnessy
 * @author Lucas Ponce
 */
public class IspnCacheManager {
    private static final MsgLogger log = MsgLogging.getMsgLogger(IspnCacheManager.class);
    private static final String CONFIG_PATH = "hawkular.configuration";
    private static final String ISPN_CONFIG_DISTRIBUTED = "ispn-alerting-distributed.xml";
    private static final String ISPN_CONFIG_LOCAL = "ispn-alerting-local.xml";
    private static final String ALERTS_DISTRIBUTED = "hawkular-alerts.distributed";
    private static final String ALERTS_DISTRIBUTED_ENV = "HAWKULAR_ALERTS_DISTRIBUTED";
    private static final String ALERTS_DISTRIBUTED_DEFAULT = "false";

    private static EmbeddedCacheManager cacheManager = null;
    private static boolean distributed = false;

    public static EmbeddedCacheManager getCacheManager() {
        if (cacheManager == null) {
            init();
        }
        return cacheManager;
    }

    public static boolean isDistributed() {
        return distributed;
    }

    public static void stop() {
        if (cacheManager != null) {
            cacheManager.stop();
            cacheManager = null;
        }
    }

    private static synchronized void init() {
        if (cacheManager == null) {
            try {
                distributed = false;
                String configPath = System.getProperty(CONFIG_PATH);

                if (configPath != null) {
                    File configFile = new File(configPath, ISPN_CONFIG_LOCAL);
                    try (InputStream is = new FileInputStream(configFile)) {
                        cacheManager = new DefaultCacheManager(is);
                        return;
                    }
                }
                InputStream is = IspnCacheManager.class.getResourceAsStream("/" + ISPN_CONFIG_LOCAL);
                cacheManager = new DefaultCacheManager(is);
            } catch (IOException e) {
                log.error(e);
            }
        }
    }
}
