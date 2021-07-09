package com.redhat.cloud.policies.engine.history.alert.repositories;

import com.redhat.cloud.policies.engine.history.alert.requests.AlertsRequest;
import com.redhat.cloud.policies.engine.history.alert.entities.AlertEntity;
import org.hibernate.Session;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static org.hawkular.alerts.api.model.event.Alert.Status;

@ApplicationScoped
public class AlertsRepository {

    private static final String BASE_SELECT_QUERY = "SELECT a FROM AlertEntity a";
    private static final String BASE_DELETE_QUERY = "DELETE FROM AlertEntity a";
    private static final Pattern TAG_QUERY_OUTER_SPLIT_PATTERN = Pattern.compile("[ ]?AND[ ]?");
    private static final Pattern TAG_QUERY_INNER_SPLIT_PATTERN = Pattern.compile("[ ]?=[ ]?");

    @Inject
    Session session;

    public void save(AlertEntity alertEntity) {
        session.persist(alertEntity);
    }

    public AlertEntity find(String tenantId, String alertId) {
        String hqlQuery = "FROM AlertEntity WHERE tenantId = :tenantId AND id = :alertId";
        return session.createQuery(hqlQuery, AlertEntity.class)
                .setParameter("tenantId", tenantId)
                .setParameter("alertId", alertId)
                .uniqueResult();
    }

    public List<AlertEntity> findAll(AlertsRequest alertsRequest) {
        String hqlQuery = buildHqlQuery(includeTagQuery(BASE_SELECT_QUERY, alertsRequest), alertsRequest);
        TypedQuery<AlertEntity> typedQuery = session.createQuery(hqlQuery, AlertEntity.class)
                .setParameter("tenantId", alertsRequest.getTenantId());
        setQueryParameters(typedQuery, alertsRequest);
        return typedQuery.getResultList();
    }

    public int delete(AlertsRequest alertsRequest) {
        String hqlQuery = buildHqlQuery(BASE_DELETE_QUERY, alertsRequest);
        // TODO Include tag query.
        Query query = session.createQuery(hqlQuery)
                .setParameter("tenantId", alertsRequest.getTenantId());
        setQueryParameters(query, alertsRequest);
        return query.executeUpdate();
    }

    private static String buildHqlQuery(String baseHqlQuery, AlertsRequest alertsRequest) {
        List<String> conditions = new ArrayList<>();
        conditions.add("a.id.tenantId = :tenantId");

        if (alertsRequest.getQuery() != null && !alertsRequest.getQuery().isBlank()) {
            conditions.add(alertsRequest.getQuery());
        }

        if (alertsRequest.getStartTime() != null && alertsRequest.getEndTime() != null) {
            conditions.add("a.ctime BETWEEN :startTime AND :endTime");
        } else if (alertsRequest.getStartTime() != null) {
            conditions.add("a.ctime >= :startTime");
        } else if (alertsRequest.getEndTime() != null) {
            conditions.add("a.ctime <= :endTime");
        }

        if (alertsRequest.getStartResolvedTime() != null || alertsRequest.getEndResolvedTime() != null) {
            conditions.add("a.status = :status");
            if (alertsRequest.getStartResolvedTime() != null && alertsRequest.getEndResolvedTime() != null) {
                conditions.add("a.stime BETWEEN :startResolvedTime AND :endResolvedTime");
            } else if (alertsRequest.getStartResolvedTime() != null) {
                conditions.add("a.stime >= :startResolvedTime");
            } else if (alertsRequest.getEndResolvedTime() != null) {
                conditions.add("a.stime <= :endResolvedTime");
            }
        }

        if (alertsRequest.getStartAckTime() != null || alertsRequest.getEndAckTime() != null) {
            conditions.add("a.status = :status");
            if (alertsRequest.getStartAckTime() != null && alertsRequest.getEndAckTime() != null) {
                conditions.add("a.stime BETWEEN :startAckTime AND :endAckTime");
            } else if (alertsRequest.getStartAckTime() != null) {
                conditions.add("a.stime >= :startAckTime");
            } else if (alertsRequest.getEndAckTime() != null) {
                conditions.add("a.stime <= :endAckTime");
            }
        }

        if (alertsRequest.getStartStatusTime() != null && alertsRequest.getEndStatusTime() != null) {
            conditions.add("a.stime BETWEEN :startStatusTime AND :endStatusTime");
        } else if (alertsRequest.getStartStatusTime() != null) {
            conditions.add("a.stime >= :startStatusTime");
        } else if (alertsRequest.getEndStatusTime() != null) {
            conditions.add("a.stime <= :endStatusTime");
        }

        if (alertsRequest.getAlertIds() != null && !alertsRequest.getAlertIds().isEmpty()) {
            conditions.add("a.id.eventId IN (:alertIds)");
        }

        if (alertsRequest.getStatuses() != null && !alertsRequest.getStatuses().isEmpty()) {
            conditions.add("a.status IN (:statuses)");
        }

        if (alertsRequest.getSeverities() != null && !alertsRequest.getSeverities().isEmpty()) {
            conditions.add("a.severity IN (:severities)");
        }

        if (alertsRequest.getTriggerIds() != null && !alertsRequest.getTriggerIds().isEmpty()) {
            conditions.add("a.triggerId IN (:triggerIds)");
        }

        baseHqlQuery += hasTagQuery(alertsRequest) ? " AND " : " WHERE ";

        if (!conditions.isEmpty()) {
            baseHqlQuery += String.join(" AND ", conditions);
        }

        return baseHqlQuery;
    }

