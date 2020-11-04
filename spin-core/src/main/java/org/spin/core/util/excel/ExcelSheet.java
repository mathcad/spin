package org.spin.core.util.excel;

import org.spin.core.Assert;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
    private List<GridHeader> headers = new LinkedList<>();
    private List<GridColumn> columns = new LinkedList<>();

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

    public List<GridHeader> getHeaders() {
        return headers;
    }

    public void setHeaders(List<GridHeader> headers) {
        this.headers = headers;
    }

    public List<GridColumn> getColumns() {
        return columns;
    }

    public void setColumns(List<GridColumn> columns) {
        this.columns = columns;
    }

    public void validateHeader() {
        Assert.notEmpty(headers, "表格[" + sheetName + "]未设置任何表头");
        Map<Integer, Integer> additionalColNumber = new HashMap<>();

        int nextAdditionalColNumber = 0;
        int currentColNumber = 0;
        int lastColNumber;
        for (int h = 0; h < headers.size(); ++h) {
            if (!additionalColNumber.containsKey(h)) {
                additionalColNumber.put(h, 0);
            }
            lastColNumber = currentColNumber;
            currentColNumber = nextAdditionalColNumber;
            GridHeader header = Assert.notNull(headers.get(h), "表格[" + sheetName + "]的第" + (h + 1) + "行表头为空");
            for (int i = 0; i < header.getColumns().size(); i++) {
                GridColumn column = Assert.notNull(header.getColumns().get(i),
                    "表格[" + sheetName + "]的第" + (h + 1) + "行表头的第" + (i + 1) + "列为空");
                Assert.le(column.getRowspan(), headers.size(),
                    "表格[" + sheetName + "]的第" + (h + 1) + "行表头的第" + (i + 1) + "列的跨行设置超出范围");
                if (column.getRowspan() > 1) {
                    nextAdditionalColNumber = nextAdditionalColNumber + column.getRowspan() - 1;
                }
                currentColNumber += column.getColspan();
            }

            Assert.isEquals(lastColNumber, currentColNumber, "表格[" + sheetName + "]的第" + (h + 1) + "行表头与上一行表头的列数不一致");
        }
    }
}
