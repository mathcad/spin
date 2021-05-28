package org.spin.jpa.vo;

import org.spin.core.util.ObjectUtils;
import org.spin.data.rs.ColumnVisitor;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2021/5/28</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class VirtualColumnVisitor extends ColumnVisitor {
    private final Object[] columns;
    private final String[] names;

    public VirtualColumnVisitor(Object[] columns, String[] names) throws SQLException {
        super(null);
        this.columns = columns;
        this.names = names;
    }


    @Override
    public Object getColumnValue(int columnIndex) {
        return columns[columnIndex];
    }

    @Override
    public Object getColumnValue(int columnIndex, Class<?> requiredType) {
        return ObjectUtils.convert(requiredType, columns[columnIndex]);
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public String[] getColumnNames() {
        return names;
    }

    @Override
    public String getColumnName(int columnIndex) {
        return names[columnIndex];
    }

    @Override
    public String lookupColumnName(ResultSetMetaData resultSetMetaData, int columnIndex) {
        return names[columnIndex];
    }
}
