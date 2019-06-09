package org.spin.data.rs;


import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
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

    public Map<String, Object> apply(ResultSet rs, int rowNum) throws SQLException {
        ResultSetMetaData rsmd = rs.getMetaData();
        int columnCount = rsmd.getColumnCount();
        Map<String, Object> mapOfColValues = new HashMap<>(columnCount);
        for (int i = 1; i <= columnCount; i++) {
            String key = lookupColumnName(rsmd, i);
            Object obj = getResultSetValue(rs, i);
            mapOfColValues.put(key, obj);
        }
        return mapOfColValues;
    }
}
