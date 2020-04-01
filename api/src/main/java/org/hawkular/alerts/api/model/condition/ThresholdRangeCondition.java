package org.hawkular.alerts.api.model.condition;

import java.util.Locale;
import java.util.Objects;

import org.hawkular.alerts.api.doc.DocModel;
import org.hawkular.alerts.api.doc.DocModelProperty;
import org.hawkular.alerts.api.model.trigger.Mode;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * A numeric threshold range condition.
 *
 * @author Jay Shaughnessy
 * @author Lucas Ponce
 */
@DocModel(description = "A numeric threshold range condition.")
public class ThresholdRangeCondition extends Condition {

    private static final long serialVersionUID = 1L;

    public enum Operator {
        INCLUSIVE("[", "]"), EXCLUSIVE("(", ")");

        private String low, high;

        Operator(String low, String high) {
            this.low = low;
            this.high = high;
        }

        public String getLow() {
            return low;
        }

        public String getHigh() {
            return high;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String dataId;

    @DocModelProperty(description = "Define whether low threshold value is inclusive or exclusive.",
            position = 0,
            required = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Operator operatorLow;

    @DocModelProperty(description = "Define whether high threshold value is inclusive or exclusive.",
            position = 1,
            required = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Operator operatorHigh;

    @DocModelProperty(description = "Low threshold of the range interval.",
            position = 2,
            required = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double thresholdLow;

    @DocModelProperty(description = "High threshold of the range interval.",
            position = 3,
            required = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double thresholdHigh;

    @DocModelProperty(description = "Flag to indicate if condition will match when value is within the range interval or " +
            "outside the range interval.",
            position = 4,
            required = true)
    @JsonInclude
    private boolean inRange;

    public ThresholdRangeCondition() {
        /*
            Default constructor is needed for JSON libraries in JAX-RS context.
         */
        this("", "", 1, 1, null, null, null, null, null, false);
    }

    public ThresholdRangeCondition(String tenantId, String triggerId,
            String dataId, Operator operatorLow, Operator operatorHigh,
            Double thresholdLow, Double thresholdHigh, boolean inRange) {

        this(tenantId, triggerId, Mode.FIRING, 1, 1, dataId, operatorLow, operatorHigh,
                thresholdLow, thresholdHigh, inRange);
    }

    /**
     * This constructor requires the tenantId be assigned prior to persistence. It can be used when
     * creating triggers via Rest, as the tenant will be assigned automatically.
     */
    public ThresholdRangeCondition(String triggerId, Mode triggerMode, String dataId, Operator operatorLow,
            Operator operatorHigh, Double thresholdLow, Double thresholdHigh, boolean inRange) {

        this("", triggerId, triggerMode, 1, 1, dataId, operatorLow, operatorHigh,
                thresholdLow, thresholdHigh, inRange);
    }

    public ThresholdRangeCondition(String tenantId, String triggerId, Mode triggerMode,
            String dataId, Operator operatorLow, Operator operatorHigh,
            Double thresholdLow, Double thresholdHigh, boolean inRange) {

        this(tenantId, triggerId, triggerMode, 1, 1, dataId, operatorLow, operatorHigh,
                thresholdLow, thresholdHigh, inRange);
    }

    public ThresholdRangeCondition(String tenantId, String triggerId, int conditionSetSize, int conditionSetIndex,
            String dataId, Operator operatorLow, Operator operatorHigh,
            Double thresholdLow, Double thresholdHigh, boolean inRange) {

        this(tenantId, triggerId, Mode.FIRING, conditionSetSize, conditionSetIndex, dataId, operatorLow, operatorHigh,
                thresholdLow, thresholdHigh, inRange);
    }

    /**
     * This constructor requires the tenantId be assigned prior to persistence. It can be used when
     * creating triggers via Rest, as the tenant will be assigned automatically.
     */
    public ThresholdRangeCondition(String triggerId, Mode triggerMode, int conditionSetSize,
            int conditionSetIndex, String dataId, Operator operatorLow, Operator operatorHigh,
            Double thresholdLow, Double thresholdHigh, boolean inRange) {

        this("", triggerId, triggerMode, conditionSetSize, conditionSetIndex, dataId, operatorLow, operatorHigh,
                thresholdLow, thresholdHigh, inRange);
    }

    public ThresholdRangeCondition(String tenantId, String triggerId, Mode triggerMode, int conditionSetSize,
            int conditionSetIndex, String dataId, Operator operatorLow, Operator operatorHigh,
            Double thresholdLow, Double thresholdHigh, boolean inRange) {
        super(tenantId, triggerId, triggerMode, conditionSetSize, conditionSetIndex, Type.RANGE);
        this.dataId = dataId;
        this.operatorLow = operatorLow;
        this.operatorHigh = operatorHigh;
        this.thresholdLow = thresholdLow;
        this.thresholdHigh = thresholdHigh;
        this.inRange = inRange;
        updateDisplayString();
    }

    public ThresholdRangeCondition(ThresholdRangeCondition condition) {
        super(condition);

        this.dataId = condition.getDataId();
        this.operatorHigh = condition.getOperatorHigh();
        this.operatorLow = condition.getOperatorLow();
        this.thresholdHigh = condition.getThresholdHigh();
        this.thresholdLow = condition.getThresholdLow();
        this.inRange = condition.isInRange();
    }

    @Override
    public String getDataId() {
        return dataId;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
    }

    public boolean isInRange() {
        return inRange;
    }

    public void setInRange(boolean inRange) {
        this.inRange = inRange;
    }

    public Operator getOperatorHigh() {
        return operatorHigh;
    }

    public void setOperatorHigh(Operator operatorHigh) {
        this.operatorHigh = operatorHigh;
    }

    public Operator getOperatorLow() {
        return operatorLow;
    }

    public void setOperatorLow(Operator operatorLow) {
        this.operatorLow = operatorLow;
    }

    public Double getThresholdHigh() {
        return thresholdHigh;
    }

    public void setThresholdHigh(Double thresholdHigh) {
        this.thresholdHigh = thresholdHigh;
    }

    public Double getThresholdLow() {
        return thresholdLow;
    }

    public void setThresholdLow(Double thresholdLow) {
        this.thresholdLow = thresholdLow;
    }

    public boolean match(double value) {
        boolean aboveLow = false;
        boolean belowHigh = false;

        switch (operatorLow) {
            case INCLUSIVE:
                aboveLow = value >= thresholdLow;
                break;
            case EXCLUSIVE:
                aboveLow = value > thresholdLow;
                break;
            default:
                throw new IllegalStateException("Unknown operatorLow: " + operatorLow.name());
        }

        if (!aboveLow) {
            return inRange ? false : true;
        }

        switch (operatorHigh) {
            case INCLUSIVE:
                belowHigh = value <= thresholdHigh;
                break;
            case EXCLUSIVE:
                belowHigh = value < thresholdHigh;
                break;
            default:
                throw new IllegalStateException("Unknown operatorHigh: " + operatorLow.name());
        }

        return (belowHigh == inRange);
    }

    @Override
    public void updateDisplayString() {
        String operatorLow = null == this.operatorLow ? null : this.operatorLow.getLow();
        String operatorHigh = null == this.operatorHigh ? null : this.operatorHigh.getHigh();
        String s = String.format(Locale.US, "%s %s %s%.2f , %.2f%s", this.dataId, (isInRange() ? "in" : "not in"),
                                 operatorLow,
                this.thresholdLow, this.thresholdHigh, operatorHigh);
        setDisplayString(s);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ThresholdRangeCondition that = (ThresholdRangeCondition) o;
        return inRange == that.inRange &&
                Objects.equals(dataId, that.dataId) &&
                operatorLow == that.operatorLow &&
                operatorHigh == that.operatorHigh &&
                Objects.equals(thresholdLow, that.thresholdLow) &&
                Objects.equals(thresholdHigh, that.thresholdHigh);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), dataId, operatorLow, operatorHigh, thresholdLow, thresholdHigh, inRange);
    }

    @Override
    public String toString() {
        return "ThresholdRangeCondition [triggerId='" + triggerId + "', " +
                "triggerMode=" + triggerMode + ", " +
                "dataId=" + (dataId == null ? null : '\'' + dataId + '\'') + ", " +
                "operatorLow=" + (operatorLow == null ? null : '\'' + operatorLow.toString() + '\'') + ", " +
                "operatorHigh=" + (operatorHigh == null ? null : '\'' + operatorHigh.toString() + '\'') + ", " +
                "thresholdLow=" + thresholdLow + ", " +
                "thresholdHigh=" + thresholdHigh + ", " +
                "inRange=" + inRange + "]";
    }

}
