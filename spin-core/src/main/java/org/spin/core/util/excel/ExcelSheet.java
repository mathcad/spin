package org.spin.core.util.excel;

import org.spin.core.Assert;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Excel中的sheet
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/10/18</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class ExcelSheet implements Serializable {
    private static final long serialVersionUID = -7239641413329751595L;

    private String sheetName;
    private List<GridColumn> columns = new LinkedList<>();
    private Set<String> excludeColumns = new HashSet<>();

    public ExcelSheet(String sheetName) {
        Assert.notEmpty(sheetName, "Sheet名称不能为空");
        this.sheetName = sheetName;
    }

    public ExcelSheet appendColumn(String header, String dataIndex) {
        GridColumn col = new GridColumn(header, dataIndex);
        this.columns.add(col);
        return this;
    }

    public ExcelSheet appendColumn(String header, Integer width, String dataIndex, String dataType) {
        GridColumn col = new GridColumn(header, width, dataIndex, dataType);
        this.columns.add(col);
        return this;
    }

    public ExcelSheet appendColumns(GridColumn... columns) {
        Collections.addAll(this.columns, columns);
        return this;
    }

    public ExcelSheet removeColumn(String header) {
        columns.removeIf(column -> header.equals(column.getHeader()));
        return this;
    }

    public String getSheetName() {
        return sheetName;
    }

    public void setSheetName(String sheetName) {
        Assert.notEmpty(sheetName, "Sheet名称不能为空");
        this.sheetName = sheetName;
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
