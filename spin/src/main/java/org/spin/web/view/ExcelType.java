package org.spin.web.view;

/**
 * Excel文件版本
 * Created by xuweinan on 2017/2/7.
 *
 * @author xuweinna
 */
public enum ExcelType {
    /**
     * Excel 97-2003
     */
    Xls(".xls", "application/vnd.ms-excel"),

    /**
     * Excel 2007
     */
    Xlsx(".xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    private String value;
    private String contentType;

    ExcelType(String value, String contentType) {
        this.value = value;
        this.contentType = contentType;
    }

    public String getValue() {
        return value;
    }

    public String getContentType() {
        return contentType;
    }
}