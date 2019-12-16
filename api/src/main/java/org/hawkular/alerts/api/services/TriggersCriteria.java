package org.hawkular.alerts.api.services;

import static org.hawkular.alerts.api.util.Util.isEmpty;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Query criteria for fetching Triggers.
 * @author jay shaughnessy
 * @author lucas ponce
 */
public class TriggersCriteria {
    String triggerId = null;
    Collection<String> triggerIds = null;
    Map<String, String> tags = null;
    boolean thin = false;

    public TriggersCriteria() {
        super();
    }

    public String getTriggerId() {
        return triggerId;
    }

    public void setTriggerId(String triggerId) {
        this.triggerId = triggerId;
    }

    public Collection<String> getTriggerIds() {
        return triggerIds;
    }

    public void setTriggerIds(Collection<String> triggerIds) {
        this.triggerIds = triggerIds;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    public void addTag(String name, String value) {
        if (null == tags) {
            tags = new HashMap<>();
        }
        tags.put(name, value);
    }

    public boolean isThin() {
        return thin;
    }

    public void setThin(boolean thin) {
        this.thin = thin;
    }

    public boolean hasTagCriteria() {
        return (null != tags && !tags.isEmpty());
    }

    public boolean hasTriggerIdCriteria() {
        return !isEmpty(triggerId) || !isEmpty(triggerIds);
    }

    public boolean hasCriteria() {
        return hasTriggerIdCriteria()
                || hasTagCriteria();
    }

    @Override
    public String toString() {
        return "TriggersCriteria [triggerId=" + triggerId + ", triggerIds=" + triggerIds + ", tags=" + tags
                + ", thin=" + thin + "]";
    }

}
