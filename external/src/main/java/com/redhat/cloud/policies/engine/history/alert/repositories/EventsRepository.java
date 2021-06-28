package com.redhat.cloud.policies.engine.history.alert.repositories;

import com.redhat.cloud.policies.engine.history.alert.requests.EventsRequest;
import com.redhat.cloud.policies.engine.history.alert.entities.EventEntity;
import org.hibernate.Session;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class EventsRepository {

    private static final String BASE_SELECT_QUERY = "FROM EventEntity WHERE tenantId = :tenantId";
    private static final String BASE_DELETE_QUERY = "DELETE FROM EventEntity WHERE tenantId = :tenantId";

    @Inject
    Session session;

    public void save(EventEntity eventEntity) {
        session.persist(eventEntity);
    }

    public EventEntity find(String tenantId, String eventId) {
        String hqlQuery = "FROM EventEntity WHERE tenantId = :tenantId AND id = :eventId";
        return session.createQuery(hqlQuery, EventEntity.class)
                .setParameter("tenantId", tenantId)
                .setParameter("eventId", eventId)
                .uniqueResult();
    }

    public List<EventEntity> findAll(EventsRequest eventsRequest) {
        String hqlQuery = buildHqlQuery(BASE_SELECT_QUERY, eventsRequest);
        TypedQuery<EventEntity> typedQuery = session.createQuery(hqlQuery, EventEntity.class)
                .setParameter("tenantId", eventsRequest.getTenantId());
        setQueryParameters(typedQuery, eventsRequest);
        return typedQuery.getResultList();
    }

    public int delete(EventsRequest eventsRequest) {
        String hqlQuery = buildHqlQuery(BASE_DELETE_QUERY, eventsRequest);
        Query query = session.createQuery(hqlQuery)
                .setParameter("tenantId", eventsRequest.getTenantId());
        setQueryParameters(query, eventsRequest);
        return query.executeUpdate();
    }

    private static String buildHqlQuery(String baseQuery, EventsRequest eventsRequest) {
        List<String> conditions = new ArrayList<>();

        if (eventsRequest.getEventType() != null) {
            conditions.add("eventType = :eventType");
        }

        if (eventsRequest.getStartTime() != null && eventsRequest.getEndTime() != null) {
            conditions.add("ctime BETWEEN :startTime AND :endTime");
        } else if (eventsRequest.getStartTime() != null) {
            conditions.add("ctime >= :startTime");
        } else if (eventsRequest.getEndTime() != null) {
            conditions.add("ctime <= :endTime");
        }

        if (eventsRequest.getEventIds() != null && !eventsRequest.getEventIds().isEmpty()) {
            conditions.add("id.eventId IN (:eventIds)");
        }

        if (eventsRequest.getTriggerIds() != null && !eventsRequest.getTriggerIds().isEmpty()) {
            conditions.add("triggerId IN (:triggerIds)");
        }

        if (eventsRequest.getCategories() != null && !eventsRequest.getCategories().isEmpty()) {
            conditions.add("category IN (:categories)");
        }

        if (eventsRequest.getTagQuery() != null) {
            conditions.add(eventsRequest.getTagQuery());
        }

        if (conditions.isEmpty()) {
            return baseQuery;
        } else {
            return baseQuery + " WHERE " + String.join(" AND ", conditions);
        }
    }

    private static void setQueryParameters(Query query, EventsRequest eventsRequest) {

        if (eventsRequest.getEventType() != null) {
            query.setParameter("eventType", eventsRequest.getEventType());
        }

        if (eventsRequest.getStartTime() != null && eventsRequest.getEndTime() != null) {
            query.setParameter("startTime", eventsRequest.getStartTime()).setParameter("endTime", eventsRequest.getEndTime());
        } else if (eventsRequest.getStartTime() != null) {
            query.setParameter("startTime", eventsRequest.getStartTime());
        } else if (eventsRequest.getEndTime() != null) {
            query.setParameter("endTime", eventsRequest.getEndTime());
        }

        if (eventsRequest.getEventIds() != null && !eventsRequest.getEventIds().isEmpty()) {
            query.setParameter("eventIds", eventsRequest.getEventIds());
        }

        if (eventsRequest.getTriggerIds() != null && !eventsRequest.getTriggerIds().isEmpty()) {
            query.setParameter("triggerIds", eventsRequest.getTriggerIds());
        }

        if (eventsRequest.getCategories() != null && !eventsRequest.getCategories().isEmpty()) {
            query.setParameter("categories", eventsRequest.getCategories());
        }
    }
}
