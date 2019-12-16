package org.hawkular.alerts.api.model.paging;

import java.util.Comparator;

import org.hawkular.alerts.api.model.event.Event;

/**
 *
 * @author Lucas Ponce
 */
public class EventComparator implements Comparator<Event> {

    public enum Field {
        ID("id"),
        CATEGORY("category"),
        CTIME("ctime"),
        TEXT("text"),
        TRIGGER_DESCRIPTION("trigger.description"),
        TRIGGER_ID("trigger.id"),
        TRIGGER_NAME("trigger.name"),
        CONTEXT("context");

        private String name;

        Field(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }

        public static Field getField(String name) {
            if (name == null || name.trim().isEmpty()) {
                return ID;
            }

            for (Field f : values()) {
                // context.<key>
                if (CONTEXT == f && name.toLowerCase().startsWith("context.")) {
                    return f;
                } else if (f.getName().compareToIgnoreCase(name) == 0) {
                    return f;
                }
            }
            return ID;
        }

        public static String getContextKey(String context) {
            if (context == null || context.trim().isEmpty() || !context.toLowerCase().startsWith("context.")) {
                return "";
            }
            return context.substring(8);
        }
    };

    private Field field;
    private String contextKey;
    private Order.Direction direction;

    public EventComparator() {
        this(Field.ID.getName(), Order.Direction.ASCENDING);
    }

    public EventComparator(String fieldName, Order.Direction direction) {
        this.field = Field.getField(fieldName);
        if (Field.CONTEXT == this.field) {
            this.contextKey = Field.getContextKey(fieldName);
        }
        this.direction = direction;
    }

    @Override
    public int compare(Event o1, Event o2) {
        if (o1 == null && o2 == null) {
            return 0;
        }
        if (o1 == null && o2 != null) {
            return 1;
        }
        if (o1 != null && o2 == null) {
            return -1;
        }
        int iOrder = direction == Order.Direction.ASCENDING ? 1 : -1;
        /*
            Using tenant comparator first
         */
        int tenantComparator = o1.getTenantId().compareTo(o2.getTenantId());
        if (tenantComparator != 0) {
            return tenantComparator * iOrder;
        }
        switch (field) {
            case ID:
                return o1.getId().compareTo(o2.getId()) * iOrder;

            case CONTEXT:
                if (o1.getContext() == null && o2.getContext() == null) {
                    return 0;
                }
                if (o1.getContext().isEmpty() && o2.getContext().isEmpty()) {
                    return 0;
                }
                if (!o1.getContext().containsKey(contextKey) && !o2.getContext().containsKey(contextKey)) {
                    return 0;
                }
                if (!o1.getContext().containsKey(contextKey) && o2.getContext().containsKey(contextKey)) {
                    return 1;
                }
                if (!o1.getContext().containsKey(contextKey) && !o2.getContext().containsKey(contextKey)) {
                    return -1;
                }
                return o1.getContext().get(contextKey).compareTo(o2.getContext().get(contextKey)) * iOrder;

            case CATEGORY:
                if (o1.getCategory() == null && o2.getCategory() == null) {
                    return 0;
                }
                if (o1.getCategory() == null && o2.getCategory() != null) {
                    return 1;
                }
                if (o1.getCategory() != null && o2.getCategory() == null) {
                    return -1;
                }
                return o1.getCategory().compareTo(o2.getCategory()) * iOrder;

            case CTIME:
                return (int) ((o1.getCtime() - o2.getCtime()) * iOrder);

            case TEXT:
                if (o1.getText() == null && o2.getText() == null) {
                    return 0;
                }
                if (o1.getText() == null && o2.getText() != null) {
                    return 1;
                }
                if (o1.getText() != null && o2.getText() == null) {
                    return -1;
                }
                return o1.getText().compareTo(o2.getText()) * iOrder;

            case TRIGGER_DESCRIPTION:
                String o1TriggerDesc = null == o1.getTrigger() ? null : o1.getTrigger().getDescription();
                String o2TriggerDesc = null == o2.getTrigger() ? null : o2.getTrigger().getDescription();
                if (o1TriggerDesc == null && o2TriggerDesc == null) {
                    return 0;
                }
                if (o1TriggerDesc == null && o2TriggerDesc != null) {
                    return 1;
                }
                if (o1TriggerDesc != null && o2TriggerDesc == null) {
                    return -1;
                }
                return o1TriggerDesc.compareTo(o2TriggerDesc) * iOrder;
            case TRIGGER_ID:
                String o1TriggerId = null == o1.getTrigger() ? null : o1.getTrigger().getId();
                String o2TriggerId = null == o2.getTrigger() ? null : o2.getTrigger().getId();
                if (o1TriggerId == null && o2TriggerId == null) {
                    return 0;
                }
                if (o1TriggerId == null && o2TriggerId != null) {
                    return 1;
                }
                if (o1TriggerId != null && o2TriggerId == null) {
                    return -1;
                }
                return o1TriggerId.compareTo(o2TriggerId) * iOrder;
            case TRIGGER_NAME:
                String o1TriggerName = null == o1.getTrigger() ? null : o1.getTrigger().getName();
                String o2TriggerName = null == o2.getTrigger() ? null : o2.getTrigger().getName();
                if (o1TriggerName == null && o2TriggerName == null) {
                    return 0;
                }
                if (o1TriggerName == null && o2TriggerName != null) {
                    return 1;
                }
                if (o1TriggerName != null && o2TriggerName == null) {
                    return -1;
                }
                return o1TriggerName.compareTo(o2TriggerName) * iOrder;
        }
        return 0;
    }
}
