package org.spin.enhance.geo;

import org.spin.core.trait.IntEvaluatable;

/**
 * 坐标系类型
 * Created by xuweinan on 2017/1/11.
 *
 * @author xuweinan
 */
public enum CoordinateSystem implements IntEvaluatable {
    OFFSET(-1, "offset", "坐标偏移量"),
    UNKNOWN(0, "unknown", "未知坐标系"),
    GPS(1, "wgs84", "GPS坐标系"),
    GCJ02(2, "gcj-02", "国测坐标系"),
    BAIDU(4, "bd09ll", "百度经纬度坐标系"),
    MERCATOR(8, "bd09mc", "墨卡托米制坐标系");

    private final int value;
    private final String code;
    private final String desc;

    CoordinateSystem(int value, String code, String desc) {
        this.value = value;
        this.code = code;
        this.desc = desc;
    }

    @Override
    public int intValue() {
        return value;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