    private static boolean hasTagQuery(AlertsRequest alertsRequest) {
        return alertsRequest.getTagQuery() != null && !alertsRequest.getTagQuery().isBlank();
    }

    // FIXME This is far from being complete. The immediate goal is only to have the tests pass.
    private static String includeTagQuery(String baseHqlQuery, AlertsRequest alertsRequest) {
        if (hasTagQuery(alertsRequest)) {
            baseHqlQuery += " JOIN a.tags t JOIN t.values v WHERE ";
            for (String tagCondition : TAG_QUERY_OUTER_SPLIT_PATTERN.split(alertsRequest.getTagQuery())) {
                String[] tagConditionElements = TAG_QUERY_INNER_SPLIT_PATTERN.split(tagCondition);
                baseHqlQuery += "t.key = '" + tagConditionElements[0].replace("tags.", "") + "' AND v.value = " + tagConditionElements[1];
            }
        }
        return baseHqlQuery;
    }

    private static void setQueryParameters(Query query, AlertsRequest alertsRequest) {

        if (alertsRequest.getStartTime() != null && alertsRequest.getEndTime() != null) {
            query.setParameter("startTime", alertsRequest.getStartTime()).setParameter("endTime", alertsRequest.getEndTime());
        } else if (alertsRequest.getStartTime() != null) {
            query.setParameter("startTime", alertsRequest.getStartTime());
        } else if (alertsRequest.getEndTime() != null) {
            query.setParameter("endTime", alertsRequest.getEndTime());
        }

        if (alertsRequest.getStartResolvedTime() != null || alertsRequest.getEndResolvedTime() != null) {
            query.setParameter("status", Status.RESOLVED);
            if (alertsRequest.getStartResolvedTime() != null && alertsRequest.getEndResolvedTime() != null) {
                query.setParameter("startResolvedTime", alertsRequest.getStartResolvedTime()).setParameter("endResolvedTime", alertsRequest.getEndResolvedTime());
            } else if (alertsRequest.getStartResolvedTime() != null) {
                query.setParameter("startResolvedTime", alertsRequest.getStartResolvedTime());
            } else if (alertsRequest.getEndResolvedTime() != null) {
                query.setParameter("endResolvedTime", alertsRequest.getEndResolvedTime());
            }
        }

        if (alertsRequest.getStartAckTime() != null || alertsRequest.getEndAckTime() != null) {
            query.setParameter("status", Status.ACKNOWLEDGED);
            if (alertsRequest.getStartAckTime() != null && alertsRequest.getEndAckTime() != null) {
                query.setParameter("startAckTime", alertsRequest.getStartAckTime()).setParameter("endAckTime", alertsRequest.getEndAckTime());
            } else if (alertsRequest.getStartAckTime() != null) {
                query.setParameter("startAckTime", alertsRequest.getStartAckTime());
            } else if (alertsRequest.getEndAckTime() != null) {
                query.setParameter("endAckTime", alertsRequest.getEndAckTime());
            }
        }

        if (alertsRequest.getStartStatusTime() != null && alertsRequest.getEndStatusTime() != null) {
            query.setParameter("startStatusTime", alertsRequest.getStartStatusTime()).setParameter("endStatusTime", alertsRequest.getEndStatusTime());
        } else if (alertsRequest.getStartStatusTime() != null) {
            query.setParameter("startStatusTime", alertsRequest.getStartStatusTime());
        } else if (alertsRequest.getEndStatusTime() != null) {
            query.setParameter("endStatusTime", alertsRequest.getEndStatusTime());
        }

        if (alertsRequest.getAlertIds() != null && !alertsRequest.getAlertIds().isEmpty()) {
            query.setParameter("alertIds", alertsRequest.getAlertIds());
        }

        if (alertsRequest.getStatuses() != null && !alertsRequest.getStatuses().isEmpty()) {
            query.setParameter("statuses", alertsRequest.getStatuses());
        }

        if (alertsRequest.getSeverities() != null && !alertsRequest.getSeverities().isEmpty()) {
            query.setParameter("severities", alertsRequest.getSeverities());
        }

        if (alertsRequest.getTriggerIds() != null && !alertsRequest.getTriggerIds().isEmpty()) {
            query.setParameter("triggerIds", alertsRequest.getTriggerIds());
        }
    }
}
