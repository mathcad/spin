package org.spin.enhance.geo;

import org.spin.core.Assert;
import org.spin.core.util.ObjectUtils;

import java.io.Serializable;


/**
 * 表示一个坐标位置
 *
 * @author xuweinan
 */
public class Coordinate implements Serializable {
    private static final long serialVersionUID = 7457963026513014856L;

    /**
     * 坐标系统
     */
    private final CoordinateSystem type;

    /**
     * 经度
     */
    private final double longitude;

    /**
     * 纬度
     */
    private final double latitude;

    public Coordinate(Coordinate other) {
        this(other.type, other.latitude, other.longitude);
    }

    public Coordinate(double longitude, double latitude) {
        this(CoordinateSystem.UNKNOWN, longitude, latitude);
    }

    public Coordinate(CoordinateSystem type, double longitude, double latitude) {
        this.type = Assert.notNull(type, "请指定坐标系类型");
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public CoordinateSystem getType() {
        return type;
    }

    /**
     * 获得经度
     *
     * @return 经度
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * 获得纬度
     *
     * @return 纬度
     */
    public double getLatitude() {
        return latitude;
    }

    public Coordinate diff(Coordinate another) {
        return new Coordinate(CoordinateSystem.OFFSET, another.longitude - longitude, another.latitude - latitude);
    }

    @Override
    public String toString() {
        return String.format(" %s[%f, %f]", this.type.name(), this.longitude, this.latitude);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Coordinate) {
            Coordinate other = (Coordinate) obj;
            return ObjectUtils.nullSafeEquals(type, other.type)
                && Double.valueOf(longitude).equals(other.longitude)
                && Double.valueOf(latitude).equals(other.latitude);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = 42;
        long lngBits = Double.doubleToLongBits(longitude);
        long latBits = Double.doubleToLongBits(latitude);
        result = 31 * result + (int) (lngBits ^ (lngBits >>> 32));
        result = 31 * result + (int) (latBits ^ (latBits >>> 32));
        return result;
    }
}
