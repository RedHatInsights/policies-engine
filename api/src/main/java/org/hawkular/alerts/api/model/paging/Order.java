package org.hawkular.alerts.api.model.paging;

/**
 * @author Lukas Krejci
 * @since 0.0.1
 */
public final class Order {
    private final String field;
    private final Direction direction;

    public Order(String field, Direction direction) {
        this.field = field;
        this.direction = direction;
    }

    public static Order by(String field, Direction direction) {
        return new Order(field, direction);
    }

    public static Order unspecified() {
        return new Order(null, Direction.ASCENDING);
    }

    public Direction getDirection() {
        return direction;
    }

    public String getField() {
        return field;
    }

    public boolean isSpecific() {
        return field != null;
    }

    public boolean isAscending() {
        return direction == Direction.ASCENDING;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Order)) return false;

        Order order = (Order) o;

        return field.equals(order.field) && direction == order.direction;
    }

    @Override
    public int hashCode() {
        int result = field.hashCode();
        result = 31 * result + direction.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Order[" + "direction=" + direction + ", field='" + field + '\'' + ']';
    }

    public enum Direction {
        ASCENDING("asc"), DESCENDING("desc");

        private final String shortString;

        Direction(String shortString) {
            this.shortString = shortString;
        }

        public static Direction fromShortString(String shortString) {
            switch (shortString) {
                case "asc":
                    return ASCENDING;
                case "desc":
                    return DESCENDING;
                default:
                    throw new IllegalArgumentException("Unkown short ordering direction representation: " +
                            shortString);
            }
        }

        public String getShortString() {
            return shortString;
        }
    }
}
