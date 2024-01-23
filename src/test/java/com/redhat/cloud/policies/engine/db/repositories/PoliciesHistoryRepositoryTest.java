package com.redhat.cloud.policies.engine.db.repositories;

import com.redhat.cloud.policies.engine.TestLifecycleManager;
import com.redhat.cloud.policies.engine.db.StatelessSessionFactory;
import com.redhat.cloud.policies.engine.db.entities.PoliciesHistoryEntry;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import org.hibernate.Session;
import org.junit.jupiter.api.Test;

import com.redhat.cloud.policies.engine.process.Event;

import jakarta.inject.Inject;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
@QuarkusTestResource(TestLifecycleManager.class)
class PoliciesHistoryRepositoryTest {

    @InjectSpy
    StatelessSessionFactory statelessSessionFactory;

    @Inject
    Session session;

    @Inject
    PoliciesHistoryRepository repository;

    @Test
    public void testHostGroupsPersistence() {
        var orgId = UUID.randomUUID().toString();
        var event = new Event(UUID.randomUUID().toString(), UUID.randomUUID().toString(), orgId, "category", "text");

        List<Object> groups = List.of(JsonObject.of("name", "group_one",
                                                    "id", "00000000-0000-0000-0000-000000000001"));
        event.addHostGroups(groups);

        statelessSessionFactory.withSession(session -> {
            repository.create(UUID.randomUUID(), event);
        });

        var hql = "FROM PoliciesHistoryEntry WHERE orgId = :orgId";
        var query = session.createQuery(hql, PoliciesHistoryEntry.class)
                           .setParameter("orgId", orgId);
        PoliciesHistoryEntry entry = query.getSingleResult();
        assertNotNull("PoliciesHistoryEntry not found", entry);
        assertEquals(orgId, entry.getOrgId());
        assertEquals(new JsonArray(groups), entry.getHostGroups());
    }
}
