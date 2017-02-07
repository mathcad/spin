package org.spin.web.view;

import java.io.Serializable;

/**
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
     * 访问值
     */
    private String dataIndex;

    /**
     * 数据类型
     */
    private String dataType;

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
}