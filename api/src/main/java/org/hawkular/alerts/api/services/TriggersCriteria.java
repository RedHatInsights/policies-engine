package org.hawkular.alerts.api.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.hawkular.alerts.api.util.Util.isEmpty;

public class TriggersCriteria {
    List<String> triggerIds = new ArrayList<>();
    String query = null;
    boolean thin = false;

    public TriggersCriteria() {
        super();
    }

    @Deprecated
    public void addTriggerIdFilter(String triggerId) {
        this.triggerIds.add(triggerId);
    }

    @Deprecated
    public void setTriggerIds(Collection<String> triggerIds) {
        this.triggerIds.clear();
        if(triggerIds != null) {
            this.triggerIds.addAll(triggerIds);
        }
    }

    public String getQuery() {
        if(hasTriggerIdCriteria()) {
            String triggerQuery = getTriggerIdQuery();
            if(hasQueryCriteria()) {
                return query + " and (" + triggerQuery + ")";
            } else {
                return triggerQuery;
            }
        }
        return query;
    }

    private String getTriggerIdQuery() {
        StringBuilder builder = new StringBuilder();
        builder.append("triggerId in ['");
        builder.append(String.join("', '", triggerIds));
        builder.append("']");

        return builder.toString();
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public boolean isThin() {
        return thin;
    }

    public void setThin(boolean thin) {
        this.thin = thin;
    }

    private boolean hasQueryCriteria() {
        return !isEmpty(query);
    }

    private boolean hasTriggerIdCriteria() {
        return !isEmpty(triggerIds);
    }

    public boolean hasCriteria() {
        return hasTriggerIdCriteria() || hasQueryCriteria();
    }

    @Override
    public String toString() {
        return "TriggersCriteria [triggerIds=" + triggerIds + ", query=" + query
                + ", thin=" + thin + "]";
    }

}
