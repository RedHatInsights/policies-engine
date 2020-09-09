package org.hawkular.alerts.api.services;

import org.hawkular.alerts.api.model.Severity;
import org.hawkular.alerts.api.model.event.Alert;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static org.hawkular.alerts.api.util.Util.isEmpty;


/**
 * Query criteria for fetching Alerts.
 * @author jay shaughnessy
 * @author lucas ponce
 */
public class AlertsCriteria {
    Long startTime = null;
    Long endTime = null;
    Long startResolvedTime = null;
    Long endResolvedTime = null;
    Long startAckTime = null;
    Long endAckTime = null;
    Long startStatusTime = null;
    Long endStatusTime = null;
    String alertId = null;
    Collection<String> alertIds = null;
    Alert.Status status = null;
    Collection<Alert.Status> statusSet = null;
    Severity severity = null;
    Collection<Severity> severities = null;
    String triggerId = null;
    Collection<String> triggerIds = null;
    String tagQuery = null;
    boolean thin = false;

    public AlertsCriteria() {
        super();
    }

    public AlertsCriteria(Long startTime, Long endTime, String alertIds, String triggerIds,
                          String statuses, String severities, String tagQuery, Long startResolvedTime,
                          Long endResolvedTime, Long startAckTime, Long endAckTime, Long startStatusTime,
                          Long endStatusTime, Boolean thin) {
        setStartTime(startTime);
        setEndTime(endTime);
        if (!isEmpty(alertIds)) {
            setAlertIds(Arrays.asList(alertIds.split(",")));
        }
        if (!isEmpty(triggerIds)) {
            setTriggerIds(Arrays.asList(triggerIds.split(",")));
        }
        if (!isEmpty(statuses)) {
            Set<Alert.Status> statusSet = new HashSet<>();
            for (String s : statuses.split(",")) {
                statusSet.add(Alert.Status.valueOf(s));
            }
            setStatusSet(statusSet);
        }
        if (null != severities && !severities.trim().isEmpty()) {
            Set<Severity> severitySet = new HashSet<>();
            for (String s : severities.split(",")) {
                severitySet.add(Severity.valueOf(s));
            }
            setSeverities(severitySet);
        }
        setTagQuery(tagQuery);
        setStartResolvedTime(startResolvedTime);
        setEndResolvedTime(endResolvedTime);
        setStartAckTime(startAckTime);
        setEndAckTime(endAckTime);
        setStartStatusTime(startStatusTime);
        setEndStatusTime(endStatusTime);
        if (null != thin) {
            setThin(thin.booleanValue());
        }
    }

