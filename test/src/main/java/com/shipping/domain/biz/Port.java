package com.shipping.domain.biz;

import org.spin.data.core.AbstractEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * 港口
 *
 * @author X
 * @contact TEL:18900539326, QQ:396616781
 * @create 2017-09-04 下午10:43
 */
@Entity
@Table(name = "biz_port")
public class Port extends AbstractEntity {

    /**
     * 港口名称
     */
    @Column(length = 32)
    private String name;

    /**
     * 经度
     */
    @Column(length = 20)
    private Double longitude;

    /**
     * 纬度
     */
    @Column(length = 20)
    private Double latitude;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    @Override
    public String toString() {
        return "Port{" +
            "name='" + name + '\'' +
            ", longitude=" + longitude +
            ", latitude=" + latitude +
            '}';
    }
}
