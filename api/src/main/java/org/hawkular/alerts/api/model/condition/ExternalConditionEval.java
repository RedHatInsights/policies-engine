package org.hawkular.alerts.api.model.condition;

import org.hawkular.alerts.api.doc.DocModel;
import org.hawkular.alerts.api.doc.DocModelProperty;
import org.hawkular.alerts.api.model.condition.Condition.Type;
import org.hawkular.alerts.api.model.data.Data;
import org.hawkular.alerts.api.model.event.Event;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.util.Objects;

/**
 * An evaluation state for an external condition.  Note that external conditions may report a <code>Data</code> value
 * or an <code>Event</code>.
 *
 * @author Jay Shaughnessy
 * @author Lucas Ponce
 */
@DocModel(description = "An evaluation state for an external condition. + \n" +
        "Note that external conditions may report a Data value or an Event.")
public class ExternalConditionEval extends ConditionEval {

    private static final long serialVersionUID = 1L;

    @DocModelProperty(description = "String value used for dataId.",
            position = 0)
    @JsonInclude(Include.NON_NULL)
    private String value;

    @DocModelProperty(description = "Event value used for dataId.",
            position = 1)
    @JsonInclude(Include.NON_NULL)
    private Event event;

    public ExternalConditionEval() {
        super(Type.EXTERNAL, false, 0, null);
        this.condition = null;
        this.value = null;
        this.event = null;
    }

    public ExternalConditionEval(ExternalCondition condition, Event event) {
        super(Type.EXTERNAL, condition.match(event.getText()), event.getCtime(), event.getContext());
        setCondition(condition);
        this.event = event;
    }

    public ExternalConditionEval(ExternalCondition condition, Data data) {
        super(Type.EXTERNAL, condition.match(data.getValue()), data.getTimestamp(), data.getContext());
        setCondition(condition);
        this.value = data.getValue();
    }

    @Override
    public ExternalCondition getCondition() {
        return (ExternalCondition) condition;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
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
        String s = String.format("External[%s]: %s[%s] matches [%s]", getCondition().getAlerterId(),
                getCondition().getDataId(), (value != null ? value : event.toString()), getCondition().getExpression());
        setDisplayString(s);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        ExternalConditionEval that = (ExternalConditionEval) o;

        if (!Objects.equals(condition, that.condition)) return false;
        if (!Objects.equals(value, that.value)) return false;
        return Objects.equals(event, that.event);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (condition != null ? condition.hashCode() : 0);
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + (event != null ? event.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ExternalConditionEval{" +
                "condition=" + condition +
                ", value='" + value + '\'' +
                ", event=" + event +
                '}';
    }
}
