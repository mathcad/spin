package org.spin.core.util.excel;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * 表格中的表头
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/10/21</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class GridHeader implements Serializable {
    private static final long serialVersionUID = -4042970349541054517L;

    private List<GridColumn> columns = new LinkedList<>();
    private Set<String> excludeColumns = new HashSet<>();

    public GridHeader appendColumn(String header, String dataIndex) {
        GridColumn col = new GridColumn(header, dataIndex);
        this.columns.add(col);
        return this;
    }

    public GridHeader appendColumn(String header, Integer width, String dataIndex, String dataType) {
        GridColumn col = new GridColumn(header, width, dataIndex, dataType);
        this.columns.add(col);
        return this;
    }

    public GridHeader appendColumns(GridColumn... columns) {
        Collections.addAll(this.columns, columns);
        return this;
    }

    public GridHeader removeColumn(String header) {
        columns.removeIf(column -> header.equals(column.getHeader()));
        return this;
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
