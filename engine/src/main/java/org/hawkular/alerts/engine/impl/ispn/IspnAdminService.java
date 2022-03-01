package org.hawkular.alerts.engine.impl.ispn;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.NoLockFactory;
import org.eclipse.microprofile.config.ConfigProvider;
import org.hawkular.alerts.engine.cache.IspnCacheManager;
import org.hawkular.alerts.log.AlertingLogger;
import org.hawkular.alerts.log.MsgLogging;
import org.infinispan.Cache;
import org.infinispan.persistence.manager.PersistenceManager;
import org.infinispan.persistence.rocksdb.RocksDBStore;
import org.infinispan.query.Search;
import org.infinispan.query.dsl.QueryFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import static java.time.ZoneOffset.UTC;

public class IspnAdminService {

    private final AlertingLogger log = MsgLogging.getMsgLogger(AlertingLogger.class, IspnAdminService.class);
    Cache<String, Object> backend;
    QueryFactory queryFactory;

    public void init() {
        backend = IspnCacheManager.getCacheManager().getCache("backend");
        if (backend == null) {
            log.error("Ispn backend cache not found. Check configuration.");
            throw new RuntimeException("backend cache not found");
        }
        queryFactory = Search.getQueryFactory(backend);
    }

    private long getAlertLatestExpireTime() {
        long alertsLifespanInHours = ConfigProvider.getConfig().getValue("engine.backend.ispn.alerts-lifespan", Long.class);

        Date currentDate = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(currentDate);
        c.add(10, (int) (alertsLifespanInHours * -1));
        Date maxExpireTimeDate = c.getTime();
        return maxExpireTimeDate.getTime();
    }

    public void deleteExpiredKeys() {
        long maxExpireTimeForAlerts = getAlertLatestExpireTime();
        // Calculate here expired time etc.. since we can't access RocksDB directly from here
        backend.keySet().stream()
                .filter(s -> s.startsWith("Action-"))
                .forEach(sKey -> {
                    String[] splitParts = sKey.split("-");
                    try {
                        long ctime = Long.valueOf(splitParts[splitParts.length - 1]);
                        if (ctime < maxExpireTimeForAlerts) {
                            backend.remove(sKey);
                        }
                    } catch (Exception var34) {
                    }
                });
    }

    public void printDataStatistics() {
        final long maxExpireTimeForAlerts = getAlertLatestExpireTime();
        final Map<String, AtomicLong> keyStats = new HashMap<>();
        // var allows accessing these params inside lambda
        var stats = new Object() {
            long totalCount = 0L;
            long expiredEvents = 0L;
            long expiredActions = 0;
        };
        backend.keySet().forEach(sKey -> {
            stats.totalCount++;
            String[] splitParts = sKey.split("-");
            String subKey = splitParts[0];
            if(splitParts.length < 2) {
                subKey = "Unknown";
            }
            if(keyStats.containsKey(subKey)) {
                keyStats.get(subKey).addAndGet(1);
            } else {
                keyStats.put(subKey, new AtomicLong(1));
            }

            if (subKey.equals("Event")) {
                if (splitParts.length - 6 > 0) {
                    try {
                        long ctime = Long.valueOf(splitParts[splitParts.length - 6]);
                        if (ctime < maxExpireTimeForAlerts) {
                            ++stats.expiredEvents;
                        }
                    } catch (Exception var34) {
                    }
                }
            }

            if(subKey.equals("Action")) {
                try {
                    long ctime = Long.valueOf(splitParts[splitParts.length - 1]);
                    if (ctime < maxExpireTimeForAlerts) {
                        ++stats.expiredActions;
                    }
                } catch (Exception var34) {
                }
            }
        });


        log.info("Statistics from scanning Infinispan's backend cache:");
        log.infof("\tTotal key count: %d", stats.totalCount);
        log.infof("\tExpired events from key parsing: %d", stats.expiredEvents);
        log.infof("\tExpired actions from key parsing: %d", stats.expiredActions);

        log.info("\tDetailed key counts:");
        for (Map.Entry<String, AtomicLong> ae : keyStats.entrySet()) {
            log.infof("\t\t%s\t\t%d", ae.getKey(), ae.getValue().get());
        }
    }

    public void executeRocksDBCompaction() throws Exception {
        log.info("Executing manual compaction in RocksDB");
        PersistenceManager component = backend.getAdvancedCache().getComponentRegistry().getComponent(PersistenceManager.class);
        Set<RocksDBStore> stores = component.getStores(RocksDBStore.class);
        log.infof("Starting to compact %d stores", stores.size());
        for (RocksDBStore store : stores) {
            try {
                store.compact();
            } catch(Throwable t) {
                log.error("Failed to execute compaction", t);
            }
        }
        log.info("Finished RocksDB Compaction");
    }

    public void deleteIndexedEventsBefore(int ageInDays) {
        log.infof("Executing Lucene 'event' index cleaning. Events older than %d day(s) will be removed from the index.", ageInDays);
        try {
            try (
                    Directory directory = FSDirectory.open(Path.of(System.getProperty("hawkular.data") + "/alerting/event"), NoLockFactory.INSTANCE);
                    IndexWriter indexWriter = new IndexWriter(directory, new IndexWriterConfig(new StandardAnalyzer()))
            ) {
                String before = String.valueOf(LocalDateTime.now().minusDays(ageInDays).toEpochSecond(UTC));
                Query query = TermRangeQuery.newStringRange("ctime", null, before, false, false);
                indexWriter.deleteDocuments(query);
            }
        } catch (IOException e) {
            log.error("Failed to clean Lucene 'event' index", e);
            throw new UncheckedIOException(e);
        }
        log.info("Finished cleaning Lucene 'event' index");
    }
}
