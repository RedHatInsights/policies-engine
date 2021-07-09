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
import java.util.regex.Pattern;

@ApplicationScoped
public class EventsRepository {

    private static final String BASE_SELECT_QUERY = "SELECT e FROM EventEntity e";
    private static final String BASE_DELETE_QUERY = "DELETE FROM EventEntity e";
    private static final Pattern TAG_QUERY_OUTER_SPLIT_PATTERN = Pattern.compile("[ ]?AND[ ]?");
    private static final Pattern TAG_QUERY_INNER_SPLIT_PATTERN = Pattern.compile("[ ]?=[ ]?");

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
        String hqlQuery = buildHqlQuery(includeTagQuery(BASE_SELECT_QUERY, eventsRequest), eventsRequest);
        TypedQuery<EventEntity> typedQuery = session.createQuery(hqlQuery, EventEntity.class)
                .setParameter("tenantId", eventsRequest.getTenantId());
        setQueryParameters(typedQuery, eventsRequest);
        return typedQuery.getResultList();
    }

    public int delete(EventsRequest eventsRequest) {
        String hqlQuery = buildHqlQuery(BASE_DELETE_QUERY, eventsRequest);
        // TODO Include tag query.
        Query query = session.createQuery(hqlQuery)
                .setParameter("tenantId", eventsRequest.getTenantId());
        setQueryParameters(query, eventsRequest);
        return query.executeUpdate();
    }

    private static String buildHqlQuery(String baseHqlQuery, EventsRequest eventsRequest) {
        List<String> conditions = new ArrayList<>();
        conditions.add("e.id.tenantId = :tenantId");

        if (eventsRequest.getEventType() != null) {
            conditions.add("e.eventType = :eventType");
        }

        if (eventsRequest.getStartTime() != null && eventsRequest.getEndTime() != null) {
            conditions.add("e.ctime BETWEEN :startTime AND :endTime");
        } else if (eventsRequest.getStartTime() != null) {
            conditions.add("e.ctime >= :startTime");
        } else if (eventsRequest.getEndTime() != null) {
            conditions.add("e.ctime <= :endTime");
        }

        if (eventsRequest.getEventIds() != null && !eventsRequest.getEventIds().isEmpty()) {
            conditions.add("e.id.eventId IN (:eventIds)");
        }

        if (eventsRequest.getTriggerIds() != null && !eventsRequest.getTriggerIds().isEmpty()) {
            conditions.add("e.triggerId IN (:triggerIds)");
        }

        if (eventsRequest.getCategories() != null && !eventsRequest.getCategories().isEmpty()) {
            conditions.add("e.category IN (:categories)");
        }

        baseHqlQuery += hasTagQuery(eventsRequest) ? " AND " : " WHERE ";

        if (eventsRequest.getTagQuery() != null && !eventsRequest.getTagQuery().isBlank()) {
            conditions.add(eventsRequest.getTagQuery());
        }

        if (!conditions.isEmpty()) {
            baseHqlQuery += String.join(" AND ", conditions);
        }

        return baseHqlQuery;
    }

    private static boolean hasTagQuery(EventsRequest eventsRequest) {
        return eventsRequest.getTagQuery() != null && !eventsRequest.getTagQuery().isBlank();
    }

    // FIXME This is far from being complete. The immediate goal is only to have the tests pass.
    private static String includeTagQuery(String baseHqlQuery, EventsRequest eventsRequest) {
        if (hasTagQuery(eventsRequest)) {
            baseHqlQuery += " JOIN a.tags t JOIN t.values v WHERE ";
            for (String tagCondition : TAG_QUERY_OUTER_SPLIT_PATTERN.split(eventsRequest.getTagQuery())) {
                String[] tagConditionElements = TAG_QUERY_INNER_SPLIT_PATTERN.split(tagCondition);
                baseHqlQuery += "t.key = '" + tagConditionElements[0].replace("tags.", "") + "' AND v.value = " + tagConditionElements[1];
            }
        }
        return baseHqlQuery;
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
