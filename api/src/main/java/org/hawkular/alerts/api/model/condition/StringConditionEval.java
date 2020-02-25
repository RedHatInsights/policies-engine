package org.hawkular.alerts.api.model.condition;

import org.hawkular.alerts.api.doc.DocModel;
import org.hawkular.alerts.api.doc.DocModelProperty;
import org.hawkular.alerts.api.model.condition.Condition.Type;
import org.hawkular.alerts.api.model.data.Data;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.util.Objects;

/**
 * An evaluation state for string condition.
 *
 * @author Jay Shaughnessy
 * @author Lucas Ponce
 */
@DocModel(description = "An evaluation state for string condition.")
public class StringConditionEval extends ConditionEval {

    private static final long serialVersionUID = 1L;

    @DocModelProperty(description = "String value for dataId used in the evaluation.")
    @JsonInclude(Include.NON_NULL)
    private String value;

    public StringConditionEval() {
        super(Type.STRING, false, 0, null);
        this.condition = null;
        this.value = null;
    }

    public StringConditionEval(StringCondition condition, Data data) {
        super(Type.STRING, condition.match(data.getValue()), data.getTimestamp(), data.getContext());
        setCondition(condition);
        this.value = data.getValue();
    }

    @Override
    public StringCondition getCondition() {
        return (StringCondition) condition;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
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
        String s = String.format("String: %s[%s] %s [%s]%s", getCondition().getDataId(), value,
                getCondition().getOperator().name(), getCondition().getPattern(),
                (getCondition().isIgnoreCase() ? " Ignoring Case" : ""));
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

        StringConditionEval that = (StringConditionEval) o;

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
        return "StringConditionEval [condition=" + condition + ", value=" + value + ", match=" + match
                + ", evalTimestamp=" + evalTimestamp + ", dataTimestamp=" + dataTimestamp + "]";
    }

}
