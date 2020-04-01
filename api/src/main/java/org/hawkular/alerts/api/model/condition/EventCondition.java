package org.hawkular.alerts.api.model.condition;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.redhat.cloud.policies.api.model.condition.expression.ExprParser;
import org.hawkular.alerts.api.doc.DocModel;
import org.hawkular.alerts.api.doc.DocModelProperty;
import org.hawkular.alerts.api.model.event.Event;
import org.hawkular.alerts.api.model.trigger.Mode;

import java.util.Objects;

import static org.hawkular.alerts.api.util.Util.isEmpty;

/**
 * An <code>EventCondition</code> is used for condition evaluations over Event data using expressions.
 *
 * Empty expression will return true, non valid will throw InvalidArgumentExpression
 *
 */
@DocModel(description = "An EventCondition is used for condition evaluations over Event data using expressions.\n")
public class EventCondition extends Condition {

    private static final long serialVersionUID = 1L;

    @JsonInclude
    private String dataId;

    @DocModelProperty(description = "Event expression used for this condition.",
            position = 0,
            required = false)
    @JsonInclude
    private String expression;

    public EventCondition() {
        this("", "", Mode.FIRING, 1, 1, null, null);
    }

    public EventCondition(String tenantId, String triggerId, String dataId, String expression) {
        this(tenantId, triggerId, Mode.FIRING, 1, 1, dataId, expression);
    }

    public EventCondition(String tenantId, String triggerId, Mode triggerMode, String dataId) {
        this(tenantId, triggerId, triggerMode, 1, 1, dataId, null);
    }

    /**
     * This constructor requires the tenantId be assigned prior to persistence. It can be used when
     * creating triggers via Rest, as the tenant will be assigned automatically.
     */
    public EventCondition(String triggerId, Mode triggerMode, String dataId) {
        this("", triggerId, triggerMode, 1, 1, dataId, null);
    }

    public EventCondition(String triggerId, Mode triggerMode, String dataId, String expression) {
        this("", triggerId, triggerMode, 1, 1, dataId, expression);
    }

    public EventCondition(String tenantId, String triggerId, Mode triggerMode, String dataId, String expression) {
        this(tenantId, triggerId, triggerMode, 1, 1, dataId, expression);
    }

    public EventCondition(String tenantId, String triggerId, int conditionSetSize, int conditionSetIndex,
            String dataId) {
        this(tenantId, triggerId, Mode.FIRING, conditionSetSize, conditionSetIndex, dataId, null);
    }

    public EventCondition(String tenantId, String triggerId, int conditionSetSize, int conditionSetIndex,
            String dataId, String expression) {
        this(tenantId, triggerId, Mode.FIRING, conditionSetSize, conditionSetIndex, dataId, expression);
    }

    public EventCondition(String tenantId, String triggerId, Mode triggerMode, int conditionSetSize,
            int conditionSetIndex, String dataId) {
        this(tenantId, triggerId, triggerMode, conditionSetSize, conditionSetIndex, dataId, null);
    }

    /**
     * This constructor requires the tenantId be assigned prior to persistence. It can be used when
     * creating triggers via Rest, as the tenant will be assigned automatically.
     */
    public EventCondition(String triggerId, Mode triggerMode, int conditionSetSize,
            int conditionSetIndex, String dataId, String expression) {
        this("", triggerId, triggerMode, conditionSetSize, conditionSetIndex, dataId, expression);
    }

    public EventCondition(String tenantId, String triggerId, Mode triggerMode, int conditionSetSize,
            int conditionSetIndex, String dataId, String expression) {
        super(tenantId, triggerId, triggerMode, conditionSetSize, conditionSetIndex, Type.EVENT);
        this.dataId = dataId;
        this.expression = expression;
        updateDisplayString();
        validate();
    }

    public EventCondition(EventCondition condition) {
        super(condition);

        this.dataId = condition.getDataId();
        this.expression = condition.getExpression();
        validate();
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
    }

    @Override
    public String getDataId() {
        return dataId;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        if(expression != null && !expression.isEmpty()) {
            ExprParser.validate(expression);
        }
        this.expression = expression;
    }

    public boolean match(Event value) {
        if (null == value) {
            return false;
        }
        if (isEmpty(expression)) {
            return true;
        }

        return ExprParser.evaluate(value, expression);
    }

    @Override
    public void validate() {
        if(this.expression != null && !expression.isEmpty()) {
            ExprParser.validate(this.expression);
        }
    }

    @Override
    public void updateDisplayString() {
        String s = String.format("%s matches [%s]", this.dataId, expression);
        setDisplayString(s);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        EventCondition that = (EventCondition) o;
        return Objects.equals(dataId, that.dataId) &&
                Objects.equals(expression, that.expression);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), dataId, expression);
    }

    @Override
    public String toString() {
        return "EventCondition{" +
                "dataId='" + dataId + '\'' +
                ",expression='" + expression + '\'' +
                '}';
    }
}
