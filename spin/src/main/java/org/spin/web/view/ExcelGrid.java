package org.spin.web.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by xuweinan on 2017/2/7.
 *
 * @author xuweinan
 */
public class ExcelGrid implements Serializable {
    private static final long serialVersionUID = 3306654738869974692L;

    private String fileId;

    private String fileName;

    private List<GridColumn> columns = new ArrayList<>();

    public void addGridColumn(String header, Integer width, String dataIndex, String dataType) {
        GridColumn col = new GridColumn();
        col.setHeader(header);
        col.setWidth(width);
        col.setDataIndex(dataIndex);
        col.setDataType(dataType);
        this.columns.add(col);
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public List<GridColumn> getColumns() {
        return columns;
    }

    public void setColumns(List<GridColumn> columns) {
        this.columns = columns;
    }
}