package org.spin.enhance.ip;

/**
 * data block class
 */
public class DataBlock {
    /**
     * city id
     */
    private int cityId;

    /**
     * region address
     */
    private Region region;

    /**
     * region ptr in the db file
     */
    private int dataPtr;

    public DataBlock(int cityId, String region, int dataPtr) {
        this.cityId = cityId;
        this.region = new Region(region);
        this.dataPtr = dataPtr;
    }

    public DataBlock(int cityId, String region) {
        this(cityId, region, 0);
    }

    public int getCityId() {
        return cityId;
    }

    public DataBlock setCityId(int city_id) {
        this.cityId = city_id;
        return this;
    }

    public Region getRegion() {
        return region;
    }

    public DataBlock setRegion(Region region) {
        this.region = region;
        return this;
    }

    public int getDataPtr() {
        return dataPtr;
    }

    public DataBlock setDataPtr(int dataPtr) {
        this.dataPtr = dataPtr;
        return this;
    }

    @Override
    public String toString() {
        return String.valueOf(cityId) + '|' + region + '|' + dataPtr;
    }
}
