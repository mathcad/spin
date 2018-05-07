package org.spin.core.util.excel;

import org.spin.core.collection.FixedVector;

import java.util.List;

/**
 * description EXCEL 数据
 *
 * @author wangy QQ 837195190
 * <p>Created by thinkpad on 2018/5/7.<p/>
 */
public class RowData {
    private int sheetIndex;
    private String SheetName;
    private int rowIndex;
    private FixedVector<String> row;
    private List<String> rowlist;

    /**
     * @param sheetIndex 工作簿索引
     * @param sheetName  工作簿名称
     * @param rowIndex   行索引
     * @param row        行数据
     */
    public RowData(int sheetIndex, String sheetName, int rowIndex, FixedVector<String> row) {
        this.sheetIndex = sheetIndex;
        SheetName = sheetName;
        this.rowIndex = rowIndex;
        this.row = row;
    }

    public RowData(int sheetIndex, String sheetName, int rowIndex, List<String> rowlist) {
        this.sheetIndex = sheetIndex;
        SheetName = sheetName;
        this.rowIndex = rowIndex;
        this.rowlist = rowlist;
    }

    public int getSheetIndex() {
        return sheetIndex;
    }

    public void setSheetIndex(int sheetIndex) {
        this.sheetIndex = sheetIndex;
    }

    public String getSheetName() {
        return SheetName;
    }

    public void setSheetName(String sheetName) {
        SheetName = sheetName;
    }

    public int getRowIndex() {
        return rowIndex;
    }

    public void setRowIndex(int rowIndex) {
        this.rowIndex = rowIndex;
    }

    public FixedVector<String> getRow() {
        return row;
    }

    public void setRow(FixedVector<String> row) {
        this.row = row;
    }

    public List<String> getRowlist() {
        return rowlist;
    }

    public void setRowlist(List<String> rowlist) {
        this.rowlist = rowlist;
    }
}
