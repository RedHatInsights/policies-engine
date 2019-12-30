package org.hawkular.alerts.engine;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.hawkular.alerts.api.services.ActionsService;
import org.hawkular.alerts.api.services.AlertsService;
import org.hawkular.alerts.api.services.DefinitionsService;
import org.hawkular.alerts.api.services.StatusService;
import org.hawkular.alerts.cache.IspnCacheManager;
import org.hawkular.alerts.engine.cache.ActionsCacheManager;
import org.hawkular.alerts.engine.cache.PublishCacheManager;
import org.hawkular.alerts.engine.impl.*;
import org.hawkular.alerts.engine.impl.ispn.IspnActionsServiceImpl;
import org.hawkular.alerts.engine.impl.ispn.IspnAlertsServiceImpl;
import org.hawkular.alerts.engine.impl.ispn.IspnDefinitionsServiceImpl;
import org.hawkular.alerts.filter.CacheClient;
import org.hawkular.commons.log.MsgLogger;
import org.hawkular.commons.log.MsgLogging;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.query.Search;
import org.infinispan.query.SearchManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Factory helper for standalone use cases.
 *
 * @author Lucas Ponce
 */
@Deprecated // Used only for old tests that haven't been ported yet
public class StandaloneAlerts {
    private static final MsgLogger log = MsgLogging.getMsgLogger(StandaloneAlerts.class);
    private static StandaloneAlerts instance;
    private static ExecutorService executor;

    @ConfigProperty(name = "engine.backend.ispn.reindex", defaultValue = "false")
    private boolean ispnReindex;

    private boolean distributed;

    private AlertsThreadFactory threadFactory;

    private ActionsCacheManager actionsCacheManager;
    private AlertsContext alertsContext;
    private AlertsEngineImpl engine;
    private CacheClient dataIdCache;
    private DataDrivenGroupCacheManager dataDrivenGroupCacheManager;
    private DroolsRulesEngineImpl rules;
    private EmbeddedCacheManager cacheManager;
    private ExtensionsServiceImpl extensions;
    private IncomingDataManagerImpl incoming;
    private IspnActionsServiceImpl ispnActions;
    private IspnAlertsServiceImpl ispnAlerts;
    private IspnDefinitionsServiceImpl ispnDefinitions;
    private StatusServiceImpl status;
    private PartitionManagerImpl partitionManager;
    private PublishCacheManager publishCacheManager;

    private StandaloneAlerts() {
        distributed = IspnCacheManager.isDistributed();
        cacheManager = IspnCacheManager.getCacheManager();

        threadFactory = new AlertsThreadFactory();
        if (executor == null) {
            executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), threadFactory);
        }

        dataIdCache = new CacheClient();
        rules = new DroolsRulesEngineImpl();
        engine = new AlertsEngineImpl();
        alertsContext = new AlertsContext();
        partitionManager = new PartitionManagerImpl();
        status = new StatusServiceImpl();
        extensions = new ExtensionsServiceImpl();
        dataDrivenGroupCacheManager = new DataDrivenGroupCacheManager();
        incoming = new IncomingDataManagerImpl();
        actionsCacheManager = new ActionsCacheManager();
        publishCacheManager = new PublishCacheManager();

        log.info("Hawkular Alerting uses Infinispan backend");

        if (ispnReindex) {
            log.info("Hawkular Alerting started with hawkular-alerts.backend-reindex=true");
            log.info("Reindexing Ispn [backend] started.");
            long startReindex = System.currentTimeMillis();
            SearchManager searchManager = Search
                    .getSearchManager(IspnCacheManager.getCacheManager().getCache("backend"));
            searchManager.getMassIndexer().start();
            long stopReindex = System.currentTimeMillis();
            log.info("Reindexing Ispn [backend] completed in [" + (stopReindex - startReindex) + " ms]");
        }

        ispnActions = new IspnActionsServiceImpl();
        ispnAlerts = new IspnAlertsServiceImpl();
        ispnDefinitions = new IspnDefinitionsServiceImpl();

        ispnActions.setActionsCacheManager(actionsCacheManager);
        ispnActions.setAlertsContext(alertsContext);
        ispnActions.setDefinitions(ispnDefinitions);

        ispnAlerts.setActionsService(ispnActions);
        ispnAlerts.setAlertsEngine(engine);
        ispnAlerts.setDefinitionsService(ispnDefinitions);
        ispnAlerts.setIncomingDataManager(incoming);

        ispnDefinitions.setAlertsEngine(engine);
        ispnDefinitions.setAlertsContext(alertsContext);

        actionsCacheManager.setDefinitions(ispnDefinitions);
        actionsCacheManager.setGlobalActionsCache(cacheManager.getCache("globalActions"));

        alertsContext.setPartitionManager(partitionManager);

        dataDrivenGroupCacheManager.setDefinitions(ispnDefinitions);

        dataIdCache.setCache(cacheManager.getCache("publish"));

        engine.setActions(ispnActions);
        engine.setAlertsService(ispnAlerts);
        engine.setDefinitions(ispnDefinitions);
        engine.setExecutor(executor);
        engine.setExtensionsService(extensions);
        engine.setPartitionManager(partitionManager);
        engine.setRules(rules);

        incoming.setAlertsEngine(engine);
        incoming.setDataDrivenGroupCacheManager(dataDrivenGroupCacheManager);
        incoming.setDataIdCache(dataIdCache);
        incoming.setDefinitionsService(ispnDefinitions);
        incoming.setExecutor(executor);
        incoming.setPartitionManager(partitionManager);

        partitionManager.setDefinitionsService(ispnDefinitions);

        actionsCacheManager.setDefinitions(ispnDefinitions);
        actionsCacheManager.setGlobalActionsCache(cacheManager.getCache("globalActions"));

        publishCacheManager.setDefinitions(ispnDefinitions);
        publishCacheManager.setPublishCache(cacheManager.getCache("publish"));
        publishCacheManager.setPublishDataIdsCache(cacheManager.getCache("dataIds"));

        status.setPartitionManager(partitionManager);

        // Initialization needs order

        ispnAlerts.init();
        ispnDefinitions.init();
        ispnActions.init();

        partitionManager.init();
        alertsContext.init();
        dataDrivenGroupCacheManager.init();
        actionsCacheManager.init();
        publishCacheManager.init();
        extensions.init();
        engine.initServices();
    }

    private static synchronized void init() {
        instance = new StandaloneAlerts();
    }

    public static ExecutorService getExecutor() {
        return executor;
    }

    public static void setExecutor(ExecutorService executor) {
        StandaloneAlerts.executor = executor;
    }

    public static void start() {
        init();
    }

    public static void stop() {
        if (instance != null) {
            instance.engine.shutdown();
            instance.partitionManager.shutdown();
            IspnCacheManager.stop();
            instance = null;
        }
    }

    public static DefinitionsService getDefinitionsService() {
        if (instance == null) {
            init();
        }
        return instance.ispnDefinitions;
    }

    public static AlertsService getAlertsService() {
        if (instance == null) {
            init();
        }
        return instance.ispnAlerts;
    }

    public static ActionsService getActionsService() {
        if (instance == null) {
            init();
        }
        return instance.ispnActions;
    }

    public static StatusService getStatusService() {
        if (instance == null) {
            init();
        }
        return instance.status;
    }

    public class AlertsThreadFactory implements ThreadFactory {
        private int count = 0;

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "HawkularAlerts-" + (++count));
        }
    }
}
