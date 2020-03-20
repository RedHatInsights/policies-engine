package org.hawkular.alerts.api.model.condition;

import org.hawkular.alerts.api.doc.DocModel;
import org.hawkular.alerts.api.doc.DocModelProperty;
import org.hawkular.alerts.api.model.condition.Condition.Type;
import org.hawkular.alerts.api.model.data.AvailabilityType;
import org.hawkular.alerts.api.model.data.Data;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.util.Objects;

/**
 * An evaluation state for availability condition.
 *
 * @author Jay Shaughnessy
 * @author Lucas Ponce
 */
@DocModel(description = "An evaluation state for availability condition.")
public class AvailabilityConditionEval extends ConditionEval<AvailabilityCondition> {

    private static final long serialVersionUID = 1L;

    @DocModelProperty(description = "Availability value used for dataId.")
    @JsonInclude(Include.NON_NULL)
    private AvailabilityType value;

    public AvailabilityConditionEval() {
        super(Type.AVAILABILITY, false, 0, null);
        this.condition = null;
        this.value = null;
    }

    public AvailabilityConditionEval(AvailabilityCondition condition, Data avail) {
        super(Type.AVAILABILITY, condition.match(AvailabilityType.valueOf(avail.getValue())), avail.getTimestamp(),
                avail.getContext());
        setCondition(condition);
        this.value = AvailabilityType.valueOf(avail.getValue());
    }

    public AvailabilityType getValue() {
        return value;
    }

    public void setValue(AvailabilityType value) {
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
        String s = String.format("Avail: %s[%s] is %s", getCondition().getDataId(), value.name(),
                getCondition().getOperator().name());
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

        AvailabilityConditionEval that = (AvailabilityConditionEval) o;

        if (!Objects.equals(condition, that.condition))
            return false;
        return value == that.value;
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
        return "AvailabilityConditionEval [condition=" + condition + ", value=" + value + ", match=" + match
                + ", evalTimestamp=" + evalTimestamp + ", dataTimestamp=" + dataTimestamp + "]";
    }

}
