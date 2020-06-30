package org.hawkular.alerts.engine.impl.ispn;

import org.eclipse.microprofile.config.ConfigProvider;
import org.hawkular.alerts.engine.cache.IspnCacheManager;
import org.hawkular.alerts.log.AlertingLogger;
import org.hawkular.alerts.log.MsgLogging;
import org.infinispan.Cache;
import org.infinispan.query.Search;
import org.infinispan.query.dsl.QueryFactory;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

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

    public String getDataStatistics() {
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


        StringBuilder builder = new StringBuilder();

        builder.append("Statistics from scanning Infinispan's backend cache:\n");
        builder.append(String.format("\tTotal key count: %d\n", stats.totalCount));
        builder.append(String.format("\tExpired events from key parsing: %d\n", stats.expiredEvents));
        builder.append(String.format("\tExpired actions from key parsing: %d\n", stats.expiredActions));

        builder.append("\tDetailed key counts:\n");
        for (Map.Entry<String, AtomicLong> ae : keyStats.entrySet()) {
            builder.append(String.format("\t\t%s\t\t%d\n", ae.getKey(), ae.getValue().get()));
        }
        builder.append("\n");

        return builder.toString();
    }
}
