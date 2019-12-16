package org.hawkular.alerts;

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
import org.hawkular.commons.properties.HawkularProperties;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.query.Search;
import org.infinispan.query.SearchManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Factory helper for standalone use cases.
 *
 * @author Lucas Ponce
 */
public class AlertsStandalone {
    private static final MsgLogger log = MsgLogging.getMsgLogger(AlertsStandalone.class);
    private static final String ISPN_BACKEND_REINDEX = "hawkular-alerts.backend-reindex";
    private static final String ISPN_BACKEND_REINDEX_DEFAULT = "false";
    private static ExecutorService executor;
    private static boolean ispnReindex;

    private boolean distributed;

//    private AlertsThreadFactory threadFactory;

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
    private PropertiesServiceImpl properties;
    private PublishCacheManager publishCacheManager;

    public AlertsStandalone() {
        distributed = IspnCacheManager.isDistributed();
        cacheManager = IspnCacheManager.getCacheManager();

//        threadFactory = new AlertsThreadFactory();
        if (executor == null) {
            executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        }

        dataIdCache = new CacheClient();
        rules = new DroolsRulesEngineImpl();
        engine = new AlertsEngineImpl();
        properties = new PropertiesServiceImpl();
        alertsContext = new AlertsContext();
        partitionManager = new PartitionManagerImpl();
        status = new StatusServiceImpl();
        extensions = new ExtensionsServiceImpl();
        dataDrivenGroupCacheManager = new DataDrivenGroupCacheManager();
        incoming = new IncomingDataManagerImpl();
        actionsCacheManager = new ActionsCacheManager();
        publishCacheManager = new PublishCacheManager();

        log.info("Hawkular Alerting uses Infinispan backend");
        ispnReindex = HawkularProperties.getProperty(ISPN_BACKEND_REINDEX, ISPN_BACKEND_REINDEX_DEFAULT).equals("true");

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
        ispnAlerts.setProperties(properties);

        ispnDefinitions.setAlertsEngine(engine);
        ispnDefinitions.setAlertsContext(alertsContext);
        ispnDefinitions.setProperties(properties);


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
        publishCacheManager.setProperties(properties);
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

    public void stop() {
            engine.shutdown();
            partitionManager.shutdown();
            IspnCacheManager.stop();
    }

    public DefinitionsService getDefinitionsService() {
        return ispnDefinitions;
    }

    public AlertsService getAlertsService() {
        return ispnAlerts;
    }

    public ActionsService getActionsService() {
        return ispnActions;
    }

    public StatusService getStatusService() {
        return status;
    }

}
