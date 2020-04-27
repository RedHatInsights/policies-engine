package org.hawkular.alerts.engine.impl;

import org.hawkular.alerts.api.services.StatusService;
import org.hawkular.alerts.engine.cache.IspnCacheManager;
import org.hawkular.alerts.engine.service.PartitionManager;
import org.infinispan.health.CacheHealth;
import org.infinispan.health.Health;

import java.util.HashMap;
import java.util.Map;

import static org.infinispan.health.HealthStatus.HEALTHY;

/**
 * An implementation of {@link org.hawkular.alerts.api.services.StatusService}.
 *
 * @author Jay Shaughnessy
 * @author Lucas Ponce
 */
public class StatusServiceImpl implements StatusService {

    private boolean started = false;

    private Map<String, String> statusNotes;

    PartitionManager partitionManager;

    public StatusServiceImpl() {
        this.statusNotes = new HashMap<>();
    }

    public void setPartitionManager(PartitionManager partitionManager) {
        this.partitionManager = partitionManager;
    }

    @Override
    public boolean isStarted() {
        return this.started;
    }

    @Override
    public boolean isHealthy() {
        // TODO Add status of Kafka connections (once Smallrye Reactive Messaging exposes them)
        return isStarted() && isInfinispanHealthy();
    }

    @Override
    public Map<String, String> getAdditionalStatus() {
        return this.statusNotes;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }

    public void setReindexing(boolean reindexing) {
        if(reindexing) {
            statusNotes.put("reindex", "true");
        } else {
            statusNotes.remove("reindex");
        }
    }

    public boolean isInfinispanHealthy() {
        boolean healthy = true;
        // Infinispan health
        if(!isDistributed()) {
            Health health = IspnCacheManager.getCacheManager().getHealth();
            // TODO Add HEALTHY_REBALANCING also and add to extra status
            healthy &= health.getClusterHealth().getHealthStatus() == HEALTHY;
            for (CacheHealth cacheHealth : health.getCacheHealth()) {
                healthy &= cacheHealth.getStatus() == HEALTHY;
            }
        }

        return healthy;
    }

    @Override
    public boolean isDistributed() {
        return partitionManager.isDistributed();
    }

    @Override
    public Map<String, String> getDistributedStatus() {
        return partitionManager.getStatus();
    }
}
