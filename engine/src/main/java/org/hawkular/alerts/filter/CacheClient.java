package org.hawkular.alerts.filter;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import org.hawkular.alerts.api.model.data.Data;
import org.hawkular.alerts.api.model.event.Event;
import org.infinispan.Cache;

/**
 * Provide access to the cache of dataIds in use by the global trigger population (not node specific). It is
 * used to perform front-line filtering of incoming data and events.  Data with dataIds not found in this cache
 * can be immediately discarded as it is not needed for trigger evaluation (on this or or other alerting nodes).
 *
 * The cache is a shared ISPN cache.
 *
 * @author Lucas Ponce
 * @author Jay Shaughnessy
 */
public class CacheClient {

    // It stores a list of triggerIds used per key (tenantId, dataId).
    // This cache is used by CacheClient to check wich dataIds are published and forwarded from metrics.
    private Cache<CacheKey, Set<String>> cache;

    public void setCache(Cache<CacheKey, Set<String>> cache) {
        this.cache = cache;
    }

    public Set<CacheKey> keySet() {
        return cache.keySet();
    }

    public boolean containsKey(CacheKey key) {
        return cache.containsKey(key);
    }

    public Set<String> get(CacheKey key) {
        return cache.get(key);
    }

    public Collection<Data> filterData(Collection<Data> data) {
        final CacheKey tester = new CacheKey("", "");
        return data.stream()
                .filter(d -> cache.containsKey(fillKey(tester, d)))
                .collect(Collectors.toList());
    }

    public Collection<Event> filterEvents(Collection<Event> events) {
        final CacheKey tester = new CacheKey("", "");
        return events.stream()
                .filter(e -> cache.containsKey(fillKey(tester, e)))
                .collect(Collectors.toList());
    }

    private CacheKey fillKey(CacheKey key, Data data) {
        key.setTenantId(data.getTenantId());
        key.setDataId(data.getId());
        return key;
    }

    private CacheKey fillKey(CacheKey key, Event event) {
        key.setTenantId(event.getTenantId());
        key.setDataId(event.getDataId());
        return key;
    }

    /**
     *  This is here for testing purposes only and should not be called in production code.
     */
    public void addTestKey(CacheKey key, Set<String> value) {
        cache.put(key, value);
    }
}
