package org.spin.enhance.geo;

/**
 * 坐标类型
 * Created by xuweinan on 2017/1/11.
 *
 * @author xuweinan
 */
public enum CoordinateTypeE {
    百度坐标("bd09ll"),
    百度摩卡("bd09mc"),
    国测坐标("gcj02"),
    GPS设备("wgs84");
    private String value;

    CoordinateTypeE(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
