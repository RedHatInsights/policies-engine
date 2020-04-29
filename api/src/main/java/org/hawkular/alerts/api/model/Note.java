package org.hawkular.alerts.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.hawkular.alerts.api.doc.DocModel;
import org.hawkular.alerts.api.doc.DocModelProperty;

import java.io.Serializable;
import java.util.Objects;

@DocModel(description = "A simple note representation.")
public class Note implements Serializable {

    @DocModelProperty(description = "User who created this note.",
            position = 0,
            required = true)
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String user;

    @DocModelProperty(description = "Note creation time.",
            position = 1,
            allowableValues = "Timestamp in milliseconds since epoch.")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private long ctime;

    @DocModelProperty(description = "The note text.",
            position = 2)
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String text;

    public Note() {
        // for json assembly
    }

    public Note(Note note) {
        this.user = note.getUser();
        this.ctime = note.getCtime();
        this.text = note.getText();
    }

    public Note(String user, String text) {
        this(user, System.currentTimeMillis(), text);
    }

    public Note(String user, long ctime, String text) {
        this.user = user;
        this.ctime = ctime;
        this.text = text;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public long getCtime() {
        return ctime;
    }

    public void setCtime(long ctime) {
        this.ctime = ctime;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Note note = (Note) o;
        return ctime == note.ctime &&
                Objects.equals(user, note.user) &&
                Objects.equals(text, note.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, ctime, text);
    }

    @Override
    public String toString() {
        return "Note{" +
                "user='" + user + '\'' +
                ", ctime=" + ctime +
                ", text='" + text + '\'' +
                '}';
    }
}
