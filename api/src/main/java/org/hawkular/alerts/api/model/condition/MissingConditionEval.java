package org.hawkular.alerts.api.model.condition;

import java.util.HashMap;
import java.util.Objects;

import org.hawkular.alerts.api.doc.DocModel;
import org.hawkular.alerts.api.doc.DocModelProperty;
import org.hawkular.alerts.api.model.condition.Condition.Type;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * An evaluation state for MissingCondition
 *
 * @author Jay Shaughnessy
 * @author Lucas Ponce
 */
@DocModel(description = "An evaluation state for missing condition.")
public class MissingConditionEval extends ConditionEval {

    private static final long serialVersionUID = 1L;

    @DocModelProperty(description = "Time when trigger was enabled or last time a data/event was received.",
            position = 0)
    @JsonInclude
    private long previousTime;

    @DocModelProperty(description = "Time when most recently evaluation of missing condition.",
            position = 1)
    @JsonInclude
    private long time;

    /**
     * Used for JSON deserialization, not for general use.
     */
    public MissingConditionEval() {
        super(Type.MISSING, false, 0, null);
        this.previousTime = 0L;
        this.time = 0L;
    }

    public MissingConditionEval(MissingCondition condition, long previousTime, long time) {
        super(Type.MISSING, condition.match(previousTime, time), time, new HashMap<>());
        setCondition(condition);
        this.previousTime = previousTime;
        this.time = time;
    }

    @Override
    public String getTenantId() {
        return condition.getTenantId();
    }

    @Override
    public String getTriggerId() {
        return condition.getTriggerId();
    }

    @Override
    public int getConditionSetSize() {
        return condition.getConditionSetSize();
    }

    @Override
    public int getConditionSetIndex() {
        return condition.getConditionSetIndex();
    }

    @Override
    public void updateDisplayString() {
        String s = String.format("Missing: %s[%tc] %dms GTE %dms", getCondition().getDataId(), time,
                (time - previousTime), getCondition().getInterval());
        setDisplayString(s);
    }

    @Override
    public MissingCondition getCondition() {
        return (MissingCondition) condition;
    }

    public long getPreviousTime() {
        return previousTime;
    }

    public void setPreviousTime(long previousTime) {
        this.previousTime = previousTime;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        MissingConditionEval that = (MissingConditionEval) o;

        if (previousTime != that.previousTime) return false;
        if (time != that.time) return false;
        return Objects.equals(condition, that.condition);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (condition != null ? condition.hashCode() : 0);
        result = 31 * result + (int) (previousTime ^ (previousTime >>> 32));
        result = 31 * result + (int) (time ^ (time >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "MissingConditionEval{" +
                "condition=" + condition +
                ", previousTime=" + previousTime +
                ", time=" + time +
                '}';
    }
}
