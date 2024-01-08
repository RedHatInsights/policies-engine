package com.redhat.cloud.policies.engine.db.repositories;

import com.redhat.cloud.policies.engine.TestLifecycleManager;
import com.redhat.cloud.policies.engine.db.StatelessSessionFactory;
import com.redhat.cloud.policies.engine.db.entities.Policy;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;

import org.hibernate.Session;
import org.junit.jupiter.api.Test;


import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
@QuarkusTestResource(TestLifecycleManager.class)
class PoliciesRepositoryTest {

    @InjectSpy
    StatelessSessionFactory statelessSessionFactory;

    @Inject
    Session session;

    @Inject
    PoliciesRepository repository;

    @Test
    public void testGetEnabledPolicies() {
        String orgId = UUID.randomUUID().toString();

        Policy policyFixture = createEnabledPolicy(orgId);

        statelessSessionFactory.withSession(session -> {
            List<Policy> enabledPolicies = repository.getEnabledPolicies(orgId);
            assertEquals(1, enabledPolicies.size());
            assertEquals(orgId, enabledPolicies.get(0).orgId);
            assertEquals(policyFixture.id, enabledPolicies.get(0).id);
        });
    }

    @Transactional
    Policy createEnabledPolicy(String orgId) {
        Policy policy = new Policy();
        policy.id = UUID.randomUUID();
        policy.accountId = "123";
        policy.orgId = orgId;
        policy.name = "Test";
        policy.description = "desc";
        policy.condition = "true";
        policy.enabled = true;
        policy.actions = "none";
        session.persist(policy);

        return policy;
    }
}
