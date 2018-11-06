package org.spin.enhance.ip;

import org.spin.core.Assert;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2018/11/5</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class Region {

    private String origin;

    /**
     * 国家
     */
    private String country;

    /**
     * 区域
     */
    private String area;

    /**
     * 省
     */
    private String province;

    /**
     * 市
     */
    private String city;

    /**
     * ISP提供商
     */
    private String isp;

    public Region(String origin) {
        this.origin = Assert.notEmpty(origin, "区域信息不能为空");
        String[] split = origin.split("\\|");
        Assert.isTrue(split.length == 5, "区域信息不合法");
        country = split[0];
        area = split[1];
        province = split[2];
        city = split[3];
        isp = split[4];
    }

    public String getCountry() {
        return country;
    }

    public String getArea() {
        return area;
    }

    public String getProvince() {
        return province;
    }

    public String getCity() {
        return city;
    }

    public String getIsp() {
        return isp;
    }

    @Override
    public int hashCode() {
        return origin.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj.getClass() == Region.class && origin.equals(((Region) obj).origin);
    }

    @Override
    public String toString() {
        return origin;
    }
}
