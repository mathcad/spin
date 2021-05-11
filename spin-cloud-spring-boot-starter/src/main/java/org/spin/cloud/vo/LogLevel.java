package org.spin.cloud.vo;

import org.spin.core.trait.FriendlyEnum;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2021/5/11</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public enum LogLevel implements FriendlyEnum<Integer> {
    INFO(0, "信息"),
    SUCCESS(1, "成功"),
    WARN(2, "警告"),
    FAIL(-1, "失败"),
    ;

    private final Integer value;
    private final String desc;

    LogLevel(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    @Override
    public Integer getValue() {
        return value;
    }

    @Override
    public String getDescription() {
        return desc;
    }
}
