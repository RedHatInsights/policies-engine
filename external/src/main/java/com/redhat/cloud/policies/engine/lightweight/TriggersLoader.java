package com.redhat.cloud.policies.engine.lightweight;

import io.quarkus.cache.CacheResult;
import org.hawkular.alerts.api.model.trigger.FullTrigger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

@ApplicationScoped
public class TriggersLoader {

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
        String hql = "SELECT latest FROM AccountLatestUpdate WHERE accountId = :accountId";
        return entityManager.createQuery(hql, LocalDateTime.class)
                .setParameter("accountId", accountId)
                .getSingleResult();
    }

    public List<FullTrigger> loadTriggersByAccount(String accountId) {
        String hql = "SELECT p FROM Policy p WHERE p.accountId = :accountId";
        return entityManager.createQuery(hql, Policy.class)
                .setParameter("accountId", accountId)
                .getResultList()
                .stream()
                .map(PolicyToTriggerConverter::convert)
                .collect(toList());
    }
}
