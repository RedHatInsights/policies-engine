package org.hawkular.alerts.api.model.event;

/**
 * @author Jay Shaughnessy
 * @author Lucas Ponce
 */
public enum EventField {
    ID ("id"),
    CTIME ("ctime"),
    DATASOURCE ("dataSource"),
    DATAID ("dataId"),
    CATEGORY ("category"),
    TEXT ("text"),
    CONTEXT ("context"),
    TAGS ("tags"),
    FACTS ("facts");

    private final String name;

    EventField(String name) {
        this.name = name;
    }

    public boolean equalsName(String name) {
        return this.name.equals(name);
    }

    public static EventField fromString(String name) {
        for (EventField field : EventField.values()) {
            if (field.equalsName(name)) {
                return field;
            }
        }
        return null;
    }

    public String toString() {
        return this.name;
    }
}
