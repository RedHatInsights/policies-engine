package com.redhat.cloud.policies.engine.history.alert.entities;

import com.google.common.collect.Multimap;
import com.redhat.cloud.policies.engine.history.alert.converters.FactsConverter;
import com.redhat.cloud.policies.engine.history.alert.converters.TagsConverter;
import com.redhat.cloud.policies.engine.history.alert.converters.ContextConverter;
import com.redhat.cloud.policies.engine.history.alert.converters.EvalSetsConverter;
import org.hawkular.alerts.api.model.condition.ConditionEval;
import org.hawkular.alerts.api.model.dampening.Dampening;
import org.hawkular.alerts.api.model.trigger.Trigger;

import javax.persistence.Convert;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static javax.persistence.DiscriminatorType.STRING;

@Entity
@Table(name = "event")
@DiscriminatorColumn(name = "discriminator", discriminatorType = STRING)
public abstract class EventBaseEntity {

    @EmbeddedId
    private EventBaseEntityId id;

    private LocalDateTime expiresAt;

    @NotNull
    private String eventType;

    @NotNull
    private Long ctime;

    private String datasource;

    @NotNull
    private String dataId;

    @NotNull
    private String category;

    @NotNull
    private String text;

    @Convert(converter = ContextConverter.class)
    private Map<String, String> context;

    @Convert(converter = TagsConverter.class)
    private Multimap<String, String> tags;

    private Trigger trigger;

    private Dampening dampening;

    @Convert(converter = EvalSetsConverter.class)
    private List<Set<ConditionEval>> evalSets;

    @Convert(converter = FactsConverter.class)
    private Map<String, Object> facts;

    public EventBaseEntity() {
        id = new EventBaseEntityId();
    }

    public String getTenantId() {
        return id.getTenantId();
    }

    public void setTenantId(String tenantId) {
        id.setTenantId(tenantId);
    }

    public String getEventId() {
        return id.getEventId();
    }

    public void setEventId(String eventId) {
        id.setEventId(eventId);
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Long getCtime() {
        return ctime;
    }

    public void setCtime(Long ctime) {
        this.ctime = ctime;
    }

    public String getDatasource() {
        return datasource;
    }

    public void setDatasource(String datasource) {
        this.datasource = datasource;
    }

    public String getDataId() {
        return dataId;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Map<String, String> getContext() {
        return context;
    }

    public void setContext(Map<String, String> context) {
        this.context = context;
    }

    public Multimap<String, String> getTags() {
        return tags;
    }

    public void setTags(Multimap<String, String> tags) {
        this.tags = tags;
    }

    public Trigger getTrigger() {
        return trigger;
    }

    public void setTrigger(Trigger trigger) {
        this.trigger = trigger;
    }

    public Dampening getDampening() {
        return dampening;
    }

    public void setDampening(Dampening dampening) {
        this.dampening = dampening;
    }

    public List<Set<ConditionEval>> getEvalSets() {
        return evalSets;
    }

    public void setEvalSets(List<Set<ConditionEval>> evalSets) {
        this.evalSets = evalSets;
    }

    public Map<String, Object> getFacts() {
        return facts;
    }

    public void setFacts(Map<String, Object> facts) {
        this.facts = facts;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof EventBaseEntity) {
            EventBaseEntity other = (EventBaseEntity) o;
            return Objects.equals(id, other.id);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
