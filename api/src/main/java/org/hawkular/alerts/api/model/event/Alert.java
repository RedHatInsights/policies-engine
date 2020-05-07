package org.hawkular.alerts.api.model.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import org.hawkular.alerts.api.doc.DocModel;
import org.hawkular.alerts.api.doc.DocModelProperty;
import org.hawkular.alerts.api.model.Lifecycle;
import org.hawkular.alerts.api.model.Note;
import org.hawkular.alerts.api.model.Severity;
import org.hawkular.alerts.api.model.condition.ConditionEval;
import org.hawkular.alerts.api.model.dampening.Dampening;
import org.hawkular.alerts.api.model.trigger.Trigger;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import static org.hawkular.alerts.api.util.Util.isEmpty;

/**
 * A status of an alert thrown by several matched conditions.
 *
 * @author Jay Shaughnessy
 * @author Lucas Ponce
 */
@DocModel(description = "Alerts are generated when an Alert Trigger fires, based on a set of defined conditions + \n" +
        "that have been matched, possibly more than once or have held true over a period of time. + \n" +
        " + \n" +
        "When fired the trigger can perform actions based on plugins (e-mail, sms, etc). + \n" +
        " + \n" +
        "Alerts then start moving through the Open, Acknowledged, Resolved life-cycle. + \n" +
        " + \n" +
        "- Open status represents an alert which has not been seen/taken yet by any user. + \n" +
        "- Acknowledge status represents an alert which has been seen/taken by any user and it is pending " +
        "resolution. + \n" +
        "- Resolved status represents an alert which problem has been resolved. + \n" +
        " + \n" +
        "Alerts can be resolved automatically using AUTORESOLVE <<Trigger>> conditions or manually via API. + \n" +
        " + \n" +
        "Alert can attach a list of notes defined by the user. + \n" +
        " + \n" +
        "There are many options on triggers to help ensure that alerts are not generated too frequently, + \n" +
        "including ways of automatically disabling and enabling the trigger. + \n")
public class Alert extends Event {

    public enum Status {
        OPEN, ACKNOWLEDGED, RESOLVED
    };

    @DocModelProperty(description = "Severity set for a <<Trigger>> and assigned to an alert when it is generated.",
            position = 0,
            defaultValue = "MEDIUM")
    @JsonInclude
    private Severity severity;

    @DocModelProperty(description = "Lifecycle current status.",
            position = 1)
    @JsonInclude
    private Status status;

    @DocModelProperty(description = "List of lifecycle states that this alert has navigated.",
            position = 2)
    @JsonInclude(Include.NON_EMPTY)
    private List<Lifecycle> lifecycle = new ArrayList<>();

    @DocModelProperty(description = "The Eval Sets that resolved the <<Trigger>> in AUTORESOLVE mode. + \n " +
            "Null for non AUTORESOLVE triggers.",
            position = 4)
    @JsonInclude(Include.NON_EMPTY)
    @Thin
    private List<Set<ConditionEval>> resolvedEvalSets;

    public Alert() {
        // for json assembly
        this.eventType = EventType.ALERT.name();
        this.status = Status.OPEN;
    }

    /**
     * Assumes default dampening.
     */
    public Alert(String tenantId, Trigger trigger, List<Set<ConditionEval>> evalSets) {
        this(tenantId, trigger, null, evalSets);
    }

    public Alert(Alert alert) {
        super((Event) alert);

        this.status = alert.getStatus();
        this.severity = alert.getSeverity();
        this.eventType = alert.getEventType();
        this.lifecycle = new ArrayList<>();
        for (Lifecycle item : alert.getLifecycle()) {
            this.lifecycle.add(new Lifecycle(item));
        }
        this.resolvedEvalSets = alert.getResolvedEvalSets();
    }

    public Alert(String tenantId, Trigger trigger, Dampening dampening, List<Set<ConditionEval>> evalSets) {
        super(tenantId, trigger, dampening, evalSets);

        this.status = Status.OPEN;
        this.severity = trigger.getSeverity();
        this.eventType = EventType.ALERT.name();
        addLifecycle(this.status, this.ctime, null);
    }

    @JsonIgnore
    public String getAlertId() {
        return id;
    }

    public void setAlertId(String alertId) {
        this.id = alertId;
    }

    @JsonIgnore
    public String getTriggerId() {
        return getTrigger() != null ? getTrigger().getId() : null;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public List<Set<ConditionEval>> getResolvedEvalSets() {
        return resolvedEvalSets;
    }

    public void setResolvedEvalSets(List<Set<ConditionEval>> resolvedEvalSets) {
        this.resolvedEvalSets = resolvedEvalSets;
    }

    public List<Lifecycle> getLifecycle() {
        return lifecycle;
    }

    public void setLifecycle(List<Lifecycle> lifecycle) {
        this.lifecycle = lifecycle;
    }

    public void addLifecycle(Status status, long stime, List<Note> notes) {
        if (status == null) {
            throw new IllegalArgumentException("Lifecycle must have non-null state and user");
        }
        setStatus(status);
        Lifecycle lifecycle = new Lifecycle(status.name(), stime);
        if(!isEmpty(notes)) {
            lifecycle.setNotes(notes);
        }
        getLifecycle().add(lifecycle);
    }

    /**
     * Add note to latest lifecycle event
     * @param note
     */
    public void addNote(Note note) {
        Lifecycle currentLifecycle = getCurrentLifecycle();
        if(currentLifecycle != null) {
            currentLifecycle.addNote(note);
        }
    }

    @JsonIgnore
    public Lifecycle getCurrentLifecycle() {
        if (getLifecycle().isEmpty()) {
            return null;
        }
        return getLifecycle().get(getLifecycle().size() - 1);
    }

    @JsonIgnore
    public Long getLastStatusTime(Status status) {
        if (getLifecycle().isEmpty()) {
            return null;
        }
        Long statusTime = null;
        ListIterator<Lifecycle> iterator = getLifecycle().listIterator(getLifecycle().size());
        while (iterator.hasPrevious()) {
            Lifecycle lifeCycle = iterator.previous();
            if (lifeCycle.getStatus().equals(status.name())) {
                statusTime = lifeCycle.getStime();
                break;
            }
        }
        return statusTime;
    }

    @JsonIgnore
    public Long getLastOpenTime() {
        return getLastStatusTime(Status.OPEN);
    }

    @JsonIgnore
    public Long getLastAckTime() {
        return getLastStatusTime(Status.ACKNOWLEDGED);
    }

    @JsonIgnore
    public Long getLastResolvedTime() {
        return getLastStatusTime(Status.RESOLVED);
    }

    @Override
    public String toString() {
        return "Alert [tenantId=" + tenantId + ", triggerId=" + getTriggerId() + ", severity=" + severity
                + ", status=" + status + ", ctime=" + ctime + ", lifecycle=" + lifecycle
                + ", resolvedEvalSets=" + resolvedEvalSets + "]";
    }

}