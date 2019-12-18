package org.spin.data.rs;


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
    public Map<String, Object> apply(String[] columnNames, Object[] columns, int columnCount, int rowIdx) {
        Map<String, Object> mapOfColValues = new HashMap<>(columnCount);
        for (int i = 0; i < columnCount; i++) {
            mapOfColValues.put(columnNames[i], columns[i]);
        }
        return mapOfColValues;
    }
}
