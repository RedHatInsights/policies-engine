package com.redhat.cloud.policies.engine.db.repositories;

import com.redhat.cloud.policies.engine.db.StatelessSessionFactory;
import com.redhat.cloud.policies.engine.db.entities.Policy;
import io.quarkus.cache.CacheInvalidateAll;
import io.quarkus.cache.CacheResult;
import io.quarkus.logging.Log;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class PoliciesRepository {

    private static final String ORG_ID_LATEST_UPDATE_CACHE_NAME = "org-id-latest-update";

    private static class EnabledPolicies {
        private LocalDateTime latestUpdate;
        private List<Policy> policies;
    }

    @Inject
    StatelessSessionFactory statelessSessionFactory;

    private final Map</* orgId */ String, EnabledPolicies> enabledPoliciesCache = new HashMap<>();

    public List<Policy> getEnabledPolicies(String orgId) {
        EnabledPolicies enabledPolicies = enabledPoliciesCache.computeIfAbsent(orgId, unused -> new EnabledPolicies());
        LocalDateTime dbLatestUpdate = findLatestUpdate(orgId);
        if (enabledPolicies.latestUpdate == null || enabledPolicies.latestUpdate.isBefore(dbLatestUpdate)) {
            Log.debug("Reloading enabled policies from DB");
            enabledPolicies.policies = findEnabledPolicies(orgId);
            enabledPolicies.latestUpdate = dbLatestUpdate;
        }
        return enabledPolicies.policies;
    }

    @CacheResult(cacheName = ORG_ID_LATEST_UPDATE_CACHE_NAME)
    LocalDateTime findLatestUpdate(String orgId) {
        Log.debugf("Finding latest policies update time for orgId %s", orgId);
        try {
            String hql = "SELECT latest FROM OrgIdLatestUpdate WHERE orgId = :orgId";
            return statelessSessionFactory.getCurrentSession().createQuery(hql, LocalDateTime.class)
                    .setParameter("orgId", orgId)
                    .getSingleResult();
        } catch (NoResultException e) {
            Log.debug("No latest policies update time found, using default value");
            return LocalDateTime.MIN;
        }
    }

    private List<Policy> findEnabledPolicies(String orgId) {
        String hql = "FROM Policy WHERE orgId = :orgId AND enabled IS TRUE";
        return statelessSessionFactory.getCurrentSession().createQuery(hql, Policy.class)
                .setParameter("orgId", orgId)
                .getResultList();
    }

    @CacheInvalidateAll(cacheName = ORG_ID_LATEST_UPDATE_CACHE_NAME)
    public void clearAllCachesOrgId() {
        Log.debug("Clearing all caches");
        enabledPoliciesCache.clear();
    }
}
