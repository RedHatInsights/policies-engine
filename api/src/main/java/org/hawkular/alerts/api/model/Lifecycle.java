package org.hawkular.alerts.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.hawkular.alerts.api.doc.DocModel;
import org.hawkular.alerts.api.doc.DocModelProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@DocModel(description = "A lifecycle event representation.")
public class Lifecycle implements Serializable {

    @DocModelProperty(description = "The event type of this lifecycle.",
            position = 0)
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String status;

    @DocModelProperty(description = "Creation time for this state.",
            position = 1,
            allowableValues = "Timestamp in milliseconds since epoch.")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private long stime;

    @DocModelProperty(description = "Notes attached to this lifecycle event",
            position = 2)
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<Note> notes = new ArrayList<>();


    public Lifecycle() {
        // for json assembly
    }

    public Lifecycle(Lifecycle lifeCycle) {
        if (lifeCycle == null) {
            throw new IllegalArgumentException("lifeCycle must be not null");
        }
        this.status = lifeCycle.getStatus();
        this.stime = lifeCycle.getStime();
        this.notes = new ArrayList<>();
        lifeCycle.getNotes().stream().forEach(n -> {
            notes.add(new Note(n));
        });
    }

    public Lifecycle(String status, long stime) {
        this.status = status;
        this.stime = stime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getStime() {
        return stime;
    }

    public void setStime(long stime) {
        this.stime = stime;
    }

    public List<Note> getNotes() {
        return notes;
    }

    public void setNotes(List<Note> notes) {
        this.notes = notes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Lifecycle lifecycle = (Lifecycle) o;
        return stime == lifecycle.stime &&
                Objects.equals(status, lifecycle.status) &&
                Objects.equals(notes, lifecycle.notes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, stime, notes);
    }

    @Override
    public String toString() {
        return "Lifecycle{" +
                "status='" + status + '\'' +
                ", stime=" + stime +
                ", notes=" + notes +
                '}';
    }
}
