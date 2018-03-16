package org.spin.web.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    private Set<String> excludeColumns = new HashSet<>();

    public void addGridColumn(String header, Integer width, String dataIndex, String dataType) {
        GridColumn col = new GridColumn(header, width, dataIndex, dataType);
        this.columns.add(col);
    }

    public ExcelGrid addGridColumns(GridColumn... columns) {
        Collections.addAll(this.columns, columns);
        return this;
    }

    public ExcelGrid removGridColumn(String header) {
        columns.removeIf(column -> header.equals(column.getHeader()));
        return this;
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

    public Set<String> getExcludeColumns() {
        return excludeColumns;
    }

    public void setExcludeColumns(Set<String> excludeColumns) {
        this.excludeColumns = excludeColumns;
    }
}
