package org.spin.jpa.pk.meta;

/**
 * ID类型
 */
public enum IdTypeE {
    /** 最大峰值型 */
    MAX_PEAK("max-peak", 0),

    /** 最小粒度型 */
    MIN_GRANULARITY("min-granularity", 1);

    private String name;
    private int value;

    IdTypeE(String name, int value) {
        this.name = name;
        this.value = value;
    }

    public int value() {
       return value;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
