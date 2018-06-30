package org.spin.core.util.excel;

import org.spin.core.util.StringUtils;

import java.io.Serializable;

/**
 * EXCEL表格中的一行
 *
 * <p>Created by xuweinan on 2016/8/15.</p>
 *
 * @author xuweinan
 */
public class ExcelRow implements Serializable {

    private static final long serialVersionUID = -2095576083028510444L;

    /**
     * sheet索引(从0开始)
     */
    private int sheetIndex = -1;

    /**
     * sheet名称
     */
    private String sheetName;

    /**
     * 行索引(从0开始)
     */
    private int rowIndex = -1;

    /**
     * 实际列数
     */
    private int columnNum = 0;

    /**
     * 容量
     */
    private int length = 0;

    /**
     * 行数据
     */
    private String[] row;

    /**
     * 是否包含有效内容
     */
    private boolean notBlank = false;


    public ExcelRow() {
    }

    /**
     * @param sheetIndex 工作簿索引
     * @param sheetName  工作簿名称
     * @param rowIndex   行索引
     */
    public ExcelRow(int sheetIndex, String sheetName, int rowIndex) {
        this.sheetIndex = sheetIndex;
        this.sheetName = sheetName;
        this.rowIndex = rowIndex;
    }

    /**
     * @param sheetIndex 工作簿索引
     * @param sheetName  工作簿名称
     * @param rowIndex   行索引
     * @param length     列数
     */
    public ExcelRow(int sheetIndex, String sheetName, int rowIndex, int length) {
        this.sheetIndex = sheetIndex;
        this.sheetName = sheetName;
        this.rowIndex = rowIndex;
        this.length = length;
    }

    public int getSheetIndex() {
        return sheetIndex;
    }

    public void setSheetIndex(int sheetIndex) {
        this.sheetIndex = sheetIndex;
    }

    public String getSheetName() {
        return sheetName;
    }

    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;
    }

    public int getRowIndex() {
        return rowIndex;
    }

    public void setRowIndex(int rowIndex) {
        this.rowIndex = rowIndex;
    }

    public int getColumnNum() {
        return columnNum;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
        if (null != row && row.length != length) {
            row = new String[length];
        }
    }

    public String[] getRow() {
        return row;
    }

    public void sheetIdxInc() {
        ++sheetIndex;
    }

    public void rowIdxInc() {
        ++rowIndex;
    }

    public void reset() {
        sheetIndex = -1;
        sheetName = null;
        rowIndex = -1;
        cleanRow();
    }

    public void cleanRow() {
        if (null == row) {
            row = new String[length];
        }
        for (int i = 0; i < length; i++) {
            row[i] = null;
        }
        notBlank = false;
    }

    public void setColumn(int colIndex, String colValue) {
        if (colIndex + 1 > columnNum) {
            columnNum = colIndex + 1;
        }
        // 扩容
        if (columnNum > length) {
            int newL = (int) (columnNum * 1.5);
            String[] newB = new String[newL];
            System.arraycopy(row, 0, newB, 0, length);
            length = newL;
            row = newB;
        }
        if (StringUtils.isNotEmpty(colValue)) {
            notBlank = true;
            if (null == row) {
                row = new String[length];
            }
            row[colIndex] = colValue;
        }
    }

    public boolean isNotBlank() {
        return notBlank;
    }
}
