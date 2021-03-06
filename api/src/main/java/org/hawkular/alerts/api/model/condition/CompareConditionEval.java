package org.hawkular.alerts.api.model.condition;

import java.util.Map;
import java.util.Objects;

import org.hawkular.alerts.api.doc.DocModel;
import org.hawkular.alerts.api.doc.DocModelProperty;
import org.hawkular.alerts.api.model.condition.Condition.Type;
import org.hawkular.alerts.api.model.data.Data;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * An evaluation state for compare condition.
 *
 * @author Jay Shaughnessy
 * @author Lucas Ponce
 */
@DocModel(description = "An evaluation state for compare condition.")
public class CompareConditionEval extends ConditionEval<CompareCondition> {

    private static final long serialVersionUID = 1L;

    @DocModelProperty(description = "Numeric value used for dataId.",
            position = 1)
    @JsonInclude(Include.NON_NULL)
    private Double value1;

    @DocModelProperty(description = "Numeric value used for data2Id.",
            position = 2)
    @JsonInclude(Include.NON_NULL)
    private Double value2;

    @DocModelProperty(description = "Properties defined by the user at Data level on the data2Id used for this evaluation.",
            position = 3)
    @JsonInclude(Include.NON_EMPTY)
    protected Map<String, String> context2;

    public CompareConditionEval() {
        super(Type.COMPARE, false, 0, null);
        this.condition = null;
        this.value1 = null;
        this.value2 = null;
        this.context2 = null;
    }

    public CompareConditionEval(CompareCondition condition, Data data1, Data data2) {
        super(Type.COMPARE, condition.match(Double.parseDouble(data1.getValue()), Double.parseDouble(data2.getValue())),
                (Math.min(data2.getTimestamp(), data1.getTimestamp())), // use the earlier ts
                data1.getContext());
        setCondition(condition);
        this.value1 = Double.valueOf(data1.getValue());
        this.value2 = Double.valueOf(data2.getValue());
        this.context2 = data2.getContext();
    }

    public Double getValue1() {
        return value1;
    }

    public void setValue1(Double value1) {
        this.value1 = value1;
    }

    public Double getValue2() {
        return value2;
    }

    public void setValue2(Double value2) {
        this.value2 = value2;
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

    public Map<String, String> getContext2() {
        return context2;
    }

    public void setContext2(Map<String, String> context2) {
        this.context2 = context2;
    }

    @Override
    public void updateDisplayString() {
        String s = String.format("Compare: %s[%.2f] %s %.2f%% %s[%.2f]", condition.getDataId(), value1,
                getCondition().getOperator().name(), (100 * getCondition().getData2Multiplier()), getCondition().getData2Id(),
                value2);
        super.setDisplayString(s);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        if (!super.equals(o))
            return false;

        CompareConditionEval that = (CompareConditionEval) o;

        if (!Objects.equals(condition, that.condition))
            return false;
        if (!Objects.equals(value1, that.value1))
            return false;
        if (!Objects.equals(value2, that.value2))
            return false;
        return Objects.equals(context2, that.context2);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (condition != null ? condition.hashCode() : 0);
        result = 31 * result + (value1 != null ? value1.hashCode() : 0);
        result = 31 * result + (value2 != null ? value2.hashCode() : 0);
        result = 31 * result + (context2 != null ? context2.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CompareConditionEval [condition=" + condition + ", value1=" + value1 + ", value2=" + value2
                + ", match=" + match + ", evalTimestamp=" + evalTimestamp + ", dataTimestamp=" + dataTimestamp + "]";
    }

}
