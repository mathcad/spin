package org.spin.data.rs;


import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * ResultSet到List&lt;Map&lt;String, Object&gt;&gt;的转换器
 * <p>Created by xuweinan on 2018/3/22.</p>
 *
 * @author xuweinan
 */
public class MapRowMapper implements RowMapper<Map<String, Object>> {

    @Override
    public Map<String, Object> apply(ColumnVisitor columnVisitor, int rowIdx) throws SQLException {
        Map<String, Object> mapOfColValues = new HashMap<>(columnVisitor.getColumnCount());
        for (int i = 0; i < columnVisitor.getColumnCount(); i++) {
            mapOfColValues.put(columnVisitor.getColumnName(i), columnVisitor.getColumnValue(i + 1));
        }
        return mapOfColValues;
    }
}
