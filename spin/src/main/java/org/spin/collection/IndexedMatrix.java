package org.spin.collection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 带有索引的二维表结构
 * Created by xuweinan on 2017/3/2.
 *
 * @author xuweinan
 */
public class IndexedMatrix<T> {

    private final List<List<T>> values = new ArrayList<>();

    private final Map<Integer, MultiValueMap<T, Integer>> index = new ConcurrentHashMap<>();

    private int columnNumber = 0;

    /**
     * 构造IndexedTable实例
     *
     * @param columnNumber 列数
     * @param indexColumns 需要建索引的列，从第2列开始；第一列为默认的主键。列编号从0开始
     */
    public IndexedMatrix(int columnNumber, int... indexColumns) {
        this.columnNumber = columnNumber;
        if (null != indexColumns && indexColumns.length > 0) {
            for (int i : indexColumns) {
                index.put(i, new HashMultiValueMap<>());
            }
        }
    }

    public T find(int column, T key) {
        MultiValueMap<T, Integer> i = index.get(column);
        if (null != i) {
            List<Integer> matches = i.get(key);
            for (Integer idx : matches) {
                T value = values.get(idx).get(column);
                if (null != value)
                    return value;
            }
        }
        return null;
    }

    /**
     * 通过主键查找。第一列为主键
     * <p>主键允许重复</p>
     *
     * @param pk 主键
     */
    public List<T> findByPrimaryKey(T pk) {
        return null;
    }

    /**
     * 向表中插入一条记录
     */
    @SafeVarargs
    public final IndexedMatrix insert(T... values) {
        if (null == values || values.length != columnNumber) {
            throw new IllegalArgumentException("插入数据的列数与定义不一至。 需要" + columnNumber + "列");
        }
        synchronized (this.values) {
            // 插入数据
            this.values.add(Arrays.asList(values));
            // 构建索引
            for (Map.Entry<Integer, MultiValueMap<T, Integer>> map : this.index.entrySet()) {
                MultiValueMap<T, Integer> idx = map.getValue();
                Integer col = map.getKey();
                idx.add(values[col], this.values.size());
            }
        }
        return this;
    }

    /**
     * 从表中删除记录
     *
     * @param column 列编号，从0开始
     * @param key    索引
     */
    public T delete(int column, T key) {
        return null;
    }
}
