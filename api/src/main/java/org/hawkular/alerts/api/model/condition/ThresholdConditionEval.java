package org.hawkular.alerts.api.model.condition;

import org.hawkular.alerts.api.doc.DocModel;
import org.hawkular.alerts.api.doc.DocModelProperty;
import org.hawkular.alerts.api.model.condition.Condition.Type;
import org.hawkular.alerts.api.model.data.Data;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.util.Objects;

/**
 * An evaluation state for threshold condition.
 *
 * @author Jay Shaughnessy
 * @author Lucas Ponce
 */
@DocModel(description = "An evaluation state for threshold condition.")
public class ThresholdConditionEval extends ConditionEval {

    private static final long serialVersionUID = 1L;

    @DocModelProperty(description = "Numeric value for dataId used in the evaluation.")
    @JsonInclude(Include.NON_NULL)
    private Double value;

    public ThresholdConditionEval() {
        super(Type.THRESHOLD, false, 0, null);
        this.value = Double.NaN;
    }

    public ThresholdConditionEval(ThresholdCondition condition, Data data) {
        super(Type.THRESHOLD, condition.match(Double.parseDouble(data.getValue())), data.getTimestamp(),
                data.getContext());
        setCondition(condition);
        this.value = Double.valueOf(data.getValue());
    }

    @Override
    public ThresholdCondition getCondition() {
        return (ThresholdCondition) condition;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
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
        String s = String.format("Threshold: %s[%.2f] %s %.2f", getCondition().getDataId(), value,
                getCondition().getOperator().name(), getCondition().getThreshold());
        setDisplayString(s);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        if (!super.equals(o))
            return false;

        ThresholdConditionEval that = (ThresholdConditionEval) o;

        if (!Objects.equals(condition, that.condition))
            return false;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (condition != null ? condition.hashCode() : 0);
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ThresholdConditionEval [condition=" + condition + ", value=" + value + ", match=" + match
                + ", evalTimestamp=" + evalTimestamp + ", dataTimestamp=" + dataTimestamp + "]";
    }

}
