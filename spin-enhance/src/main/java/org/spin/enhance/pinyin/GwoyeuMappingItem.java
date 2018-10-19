package org.spin.enhance.pinyin;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2018/10/15</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class GwoyeuMappingItem {

    private String hanyu;
    private String gwoyeuI;
    private String gwoyeuIi;
    private String gwoyeuIii;
    private String gwoyeuIv;
    private String gwoyeuV;

    public String getHanyu() {
        return hanyu;
    }

    public void setHanyu(String hanyu) {
        this.hanyu = hanyu;
    }

    public String getGwoyeuI() {
        return gwoyeuI;
    }

    public void setGwoyeuI(String gwoyeuI) {
        this.gwoyeuI = gwoyeuI;
    }

    public String getGwoyeuIi() {
        return gwoyeuIi;
    }

    public void setGwoyeuIi(String gwoyeuIi) {
        this.gwoyeuIi = gwoyeuIi;
    }

    public String getGwoyeuIii() {
        return gwoyeuIii;
    }

    public void setGwoyeuIii(String gwoyeuIii) {
        this.gwoyeuIii = gwoyeuIii;
    }

    public String getGwoyeuIv() {
        return gwoyeuIv;
    }

    public void setGwoyeuIv(String gwoyeuIv) {
        this.gwoyeuIv = gwoyeuIv;
    }

    public String getGwoyeuV() {
        return gwoyeuV;
    }

    public void setGwoyeuV(String gwoyeuV) {
        this.gwoyeuV = gwoyeuV;
    }

    public String getValue(String key) {
        switch (key) {
            case "hanyu":
                return hanyu;
            case "gwoyeuI":
                return gwoyeuI;
            case "gwoyeuIi":
                return gwoyeuIi;
            case "gwoyeuIii":
                return gwoyeuIii;
            case "gwoyeuIv":
                return gwoyeuIv;
            case "gwoyeuV":
                return gwoyeuV;
            default:
                return hanyu;

        }
    }

    public void setValue(String key, String value) {
        switch (key) {
            case "hanyu":
                hanyu = value;
                break;
            case "gwoyeuI":
                gwoyeuI = value;
                break;
            case "gwoyeuIi":
                gwoyeuIi = value;
                break;
            case "gwoyeuIii":
                gwoyeuIii = value;
                break;
            case "gwoyeuIv":
                gwoyeuIv = value;
                break;
            case "gwoyeuV":
                gwoyeuV = value;
                break;
            default:
                hanyu = value;
        }
    }
}