    public String getQuery() {
        StringBuilder query = new StringBuilder();
        if (this.hasAlertIdCriteria()) {
            query.append("and (");
            Iterator<String> iter = extractAlertIds(this).iterator();
            while (iter.hasNext()) {
                String alertId = iter.next();
                query.append("id = '").append(alertId).append("' ");
                if (iter.hasNext()) {
                    query.append("or ");
                }
            }
            query.append(") ");
        }
//        if (this.hasTagQueryCriteria()) {
//            query.append("and (tags : ");
////               parseTagQuery(this.getTagQuery(), query);
//            query.append(") ");
//        }
        if (this.hasTriggerIdCriteria()) {
            query.append("and (");
            Iterator<String> iter = extractTriggerIds(this).iterator();
            while (iter.hasNext()) {
                String triggerId = iter.next();
                query.append("triggerId = '").append(triggerId).append("' ");
                if (iter.hasNext()) {
                    query.append("or ");
                }
            }
            query.append(") ");
        }
        if (this.hasCTimeCriteria()) {
            query.append("and (");
            if (this.getStartTime() != null) {
                query.append("ctime >= ").append(this.getStartTime()).append(" ");
            }
            if (this.getEndTime() != null) {
                if (this.getStartTime() != null) {
                    query.append("and ");
                }
                query.append("ctime <= ").append(this.getEndTime()).append(" ");
            }
            query.append(") ");
        }
        if (this.hasResolvedTimeCriteria()) {
            query.append("and (status = '").append(Alert.Status.RESOLVED.name()).append("' and ");
            if (this.getStartResolvedTime() != null) {
                query.append("stime >= ").append(this.getStartResolvedTime()).append(" ");
            }
            if (this.getEndResolvedTime() != null) {
                if (this.getStartResolvedTime() != null) {
                    query.append("and ");
                }
                query.append("stime <= ").append(this.getEndResolvedTime()).append(" ");
            }
            query.append(") ");
        }
        if (this.hasAckTimeCriteria()) {
            query.append("and (status = '").append(Alert.Status.ACKNOWLEDGED.name()).append("' and ");
            if (this.getStartAckTime() != null) {
                query.append("stime >= ").append(this.getStartAckTime()).append(" ");
            }
            if (this.getEndAckTime() != null) {
                if (this.getStartAckTime() != null) {
                    query.append("and ");
                }
                query.append("stime <= ").append(this.getEndAckTime()).append(" ");
            }
            query.append(") ");
        }
        if (this.hasStatusTimeCriteria()) {
            query.append("and (");
            if (this.getStartStatusTime() != null) {
                query.append("stime >= ").append(this.getStartStatusTime()).append(" ");
            }
            if (this.getEndTime() != null) {
                if (this.getStartTime() != null) {
                    query.append("and ");
                }
                query.append("stime <= ").append(this.getEndStatusTime()).append(" ");
            }
            query.append(") ");
        }
        if (this.hasSeverityCriteria()) {
            query.append("and (");
            Iterator<Severity> iterSev = extractSeverity(this).iterator();
            while (iterSev.hasNext()) {
                Severity severity = iterSev.next();
                query.append("severity = '").append(severity.name()).append("' ");
                if (iterSev.hasNext()) {
                    query.append(" or ");
                }
            }
            query.append(") ");
        }
        if (this.hasStatusCriteria()) {
            query.append("and (");
            Iterator<Alert.Status> iterStatus = extractStatus(this).iterator();
            while (iterStatus.hasNext()) {
                Alert.Status status = iterStatus.next();
                query.append("status = '").append(status.name()).append("' ");
                if (iterStatus.hasNext()) {
                    query.append(" or ");
                }
            }
            query.append(") ");
        }

        String resultQuery = query.toString();
        if(resultQuery.startsWith("and")) {
            resultQuery = resultQuery.substring(4);
        }
        return resultQuery;
    }

    public static Set<String> extractAlertIds(AlertsCriteria criteria) {
        Set<String> alertIds = new HashSet<>();
        if (!isEmpty(criteria.getAlertId())) {
            alertIds.add(criteria.getAlertId());
        }
        if (!isEmpty(criteria.getAlertIds())) {
            alertIds.addAll(criteria.getAlertIds());
        }
        return alertIds;
    }

    public static Set<Severity> extractSeverity(AlertsCriteria criteria) {
        Set<Severity> severities = new HashSet<>();
        if (criteria.getSeverity() != null) {
            severities.add(criteria.getSeverity());
        }
        if (!isEmpty(criteria.getSeverities())) {
            severities.addAll(criteria.getSeverities());
        }
        return severities;
    }

    public static Set<Alert.Status> extractStatus(AlertsCriteria criteria) {
        Set<Alert.Status> statuses = new HashSet<>();
        if (criteria.getStatus() != null) {
            statuses.add(criteria.getStatus());
        }
        if (!isEmpty(criteria.getStatusSet())) {
            statuses.addAll(criteria.getStatusSet());
        }
        return statuses;
    }

