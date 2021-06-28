package com.redhat.cloud.policies.engine.history.alert.entities;

import com.redhat.cloud.policies.engine.history.alert.converters.ConditionEvalsConverter;
import com.redhat.cloud.policies.engine.history.alert.converters.LifecyclesConverter;
import org.hawkular.alerts.api.model.Lifecycle;
import org.hawkular.alerts.api.model.Severity;
import org.hawkular.alerts.api.model.condition.ConditionEval;

import javax.persistence.Convert;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.util.List;
import java.util.Set;

import static org.hawkular.alerts.api.model.event.Alert.Status;

@Entity
@DiscriminatorValue("alert")
public class AlertEntity extends EventBaseEntity {

    private Severity severity;

    private Status status;

    @Convert(converter = LifecyclesConverter.class)
    private List<Lifecycle> lifecycle;

    @Convert(converter = ConditionEvalsConverter.class)
    private List<Set<ConditionEval>> resolvedEvalSets;

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public List<Lifecycle> getLifecycle() {
        return lifecycle;
    }

    public void setLifecycle(List<Lifecycle> lifecycle) {
        this.lifecycle = lifecycle;
    }

    public List<Set<ConditionEval>> getResolvedEvalSets() {
        return resolvedEvalSets;
    }

    public void setResolvedEvalSets(List<Set<ConditionEval>> resolvedEvalSets) {
        this.resolvedEvalSets = resolvedEvalSets;
    }
}
