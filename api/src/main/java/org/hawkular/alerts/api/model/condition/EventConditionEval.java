package org.hawkular.alerts.api.model.condition;

import org.hawkular.alerts.api.doc.DocModel;
import org.hawkular.alerts.api.doc.DocModelProperty;
import org.hawkular.alerts.api.model.condition.Condition.Type;
import org.hawkular.alerts.api.model.event.Event;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.util.Objects;

/**
 * An evaluation state for event condition.
 *
 * @author Jay Shaughnessy
 * @author Lucas Ponce
 */
@DocModel(description = "An evaluation state for event condition.")
public class EventConditionEval extends ConditionEval {

    private static final long serialVersionUID = 1L;

    @DocModelProperty(description = "Event value used for dataId.",
            position = 1)
    @JsonInclude(Include.NON_NULL)
    private Event value;

    public EventConditionEval() {
        super(Type.EVENT, false, 0, null);
        this.condition = null;
        this.value = null;
    }

    public EventConditionEval(EventCondition condition, Event value) {
        super(Type.EVENT, condition.match(value), value.getCtime(), value.getContext());
        setCondition(condition);
        this.value = value;
    }

    @Override
    public EventCondition getCondition() {
        return (EventCondition) condition;
    }

    public Event getValue() {
        return value;
    }

    public void setValue(Event value) {
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
        String s = String.format("Event: %s[%s] matches [%s]", getCondition().getDataId(), value, getCondition().getExpression());
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

        EventConditionEval that = (EventConditionEval) o;

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

    @Override public String toString() {
        return "EventConditionEval{" +
                "condition=" + condition +
                ", value=" + value +
                '}';
    }
}