    public static Set<String> extractTriggerIds(AlertsCriteria criteria) {

        boolean hasTriggerId = !isEmpty(criteria.getTriggerId());
        boolean hasTriggerIds = !isEmpty(criteria.getTriggerIds());

        Set<String> triggerIds = hasTriggerId || hasTriggerIds ? new HashSet<>() : Collections.emptySet();

        if (!hasTriggerIds) {
            if (hasTriggerId) {
                triggerIds.add(criteria.getTriggerId());
            }
        } else {
            for (String triggerId : criteria.getTriggerIds()) {
                if (isEmpty(triggerId)) {
                    continue;
                }
                triggerIds.add(triggerId);
            }
        }

        return triggerIds;
    }

    public Long getStartTime() {
        return startTime;
    }

    /**
     * @param startTime fetched Alerts must have cTime greater than or equal to startTime
     */
    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    /**
     * @param endTime fetched Alerts must have cTime less than or equal to endTime
     */
    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public Long getStartResolvedTime() {
        return startResolvedTime;
    }

    /**
     * @param startResolvedTime fetched Alerts must have at least one resolvedTime in the lifecycle greater than or
     *                          equal to startResolvedTime.
     *                          Alerts lifecycle might involve several transitions between ACKNOWLEDGE and RESOLVE
     *                          states.
     */
    public void setStartResolvedTime(Long startResolvedTime) {
        this.startResolvedTime = startResolvedTime;
    }

    public Long getEndResolvedTime() {
        return endResolvedTime;
    }

    /**
     * @param endResolvedTime fetched Alerts must have at least one resolvedTime in the lifecycle less than or equal to
     *                        endResolvedTime.
     *                        Alerts lifecycle might involve several transitions between ACKNOWLEDGE and RESOLVE states.
     */
    public void setEndResolvedTime(Long endResolvedTime) {
        this.endResolvedTime = endResolvedTime;
    }

    public Long getStartAckTime() {
        return startAckTime;
    }

    /**
     * @param startAckTime fetched Alerts must have at least one ackTime in the lifecycle greater than or equal to
     *                     startAckTime.
     *                     Alerts lifecycle might involve several transitions between ACKNOWLEDGE and RESOLVE states.
     */
    public void setStartAckTime(Long startAckTime) {
        this.startAckTime = startAckTime;
    }

    public Long getEndAckTime() {
        return endAckTime;
    }

    /**
     * @param endAckTime fetched Alerts must have at least one ackTime in the lifecycle less than or equal to
     *                   endAckTime.
     *                   Alerts lifecycle might involve several transitions between ACKNOWLEDGE and RESOLVE states.
     */
    public void setEndAckTime(Long endAckTime) {
        this.endAckTime = endAckTime;
    }

    public Long getStartStatusTime() {
        return startStatusTime;
    }

    /**
     * @param startStatusTime fetched Alerts must have at least one statusTime in the lifecycle greater than or equal to
     *                     startStatusTime.
     */
    public void setStartStatusTime(Long startStatusTime) {
        this.startStatusTime = startStatusTime;
    }

    public Long getEndStatusTime() {
        return endStatusTime;
    }

    /**
     * @param endStatusTime fetched Alerts must have at least one statusTime in the lifecycle less than or equal to
     *                   endStatusTime.
     */
    public void setEndStatusTime(Long endStatusTime) {
        this.endStatusTime = endStatusTime;
    }

    public String getAlertId() {
        return alertId;
    }

    public void setAlertId(String alertId) {
        this.alertId = alertId;
    }

    public Collection<String> getAlertIds() {
        return alertIds;
    }

    public void setAlertIds(Collection<String> alertIds) {
        this.alertIds = alertIds;
    }

    public Alert.Status getStatus() {
        return status;
    }

    public void setStatus(Alert.Status status) {
        this.status = status;
    }

    public Collection<Alert.Status> getStatusSet() {
        return statusSet;
    }

    public void setStatusSet(Collection<Alert.Status> statusSet) {
        this.statusSet = statusSet;
    }

    public String getTriggerId() {
        return triggerId;
    }

    /**
     * @param triggerId fetched Alerts must be for the specified trigger. Ignored if triggerIds is not empty.
     */
    public void setTriggerId(String triggerId) {
        this.triggerId = triggerId;
    }

