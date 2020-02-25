package org.hawkular.alerts.api.model.condition;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

import org.hawkular.alerts.api.doc.DocModel;
import org.hawkular.alerts.api.doc.DocModelProperty;
import org.hawkular.alerts.api.json.JacksonDeserializer;
import org.hawkular.alerts.api.model.condition.Condition.Type;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * An evaluation state of a specific condition.
 *
 * @author Jay Shaughnessy
 * @author Lucas Ponce
 */
@DocModel(description = "A base class to represent an evaluation state of a specific condition.", subTypes = {
        AvailabilityConditionEval.class, CompareConditionEval.class, EventConditionEval.class,
        ExternalConditionEval.class, MissingConditionEval.class, RateConditionEval.class, StringConditionEval.class,
        ThresholdConditionEval.class, ThresholdRangeConditionEval.class })
@JsonDeserialize(using = JacksonDeserializer.ConditionEvalDeserializer.class)
public abstract class ConditionEval implements Serializable {

    private static final long serialVersionUID = 1L;

    // result of the condition evaluation
    @DocModelProperty(description = "Result of the condition evaluation.", position = 0)
    @JsonIgnore
    protected boolean match;

    // time of condition evaluation (i.e. creation time)
    @DocModelProperty(description = "Time of condition evaluation.", position = 1)
    @JsonInclude
    protected long evalTimestamp;

    // time stamped on the data used in the eval
    @DocModelProperty(description = "Time stamped on the data used in the evaluation.", position = 2)
    @JsonInclude
    protected long dataTimestamp;

    @DocModelProperty(description = "The type of the condition eval defined. Each type has its specific properties defined "
            +
            "on its subtype of condition eval.", position = 3)
    @JsonInclude
    protected Condition.Type type;

    @DocModelProperty(description = "Properties defined by the user at Data level on the dataId used for this evaluation.", position = 4)
    @JsonInclude(Include.NON_EMPTY)
    protected Map<String, String> context;

    @DocModelProperty(description = "A canonical display string of the evaluation (the result of a call to #getLog()).", position = 5)
    @JsonInclude(Include.NON_EMPTY)
    protected String displayString;

    @DocModelProperty(description = "Condition linked with this state.", position = 6)
    @JsonInclude(Include.NON_NULL)
    protected Condition condition;

    public ConditionEval() {
        // for json assembly
    }

    public ConditionEval(Type type, boolean match, long dataTimestamp, Map<String, String> context) {
        this.type = type;
        this.match = match;
        this.dataTimestamp = dataTimestamp;
        this.evalTimestamp = System.currentTimeMillis();
        this.context = context;
        this.displayString = null; // for construction speed, lazily update when requested or when serialized
    }

    public boolean isMatch() {
        return match;
    }

    public void setMatch(boolean match) {
        this.match = match;
    }

    public long getEvalTimestamp() {
        return evalTimestamp;
    }

    public void setEvalTimestamp(long evalTimestamp) {
        this.evalTimestamp = evalTimestamp;
        condition.setLastEvaluation(evalTimestamp);
    }

    public long getDataTimestamp() {
        return dataTimestamp;
    }

    public void setDataTimestamp(long dataTimestamp) {
        this.dataTimestamp = dataTimestamp;
    }

    public Condition.Type getType() {
        return type;
    }

    public void setType(Condition.Type type) {
        this.type = type;
    }

    public Map<String, String> getContext() {
        return context;
    }

    public void setContext(Map<String, String> context) {
        this.context = context;
    }

    public String getDisplayString() {
        if (null == this.displayString) {
            updateDisplayString();
        }
        return this.displayString;
    }

    public void setDisplayString(String displayString) {
        this.displayString = displayString;
    }

    @JsonIgnore
    public abstract String getTenantId();

    @JsonIgnore
    public abstract String getTriggerId();

    @JsonIgnore
    public abstract int getConditionSetSize();

    @JsonIgnore
    public abstract int getConditionSetIndex();

    /**
     * @return The condition expression with the values used to determine the match. Note that this
     * String does not include whether the match is true or false.  That can be determined via {@link #isMatch()}.
     */
    @JsonIgnore
    public abstract void updateDisplayString();

    public Condition getCondition() {
        return condition;
    }

    public void setCondition(Condition condition) {
        this.condition = condition;
        condition.setLastEvaluation(this.evalTimestamp);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        ConditionEval that = (ConditionEval) o;

        if (evalTimestamp != that.evalTimestamp)
            return false;
        if (dataTimestamp != that.dataTimestamp)
            return false;
        if (type != that.type)
            return false;
        return Objects.equals(context, that.context);

    }

    @Override
    public int hashCode() {
        int result = (int) (evalTimestamp ^ (evalTimestamp >>> 32));
        result = 31 * result + (int) (dataTimestamp ^ (dataTimestamp >>> 32));
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (context != null ? context.hashCode() : 0);
        return result;
    }
}
