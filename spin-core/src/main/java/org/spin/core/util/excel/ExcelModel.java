package org.spin.core.util.excel;

import java.io.Serializable;

/**
 * <p>Created by xuweinan on 2017/12/11.</p>
 *
 * @author xuweinan
 */
public class ExcelModel implements Serializable {
    private static final long serialVersionUID = 5788187914473247018L;

    private ExcelGrid grid;
    private Iterable<?> data;

    public ExcelModel(ExcelGrid grid, Iterable<?> data) {
        this.grid = grid;
        this.data = data;
    }

    public ExcelGrid getGrid() {
        return grid;
    }

    public void setGrid(ExcelGrid grid) {
        this.grid = grid;
    }

    public Iterable<?> getData() {
        return data;
    }

    public void setData(Iterable<?> data) {
        this.data = data;
    }
}