    public Collection<String> getTriggerIds() {
        return triggerIds;
    }

    /**
     * @param triggerIds fetched alerts must be for one of the specified triggers.
     */
    public void setTriggerIds(Collection<String> triggerIds) {
        this.triggerIds = triggerIds;
    }

    public String getTagQuery() {
        return tagQuery;
    }

    /**
     * @param tagQuery return alerts with *any* of the tags specified by the tag expression language:
     *
     * <pre>
     *        <tag_query> ::= ( <expression> | "(" <object> ")" | <object> <logical_operator> <object> )
     *        <expression> ::= ( <tag_name> | <not> <tag_name> | <tag_name> <boolean_operator> <tag_value> |
     *               <tag_key> <array_operator> <array> )
     *        <not> ::= [ "NOT" | "not" ]
     *        <logical_operator> ::= [ "AND" | "OR" | "and" | "or" ]
     *        <boolean_operator> ::= [ "==" | "!=" ]
     *        <array_operator> ::= [ "IN" | "NOT IN" | "in" | "not in" ]
     *        <array> ::= ( "[" "]" | "[" ( "," <tag_value> )* )
     *        <tag_name> ::= <identifier>                                // Tag identifier
     *        <tag_value> ::= ( "'" <regexp> "'" | <simple_value> )      // Regular expression used with quotes
     * </pre>
     */
    public void setTagQuery(String tagQuery) {
        this.tagQuery = tagQuery;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    public Collection<Severity> getSeverities() {
        return severities;
    }

    public void setSeverities(Collection<Severity> severities) {
        this.severities = severities;
    }

    public boolean isThin() {
        return thin;
    }

    public void setThin(boolean thin) {
        this.thin = thin;
    }

    public boolean hasAlertIdCriteria() {
        return !isEmpty(alertId) || !isEmpty(alertIds);
    }

    public boolean hasSeverityCriteria() {
        return  null != severity || !isEmpty(severities);
    }

    public boolean hasStatusCriteria() {
        return null != status || !isEmpty(statusSet);
    }

    public boolean hasTagQueryCriteria() {
        return !isEmpty(tagQuery);
    }

    public boolean hasCTimeCriteria() {
        return (null != startTime || null != endTime);
    }

    public boolean hasTriggerIdCriteria() {
        return !isEmpty(triggerId) || !isEmpty(triggerIds);
    }

    public boolean hasResolvedTimeCriteria() {
        return (null != startResolvedTime || null != endResolvedTime);
    }

    public boolean hasAckTimeCriteria() {
        return (null != startAckTime || null != endAckTime);
    }

    public boolean hasStatusTimeCriteria() {
        return (null != startStatusTime || null != endStatusTime);
    }

    public boolean hasCriteria() {
        return hasAlertIdCriteria()
                || hasStatusCriteria()
                || hasSeverityCriteria()
                || hasTagQueryCriteria()
                || hasCTimeCriteria()
                || hasTriggerIdCriteria()
                || hasResolvedTimeCriteria()
                || hasAckTimeCriteria()
                || hasStatusTimeCriteria();
    }

    @Override
    public String toString() {
        return "AlertsCriteria{" +
                "startTime=" + startTime +
                ", endTime=" + endTime +
                ", startResolvedTime=" + startResolvedTime +
                ", endResolvedTime=" + endResolvedTime +
                ", startAckTime=" + startAckTime +
                ", endAckTime=" + endAckTime +
                ", startStatusTime=" + startStatusTime +
                ", endStatusTime=" + endStatusTime +
                ", alertId='" + alertId + '\'' +
                ", alertIds=" + alertIds +
                ", status=" + status +
                ", statusSet=" + statusSet +
                ", severity=" + severity +
                ", severities=" + severities +
                ", triggerId='" + triggerId + '\'' +
                ", triggerIds=" + triggerIds +
                ", tagQuery='" + tagQuery + '\'' +
                ", thin=" + thin +
                '}';
    }

}
