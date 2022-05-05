package com.redhat.cloud.policies.engine.lightweight;

import org.hawkular.alerts.api.model.trigger.FullTrigger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class TriggerRepository {

    @Inject
    EntityManager entityManager;

    public List<FullTrigger> findAll() {
        String hql = "SELECT p FROM Policy p";
        return entityManager.createQuery(hql, Policy.class)
                .getResultList()
                .stream()
                .map(PolicyToTriggerConverter::convert)
                .collect(Collectors.toList());
    }

    public Map<UUID, FullTrigger> findByAccountAndIds(String accountId, Set<UUID> triggerIds) {
        String hql = "SELECT p FROM Policy p WHERE p.id IN (:triggerIds) AND p.accountId = :accountId";
        return entityManager.createQuery(hql, Policy.class)
                .setParameter("triggerIds", triggerIds)
                .setParameter("accountId", accountId)
                .getResultList()
                .stream()
                .collect(Collectors.toMap(
                        policy -> policy.id,
                        PolicyToTriggerConverter::convert
                ));
    }
}
