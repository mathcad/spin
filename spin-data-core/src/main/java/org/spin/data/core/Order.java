package org.spin.data.core;

import java.io.Serializable;
import java.util.Locale;
import java.util.Optional;


/**
 * 排序表达式
 */
public class Order implements Serializable {
    private static final long serialVersionUID = -6549747417707014359L;

    private Direction direction;
    private String property;

    public Order() {
    }

    public Order(Direction direction, String property) {
        this.direction = direction == null ? Direction.ASC : direction;
        this.property = property;
    }


    public static Order by(String property) {
        return new Order(Direction.ASC, property);
    }

    public static Order asc(String property) {
        return new Order(Direction.ASC, property);
    }

    public static Order desc(String property) {
        return new Order(Direction.DESC, property);
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public boolean isAscending() {
        return this.direction.isAscending();
    }

    public boolean isDescending() {
        return this.direction.isDescending();
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        int result = 17;

        result = 31 * result + direction.hashCode();
        result = 31 * result + property.hashCode();

        return result;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Order)) {
            return false;
        }

        Order that = (Order) obj;

        return this.direction.equals(that.direction) && this.property.equals(that.property);
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return property + " " + direction.name();
    }


    /**
     * 排序方向枚举
     */
    public enum Direction {

        /**
         * 升序
         */
        ASC,

        /**
         * 降序
         */
        DESC;

        /**
         * 是否升序
         *
         * @return 是否升序
         */
        public boolean isAscending() {
            return this.equals(ASC);
        }

        /**
         * 是否降序
         *
         * @return 是否降序
         */
        public boolean isDescending() {
            return this.equals(DESC);
        }

        /**
         * 从字符串解析方向
         *
         * @param value 字符串
         * @return 方向
         */
        public static Direction fromString(String value) {

            try {
                return Direction.valueOf(value.toUpperCase(Locale.US));
            } catch (Exception e) {
                throw new IllegalArgumentException(String.format(
                    "Invalid value '%s' for orders given! Has to be either 'desc' or 'asc' (case insensitive).", value), e);
            }
        }

        /**
         * 从字符串解析方向
         *
         * @param value 字符串
         * @return 方向
         */
        public static Optional<Direction> fromOptionalString(String value) {
            try {
                return Optional.of(fromString(value));
            } catch (IllegalArgumentException e) {
                return Optional.empty();
            }
        }
    }
}
