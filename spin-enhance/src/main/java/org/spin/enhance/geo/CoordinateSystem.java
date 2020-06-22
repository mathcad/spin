package org.spin.enhance.geo;

/**
 * 坐标类型
 * Created by xuweinan on 2017/1/11.
 *
 * @author xuweinan
 */
public enum CoordinateSystem {
    BAIDU("bd09ll", "百度经纬度坐标系"),
    MERCATOR("bd09mc", "墨卡托米制坐标系"),
    GCJ02("gcj02", "火星坐标系"),
    GPS("wgs84", "GPS坐标系");
    private final String value;
    private final String desc;

    CoordinateSystem(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public String getValue() {
        return value;
    }
}
