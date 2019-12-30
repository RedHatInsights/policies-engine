package org.hawkular.alerts.engine.cache;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.hawkular.alerts.api.model.condition.CompareCondition;
import org.hawkular.alerts.api.model.condition.Condition;
import org.hawkular.alerts.api.model.trigger.TriggerKey;
import org.hawkular.alerts.api.services.DefinitionsService;
import org.hawkular.alerts.filter.CacheKey;
import org.hawkular.alerts.log.AlertingLogger;
import org.hawkular.commons.log.MsgLogging;
import org.infinispan.Cache;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.hawkular.alerts.api.services.DefinitionsEvent.Type.TRIGGER_CONDITION_CHANGE;
import static org.hawkular.alerts.api.services.DefinitionsEvent.Type.TRIGGER_REMOVE;
import static org.hawkular.alerts.api.util.Util.isEmpty;

/**
 * Manages the cache of globally active dataIds. Incoming Data and Events with Ids not in the cache will be filtered
 * away.  Only Data and Events with active Ids will be forwarded (published) to the engine for evaluation.
 *
 * This implementation design initialize the cache on each new node.
 * Definitions events are not propagated into the cluster as there is not a real use case for them and
 * it can overload the clustering traffic.
 * A coordinator strategy to initialize will not help either as each new node can became coordinator.
 * So, it is tradeoff to maintain an extra state or let each node initialize the publish* caches.
 *
 * @author Jay Shaughnessy
 * @author Lucas Ponce
 */
public class PublishCacheManager {
    private final AlertingLogger log = MsgLogging.getMsgLogger(AlertingLogger.class, PublishCacheManager.class);

    DefinitionsService definitions;

    @ConfigProperty(name = "engine.cache.disable-publish-filtering", defaultValue = "false")
    boolean disablePublish;

    @ConfigProperty(name = "engine.cache.reset-publish-cache", defaultValue = "true")
    boolean resetCache;

    // It stores a list of dataIds used per key (tenantId, triggerId).
    private Cache<TriggerKey, Set<String>> publishDataIdsCache;

    // It stores a list of triggerIds used per key (tenantId, dataId).
    // This cache is used by CacheClient to check which dataIds are published and forwarded from metrics.
    private Cache<CacheKey, Set<String>> publishCache;

    public void setDefinitions(DefinitionsService definitions) {
        this.definitions = definitions;
    }

    public void setPublishDataIdsCache(Cache<TriggerKey, Set<String>> publishDataIdsCache) {
        this.publishDataIdsCache = publishDataIdsCache;
    }

    public void setPublishCache(Cache<CacheKey, Set<String>> publishCache) {
        this.publishCache = publishCache;
    }

    public void init() {
        if (!disablePublish) {
            if (resetCache) {
                log.warnClearPublishCache();
                publishCache.clear();
                publishDataIdsCache.clear();
            }
            log.infoInitPublishCache();

            initialCacheUpdate();

            definitions.registerListener(events -> {
                log.debugf("Receiving %s", events);
                events.stream()
                        .forEach(e -> {
                            log.debugf("Received %s", e);
                            String tenantId = e.getTargetTenantId();
                            String triggerId = e.getTargetId();
                            TriggerKey triggerKey = new TriggerKey(tenantId, triggerId);
                            publishCache.startBatch();
                            publishDataIdsCache.startBatch();
                            switch (e.getType()) {
                                case TRIGGER_CONDITION_CHANGE: {
                                    Set<String> oldDataIds = publishDataIdsCache.getOrDefault(triggerKey,
                                            Collections.emptySet());
                                    Set<String> newDataIds = e.getDataIds();
                                    if (!oldDataIds.equals(newDataIds)) {
                                        removePublishCache(tenantId, triggerId, oldDataIds);
                                        addPublishCache(tenantId, triggerId, newDataIds);
                                        publishDataIdsCache.put(triggerKey, newDataIds);
                                    }
                                    break;
                                }
                                case TRIGGER_REMOVE: {
                                    Set<String> oldDataIds = publishDataIdsCache.get(triggerKey);
                                    removePublishCache(tenantId, triggerId, oldDataIds);
                                    publishDataIdsCache.remove(triggerKey);
                                    break;
                                }
                                default:
                                    throw new IllegalStateException("Unexpected notification: " + e.toString());
                            }
                            publishDataIdsCache.endBatch(true);
                            publishCache.endBatch(true);
                        });
            }, TRIGGER_CONDITION_CHANGE, TRIGGER_REMOVE);

        } else {
            log.warnDisabledPublishCache();
        }
    }

    private void removePublishCache(String tenantId, String triggerId, Set<String> dataIds) {
        if (!isEmpty(dataIds)) {
            dataIds.stream().forEach(dataId -> {
                CacheKey cacheKey = new CacheKey(tenantId, dataId);
                Set<String> triggerIds = publishCache.get(cacheKey);
                if (!isEmpty(triggerIds)) {
                    triggerIds.remove(triggerId);
                    if (triggerIds.isEmpty()) {
                        publishCache.remove(cacheKey);
                    } else {
                        publishCache.put(cacheKey, triggerIds);
                    }
                }
            });
        }
    }

    private void addPublishCache(String tenantId, String triggerId, Set<String> dataIds) {
        if (!isEmpty(dataIds)) {
            dataIds.stream().forEach(dataId -> {
                CacheKey cacheKey = new CacheKey(tenantId, dataId);
                Set<String> triggerIds = publishCache.get(cacheKey);
                if (triggerIds == null) {
                    triggerIds = new HashSet<>();
                }
                triggerIds.add(triggerId);
                publishCache.put(cacheKey, triggerIds);
            });
        }
    }

    private void initialCacheUpdate() {
        try {
            log.debug("Initial PublishCacheManager update in progress..");

            publishCache.startBatch();
            publishDataIdsCache.startBatch();
            // This will include group trigger conditions, which is OK because for data-driven group triggers the
            // dataIds will likely be the dataIds from the group level, made distinct by the source.
            Collection<Condition> conditions = definitions.getAllConditions();
            for (Condition c : conditions) {
                String triggerId = c.getTriggerId();
                TriggerKey triggerKey = new TriggerKey(c.getTenantId(), triggerId);
                Set<String> dataIds = new HashSet<>();
                dataIds.add(c.getDataId());
                if (c instanceof CompareCondition) {
                    String data2Id = ((CompareCondition) c).getData2Id();
                    dataIds.add(data2Id);
                }
                Set<String> prevDataIds = publishDataIdsCache.get(triggerKey);
                if (prevDataIds == null) {
                    prevDataIds = new HashSet<>();
                }
                prevDataIds.addAll(dataIds);
                publishDataIdsCache.put(triggerKey, prevDataIds);
                addPublishCache(c.getTenantId(), triggerId, dataIds);
            }
            log.debugf("Published after update=%s", publishCache.size());
            if (log.isDebugEnabled()) {
                publishCache.entrySet().stream().forEach(e -> log.debugf("Published: %s", e.getValue()));
            }
            publishDataIdsCache.endBatch(true);
            publishCache.endBatch(true);
        } catch (Exception e) {
            log.error("Failed to load conditions to create Id filters. All data being forwarded to alerting!", e);
            publishDataIdsCache.endBatch(false);
            publishCache.endBatch(false);
            return;
        }
    }
}
