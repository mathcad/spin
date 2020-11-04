package org.spin.core.util.excel;

import java.io.Serializable;

/**
 * Excel表格中一列的定义
 * Created by xuweinan on 2017/2/7.
 *
 * @author xuweinan
 */
public class GridColumn implements Serializable {
    private static final long serialVersionUID = 4546366661647134014L;

    /**
     * 列头
     */
    private String header;

    /**
     * 列宽度
     */
    private Integer width;

    /**
     * 数据key
     */
    private String dataIndex;

    /**
     * 数据类型
     */
    private String dataType;

    /**
     * 跨行
     */
    private int rowspan = 1;

    /**
     * 跨列
     */
    private int colspan = 1;

    public GridColumn() {
    }

    public GridColumn(String header, String dataIndex) {
        this.header = header;
        this.dataIndex = dataIndex;
    }

    public GridColumn(String header, Integer width, String dataIndex) {
        this.header = header;
        this.width = width;
        this.dataIndex = dataIndex;
    }

    public GridColumn(String header, Integer width, String dataIndex, String dataType) {
        this.header = header;
        this.width = width;
        this.dataIndex = dataIndex;
        this.dataType = dataType;
    }

    public static GridColumn of(String header, String dataIndex) {
        return new GridColumn(header, dataIndex);
    }

    public static GridColumn of(String header, Integer width, String dataIndex) {
        return new GridColumn(header, width, dataIndex);
    }

    public static GridColumn of(String header, Integer width, String dataIndex, String dataType) {
        return new GridColumn(header, width, dataIndex, dataType);
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public String getDataIndex() {
        return dataIndex;
    }

    public void setDataIndex(String dataIndex) {
        this.dataIndex = dataIndex;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public int getRowspan() {
        return rowspan;
    }

    public void setRowspan(int rowspan) {
        this.rowspan = rowspan;
    }

    public int getColspan() {
        return colspan;
    }

    public void setColspan(int colspan) {
        this.colspan = colspan;
    }
}
