package org.hawkular.alerts.engine.impl.ispn.model;

import com.google.common.collect.Multimap;
import org.hawkular.alerts.api.model.trigger.Trigger;
import org.hibernate.search.annotations.*;
import org.hibernate.search.bridge.TwoWayFieldBridge;
import org.hibernate.search.bridge.builtin.BooleanBridge;
import org.hibernate.search.bridge.builtin.MapBridge;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Jay Shaughnessy
 * @author Lucas Ponce
 */
@Indexed(index = "trigger")
public class IspnTrigger implements Serializable {

    @Field(store = Store.YES, analyze = Analyze.NO)
    private String tenantId;

    @Field(store = Store.YES, analyze = Analyze.NO)
    private String triggerId;

    @Field(name = "")
//    @Field(name = "", store = Store.YES, analyze = Analyze.NO)
    @FieldBridge(impl = TagsBridge.class)
//    @GenericField(extraction = @ContainerExtraction(BuiltinContainerExtractors.MAP_KEY))
//    @IndexedEmbedded
//    @FieldBridge(impl = MapBridge.class)
    private Multimap<String, String> tags;
//    private Set<Tag> tags;

    @Field(store = Store.YES, analyze = Analyze.NO)
    private String memberOf;

    @Field(store = Store.YES, analyze = Analyze.NO)
    @FieldBridge(impl = BooleanBridge.class)
    private boolean enabled;

    private Trigger trigger;

    public IspnTrigger() {
    }

    public IspnTrigger(Trigger trigger) {
        updateTrigger(trigger);
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public Trigger getTrigger() {
        return new Trigger(this.trigger);
    }

    public void setTrigger(Trigger trigger) {
        updateTrigger(trigger);
    }

    public String getTriggerId() {
        return triggerId;
    }

    public void setTriggerId(String triggerId) {
        this.triggerId = triggerId;
    }

    private void updateTrigger(Trigger trigger) {
        if (trigger == null) {
            throw new IllegalArgumentException("trigger must be not null");
        }
        this.trigger = new Trigger(trigger);
        this.tenantId = trigger.getTenantId();
        this.triggerId = trigger.getId();
        this.memberOf = trigger.getMemberOf();

        // Workaround for a bug in Hibernate search
        this.enabled = trigger.isEnabled();

//        this.tags = new HashSet<>();
//        for (Map.Entry<String, String> tEntry : this.trigger.getTags().entrySet()) {
//            Tag t = new Tag(tEntry.getKey(), tEntry.getValue());
//            this.tags.add(t);
//        }

        this.tags = this.trigger.getTags();

        // These will override any tags with name "description" or "name"
//        this.tags.put("description", trigger.getDescription());
//        this.tags.put("name", trigger.getName());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IspnTrigger that = (IspnTrigger) o;

        if (tenantId != null ? !tenantId.equals(that.tenantId) : that.tenantId != null) return false;
        if (triggerId != null ? !triggerId.equals(that.triggerId) : that.triggerId != null) return false;
        if (memberOf != null ? !memberOf.equals(that.memberOf) : that.memberOf != null) return false;
        if (tags != null ? !tags.equals(that.tags) : that.tags != null) return false;
        return trigger != null ? trigger.equals(that.trigger) : that.trigger == null;
    }

    @Override
    public int hashCode() {
        int result = tenantId != null ? tenantId.hashCode() : 0;
        result = 31 * result + (triggerId != null ? triggerId.hashCode() : 0);
        result = 31 * result + (memberOf != null ? memberOf.hashCode() : 0);
        result = 31 * result + (tags != null ? tags.hashCode() : 0);
        result = 31 * result + (trigger != null ? trigger.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "IspnTrigger{" +
                "tenantId='" + tenantId + '\'' +
                ", triggerId='" + triggerId + '\'' +
                ", tags=" + tags +
                ", trigger=" + trigger +
                '}';
    }
}
