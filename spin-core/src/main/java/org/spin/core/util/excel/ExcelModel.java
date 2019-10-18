package org.spin.core.util.excel;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Excel导出模型
 * <p>Created by xuweinan on 2017/12/11.</p>
 *
 * @author xuweinan
 */
public class ExcelModel implements Serializable {
    private static final long serialVersionUID = 5788187914473247018L;

    private ExcelGrid grid;
    private Map<String, Iterable<?>> data;

    public ExcelModel(ExcelGrid grid, Iterable<?>... data) {
        this.grid = grid;
        this.data = new HashMap<>(grid.getSheets().size());

        if (data.length != 0 && grid.getSheets().size() != data.length) {
            throw new IllegalArgumentException("表格Sheet数量与数据集数量不一致");
        }

        for (int i = 0; i < data.length; i++) {
            this.data.put(grid.getSheets().get(i).getSheetName(), data[i]);
        }
    }

    public ExcelModel(ExcelGrid grid, List<Iterable<?>> data) {
        this.grid = grid;
        this.data = new HashMap<>(grid.getSheets().size());

        if (grid.getSheets().size() != data.size()) {
            throw new IllegalArgumentException("表格Sheet数量与数据集数量不一致");
        }

        for (int i = 0; i < data.size(); i++) {
            this.data.put(grid.getSheets().get(i).getSheetName(), data.get(i));
        }
    }

    public ExcelModel(ExcelGrid grid, Map<String, Iterable<?>> data) {
        this.grid = grid;
        this.data = null == data ? new HashMap<>() : data;
    }

    public ExcelModel putData(String sheetName, Iterable<?> data) {
        this.data.put(sheetName, data);
        return this;
    }

    public ExcelGrid getGrid() {
        return grid;
    }

    public void setGrid(ExcelGrid grid) {
        this.grid = grid;
    }

    public Map<String, Iterable<?>> getData() {
        return data;
    }

    public void setData(Map<String, Iterable<?>> data) {
        this.data = data;
    }
}
