package org.spin.enhance.geo;

import org.spin.core.util.ObjectUtils;
import org.spin.core.util.StringUtils;

import java.io.Serializable;


/**
 * 表示一个坐标位置
 *
 * @author xuweinan
 */
public class Cordinate implements Serializable {
    private static final long serialVersionUID = 7457963026513014856L;

    /**
     * 坐标系统
     */
    private CoordinateTypeE type = null;

    /**
     * 纬度
     */
    private double latitude = -1D;
    /**
     * 经度
     */
    private double longitude = -1D;

    /**
     * 构造
     */
    public Cordinate() {
    }

    /**
     * 构造
     *
     * @param latitude  纬度
     * @param longitude 经度
     */
    public Cordinate(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        if (Math.abs(latitude) > 90 || Math.abs(longitude) > 180) {
            throw new IllegalArgumentException("The supplied coordinates " + this + " are out of range.");
        }
    }

    /**
     * 构造
     *
     * @param other {@link Cordinate}
     */
    public Cordinate(Cordinate other) {
        this(other.latitude, other.longitude);
    }

    public CoordinateTypeE getType() {
        return type;
    }

    public void setType(CoordinateTypeE type) {
        this.type = type;
    }

    /**
     * 获得纬度
     *
     * @return 纬度
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * 设置纬度
     *
     * @param latitude 纬度
     */
    public void setLatitude(double latitude) {
        this.latitude = latitude;
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
     * 设置经度
     *
     * @param longitude 经度
     */
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        return StringUtils.plainFormat("{}[({}, {})]", this.type.name(), this.latitude, this.longitude);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Cordinate) {
            Cordinate other = (Cordinate) obj;
            return ObjectUtils.nullSafeEquals(type, other.type)
                && Double.valueOf(latitude).equals(other.latitude)
                && Double.valueOf(longitude).equals(other.longitude);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = 42;
        long latBits = Double.doubleToLongBits(latitude);
        long lonBits = Double.doubleToLongBits(longitude);
        result = 31 * result + (int) (latBits ^ (latBits >>> 32));
        result = 31 * result + (int) (lonBits ^ (lonBits >>> 32));
        return result;
    }
}
