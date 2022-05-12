package com.redhat.cloud.policies.engine.lightweight;

import io.quarkus.cache.CacheResult;
import org.hawkular.alerts.api.model.trigger.FullTrigger;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

@ApplicationScoped
public class TriggersLoader {

    private static final Logger LOGGER = Logger.getLogger(TriggersLoader.class);

    private static class AccountTriggers {
        private LocalDateTime latestUpdate;
        private List<FullTrigger> triggers;
    }

    private final Map</* accountId */ String, AccountTriggers> cache = new HashMap<>();

    @Inject
    EntityManager entityManager;

    public List<FullTrigger> getTriggers(String accountId) {
        AccountTriggers accountTriggers = cache.computeIfAbsent(accountId, unused -> new AccountTriggers());
        LocalDateTime dbLatestUpdate = findLatestUpdate(accountId);
        if (accountTriggers.latestUpdate == null || accountTriggers.latestUpdate.isBefore(dbLatestUpdate)) {
            accountTriggers.triggers = loadTriggersByAccount(accountId);
            accountTriggers.latestUpdate = dbLatestUpdate;
        }
        return accountTriggers.triggers;
    }

    @CacheResult(cacheName = "account-latest-update")
    LocalDateTime findLatestUpdate(String accountId) {
        LOGGER.debugf("Finding latest triggers update for account %s", accountId);
        String hql = "SELECT latest FROM AccountLatestUpdate WHERE accountId = :accountId";
        try {
            return entityManager.createQuery(hql, LocalDateTime.class)
                    .setParameter("accountId", accountId)
                    .getSingleResult();
        } catch (NoResultException e) {
            LOGGER.debugf("No latest triggers update found for account %s", accountId);
            return LocalDateTime.MIN;
        }
    }

    private List<FullTrigger> loadTriggersByAccount(String accountId) {
        String hql = "SELECT p FROM Policy p WHERE p.accountId = :accountId";
        List<FullTrigger> triggers = entityManager.createQuery(hql, Policy.class)
                .setParameter("accountId", accountId)
                .getResultList()
                .stream()
                .map(PolicyToTriggerConverter::convert)
                .collect(toList());
        LOGGER.debugf("%d database trigger(s) loaded for account %s", triggers.size(), accountId);
        return triggers;
    }
}
