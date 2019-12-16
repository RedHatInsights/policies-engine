package org.hawkular.alerts.api.model.condition;

import org.hawkular.alerts.api.doc.DocModel;
import org.hawkular.alerts.api.doc.DocModelProperty;
import org.hawkular.alerts.api.model.trigger.Mode;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * A numeric threshold condition.
 *
 * @author Jay Shaughnessy
 * @author Lucas Ponce
 */
@DocModel(description = "A numeric threshold condition.")
public class ThresholdCondition extends Condition {

    private static final long serialVersionUID = 1L;

    public enum Operator {
        LT, GT, LTE, GTE
    }

    @JsonInclude(Include.NON_NULL)
    private String dataId;

    @DocModelProperty(description = "Compare operator [LT (<), GT (>), LTE (<=), GTE (>=)].",
            position = 0,
            required = true)
    @JsonInclude(Include.NON_NULL)
    private Operator operator;

    @DocModelProperty(description = "Condition threshold.",
            position = 1,
            required = true)
    @JsonInclude(Include.NON_NULL)
    private Double threshold;

    public ThresholdCondition() {
        /*
            Default constructor is needed for JSON libraries in JAX-RS context.
         */
        this("", "", 1, 1, null, null, null);
    }

    public ThresholdCondition(String tenantId, String triggerId,
            String dataId, Operator operator, Double threshold) {

        this(tenantId, triggerId, Mode.FIRING, 1, 1, dataId, operator, threshold);
    }

    /**
     * This constructor requires the tenantId be assigned prior to persistence. It can be used when
     * creating triggers via Rest, as the tenant will be assigned automatically.
     */
    public ThresholdCondition(String triggerId, Mode triggerMode, String dataId, Operator operator, Double threshold) {

        this("", triggerId, triggerMode, 1, 1, dataId, operator, threshold);
    }

    public ThresholdCondition(String tenantId, String triggerId, Mode triggerMode,
            String dataId, Operator operator, Double threshold) {

        this(tenantId, triggerId, triggerMode, 1, 1, dataId, operator, threshold);
    }

    public ThresholdCondition(String tenantId, String triggerId, int conditionSetSize, int conditionSetIndex,
            String dataId, Operator operator, Double threshold) {

        this(tenantId, triggerId, Mode.FIRING, conditionSetSize, conditionSetIndex, dataId, operator, threshold);
    }

    /**
     * This constructor requires the tenantId be assigned prior to persistence. It can be used when
     * creating triggers via Rest, as the tenant will be assigned automatically.
     */
    public ThresholdCondition(String triggerId, Mode triggerMode, int conditionSetSize,
            int conditionSetIndex, String dataId, Operator operator, Double threshold) {
        this("", triggerId, triggerMode, conditionSetSize, conditionSetIndex, dataId, operator, threshold);
    }

    public ThresholdCondition(String tenantId, String triggerId, Mode triggerMode, int conditionSetSize,
            int conditionSetIndex, String dataId, Operator operator, Double threshold) {
        super(tenantId, triggerId, triggerMode, conditionSetSize, conditionSetIndex, Type.THRESHOLD);
        this.dataId = dataId;
        this.operator = operator;
        this.threshold = threshold;
        updateDisplayString();
    }

    public ThresholdCondition(ThresholdCondition condition) {
        super(condition);

        this.dataId = condition.getDataId();
        this.operator = condition.getOperator();
        this.threshold = condition.getThreshold();
    }

    @Override
    public String getDataId() {
        return dataId;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
    }

    public Operator getOperator() {
        return operator;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    public Double getThreshold() {
        return threshold;
    }

    public void setThreshold(Double threshold) {
        this.threshold = threshold;
    }

    public boolean match(double value) {
        if (threshold == null) {
            throw new IllegalStateException("Invalid threshold for condition: " + this.toString());
        }
        switch (operator) {
            case LT:
                return value < threshold;
            case GT:
                return value > threshold;
            case LTE:
                return value <= threshold;
            case GTE:
                return value >= threshold;
            default:
                throw new IllegalStateException("Unknown operator: " + operator.name());
        }
    }

    @Override
    public void updateDisplayString() {
        String operator = null == this.operator ? null : this.operator.name();
        String s = String.format("%s %s %.2f", this.dataId, operator, this.threshold);
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

        ThresholdCondition that = (ThresholdCondition) o;

        if (dataId != null ? !dataId.equals(that.dataId) : that.dataId != null)
            return false;
        if (operator != that.operator)
            return false;
        if (threshold != null ? !threshold.equals(that.threshold) : that.threshold != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (dataId != null ? dataId.hashCode() : 0);
        result = 31 * result + (operator != null ? operator.hashCode() : 0);
        result = 31 * result + (threshold != null ? threshold.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ThresholdCondition [triggerId='" + triggerId + "', " +
                "triggerMode=" + triggerMode + ", " +
                "dataId=" + (dataId == null ? null : '\'' + dataId + '\'') + ", " +
                "operator=" + (operator == null ? null : '\'' + operator.toString() + '\'') + ", " +
                "threshold=" + threshold + "]";
    }

}
