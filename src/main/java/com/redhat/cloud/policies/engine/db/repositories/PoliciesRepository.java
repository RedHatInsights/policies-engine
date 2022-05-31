package com.redhat.cloud.policies.engine.db.repositories;

import com.redhat.cloud.policies.engine.config.OrgIdConfig;
import com.redhat.cloud.policies.engine.db.StatelessSessionFactory;
import com.redhat.cloud.policies.engine.db.entities.Policy;
import io.quarkus.cache.CacheInvalidateAll;
import io.quarkus.cache.CacheResult;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class PoliciesRepository {

    private static final Logger LOGGER = Logger.getLogger(PoliciesRepository.class);
    private static final String ACCOUNT_LATEST_UPDATE_CACHE_NAME = "account-latest-update";

    private static class EnabledPolicies {
        private LocalDateTime latestUpdate;
        private List<Policy> policies;
    }

    @Inject
    StatelessSessionFactory statelessSessionFactory;

    private final Map</* accountId */ String, EnabledPolicies> enabledPoliciesCache = new HashMap<>();

    public List<Policy> getEnabledPolicies(String accountId) {
        EnabledPolicies enabledPolicies = enabledPoliciesCache.computeIfAbsent(accountId, unused -> new EnabledPolicies());
        LocalDateTime dbLatestUpdate = findLatestUpdate(accountId);
        if (enabledPolicies.latestUpdate == null || enabledPolicies.latestUpdate.isBefore(dbLatestUpdate)) {
            LOGGER.debug("Reloading enabled policies from DB");
            enabledPolicies.policies = findEnabledPoliciesByAccount(accountId);
            enabledPolicies.latestUpdate = dbLatestUpdate;
        }
        return enabledPolicies.policies;
    }

    public List<Policy> getEnabledPoliciesOrgId(String orgId) {
        EnabledPolicies enabledPolicies = enabledPoliciesCache.computeIfAbsent(orgId, unused -> new EnabledPolicies());
        LocalDateTime dbLatestUpdate = findLatestUpdateOrgId(orgId);
        if (enabledPolicies.latestUpdate == null || enabledPolicies.latestUpdate.isBefore(dbLatestUpdate)) {
            LOGGER.debug("Reloading enabled policies from DB");
            enabledPolicies.policies = findEnabledPoliciesByOrgId(orgId);
            enabledPolicies.latestUpdate = dbLatestUpdate;
        }
        return enabledPolicies.policies;
    }

    @CacheResult(cacheName = ACCOUNT_LATEST_UPDATE_CACHE_NAME)
    LocalDateTime findLatestUpdate(String accountId) {
        LOGGER.debugf("Finding latest policies update time for account %s", accountId);
        try {
            String hql = "SELECT latest FROM AccountLatestUpdate WHERE accountId = :accountId";
            return statelessSessionFactory.getCurrentSession().createQuery(hql, LocalDateTime.class)
                    .setParameter("accountId", accountId)
                    .getSingleResult();
        } catch (NoResultException e) {
            LOGGER.debug("No latest policies update time found, using default value");
            return LocalDateTime.MIN;
        }
    }

    @CacheResult(cacheName = ACCOUNT_LATEST_UPDATE_CACHE_NAME)
    LocalDateTime findLatestUpdateOrgId(String orgId) {
        LOGGER.debugf("Finding latest policies update time for account %s", orgId);
        try {
            String hql = "SELECT latest FROM OrgIdLatestUpdate WHERE orgId = :orgId";
            return statelessSessionFactory.getCurrentSession().createQuery(hql, LocalDateTime.class)
                    .setParameter("orgId", orgId)
                    .getSingleResult();
        } catch (NoResultException e) {
            LOGGER.debug("No latest policies update time found, using default value");
            return LocalDateTime.MIN;
        }
    }

    private List<Policy> findEnabledPoliciesByAccount(String accountId) {
        String hql = "FROM Policy WHERE accountId = :accountId AND enabled IS TRUE";
        return statelessSessionFactory.getCurrentSession().createQuery(hql, Policy.class)
                .setParameter("accountId", accountId)
                .getResultList();
    }

    private List<Policy> findEnabledPoliciesByOrgId(String orgId) {
        String hql = "FROM Policy WHERE orgId = :orgId AND enabled IS TRUE";
        return statelessSessionFactory.getCurrentSession().createQuery(hql, Policy.class)
                .setParameter("orgId", orgId)
                .getResultList();
    }

    @CacheInvalidateAll(cacheName = ACCOUNT_LATEST_UPDATE_CACHE_NAME)
    public void clearAllCaches() {
        LOGGER.debug("Clearing all caches");
        enabledPoliciesCache.clear();
    }
}
