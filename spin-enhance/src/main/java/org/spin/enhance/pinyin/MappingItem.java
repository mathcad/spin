package org.spin.enhance.pinyin;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2018/10/15</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class MappingItem {

    private String hanyu;
    private String wade;
    private String mpsii;
    private String yale;
    private String tongyong;

    public String getHanyu() {
        return hanyu;
    }

    public void setHanyu(String hanyu) {
        this.hanyu = hanyu;
    }

    public String getWade() {
        return wade;
    }

    public void setWade(String wade) {
        this.wade = wade;
    }

    public String getMpsii() {
        return mpsii;
    }

    public void setMpsii(String mpsii) {
        this.mpsii = mpsii;
    }

    public String getYale() {
        return yale;
    }

    public void setYale(String yale) {
        this.yale = yale;
    }

    public String getTongyong() {
        return tongyong;
    }

    public void setTongyong(String tongyong) {
        this.tongyong = tongyong;
    }

    public String getValue(String key) {
        switch (key) {
            case "hanyu":
                return hanyu;
            case "wade":
                return wade;
            case "mpsii":
                return mpsii;
            case "yale":
                return yale;
            case "tongyong":
                return tongyong;
            default:
                return hanyu;

        }
    }

    public void setValue(String key, String value) {
        switch (key) {
            case "hanyu":
                hanyu = value;
                break;
            case "wade":
                wade = value;
                break;
            case "mpsii":
                mpsii = value;
                break;
            case "yale":
                yale = value;
                break;
            case "tongyong":
                tongyong = value;
                break;
            default:
                hanyu = value;
        }
    }
}
