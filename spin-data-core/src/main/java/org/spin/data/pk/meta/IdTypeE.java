package org.spin.data.pk.meta;

/**
 * ID类型
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2017/5/5</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public enum IdTypeE {
    /**
     * 最大峰值型
     */
    MAX_PEAK("max-peak", 0),

    /**
     * 最小粒度型
     */
    MIN_GRANULARITY("min-granularity", 1);

    private String name;
    private int value;

    IdTypeE(String name, int value) {
        this.name = name;
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
