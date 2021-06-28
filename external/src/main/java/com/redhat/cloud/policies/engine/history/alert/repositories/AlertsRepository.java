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

import static org.hawkular.alerts.api.model.event.Alert.Status;

@ApplicationScoped
public class AlertsRepository {

    private static final String BASE_SELECT_QUERY = "FROM AlertEntity WHERE tenantId = :tenantId";
    private static final String BASE_DELETE_QUERY = "DELETE FROM AlertEntity WHERE id.tenantId = :tenantId";

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
        String hqlQuery = buildHqlQuery(BASE_SELECT_QUERY, alertsRequest);
        TypedQuery<AlertEntity> typedQuery = session.createQuery(hqlQuery, AlertEntity.class)
                .setParameter("tenantId", alertsRequest.getTenantId());
        setQueryParameters(typedQuery, alertsRequest);
        return typedQuery.getResultList();
    }

    public int delete(AlertsRequest alertsRequest) {
        String hqlQuery = buildHqlQuery(BASE_DELETE_QUERY, alertsRequest);
        Query query = session.createQuery(hqlQuery)
                .setParameter("tenantId", alertsRequest.getTenantId());
        setQueryParameters(query, alertsRequest);
        return query.executeUpdate();
    }

    private static String buildHqlQuery(String baseQuery, AlertsRequest alertsRequest) {
        List<String> conditions = new ArrayList<>();

        if (alertsRequest.getQuery() != null) {
            conditions.add(alertsRequest.getQuery());
        }

        if (alertsRequest.getStartTime() != null && alertsRequest.getEndTime() != null) {
            conditions.add("ctime BETWEEN :startTime AND :endTime");
        } else if (alertsRequest.getStartTime() != null) {
            conditions.add("ctime >= :startTime");
        } else if (alertsRequest.getEndTime() != null) {
            conditions.add("ctime <= :endTime");
        }

        if (alertsRequest.getStartResolvedTime() != null || alertsRequest.getEndResolvedTime() != null) {
            conditions.add("status = :status");
            if (alertsRequest.getStartResolvedTime() != null && alertsRequest.getEndResolvedTime() != null) {
                conditions.add("stime BETWEEN :startResolvedTime AND :endResolvedTime");
            } else if (alertsRequest.getStartResolvedTime() != null) {
                conditions.add("stime >= :startResolvedTime");
            } else if (alertsRequest.getEndResolvedTime() != null) {
                conditions.add("stime <= :endResolvedTime");
            }
        }

        if (alertsRequest.getStartAckTime() != null || alertsRequest.getEndAckTime() != null) {
            conditions.add("status = :status");
            if (alertsRequest.getStartAckTime() != null && alertsRequest.getEndAckTime() != null) {
                conditions.add("stime BETWEEN :startAckTime AND :endAckTime");
            } else if (alertsRequest.getStartAckTime() != null) {
                conditions.add("stime >= :startAckTime");
            } else if (alertsRequest.getEndAckTime() != null) {
                conditions.add("stime <= :endAckTime");
            }
        }

        if (alertsRequest.getStartStatusTime() != null && alertsRequest.getEndStatusTime() != null) {
            conditions.add("stime BETWEEN :startStatusTime AND :endStatusTime");
        } else if (alertsRequest.getStartStatusTime() != null) {
            conditions.add("stime >= :startStatusTime");
        } else if (alertsRequest.getEndStatusTime() != null) {
            conditions.add("stime <= :endStatusTime");
        }

        if (alertsRequest.getAlertIds() != null && !alertsRequest.getAlertIds().isEmpty()) {
            conditions.add("id.eventId IN (:alertIds)");
        }

        if (alertsRequest.getStatuses() != null && !alertsRequest.getStatuses().isEmpty()) {
            conditions.add("status IN (:statuses)");
        }

        if (alertsRequest.getSeverities() != null && !alertsRequest.getSeverities().isEmpty()) {
            conditions.add("severity IN (:severities)");
        }

        if (alertsRequest.getTriggerIds() != null && !alertsRequest.getTriggerIds().isEmpty()) {
            conditions.add("triggerId IN (:triggerIds)");
        }

        if (alertsRequest.getTagQuery() != null) {
            conditions.add(alertsRequest.getTagQuery());
        }

        if (conditions.isEmpty()) {
            return baseQuery;
        } else {
            return baseQuery + " WHERE " + String.join(" AND ", conditions);
        }
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
