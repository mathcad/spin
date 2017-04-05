package org.spin.collection;

import org.spin.sys.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 带有索引的二维表结构
 * Created by xuweinan on 2017/3/2.
 *
 * @author xuweinan
 */
public class Matrix<T> implements RowUpdateListener {

    private final List<Row<T>> rows = new ArrayList<>();

    private final Map<Integer, MultiValueMap<T, Integer>> index = new ConcurrentHashMap<>();
    private final Map<String, Integer> matrixHeader = new HashMap<>();

    private int columnNumber = 0;

    /**
     * 构造IndexedTable实例
     *
     * @param columnNumber 列数
     * @param indexColumns 需要建索引的列，从第2列开始；第一列为默认的主键。列编号从0开始
     */
    public Matrix(int columnNumber, int... indexColumns) {
        this.columnNumber = columnNumber;
        if (null != indexColumns && indexColumns.length > 0) {
            for (int i : indexColumns) {
                index.put(i, new HashMultiValueMap<>());
            }
        }
    }

    /**
     * 查找
     *
     * @param column 列
     * @param key    搜索键
     * @return 返回所有满足key的行的第column列的集合
     */
    public List<T> findValues(int column, T key) {
        return findRows(column, key).stream().map(r -> r.get(column)).collect(Collectors.toList());
    }

    public List<T> findValues(String columnHeader, T key) {
        Integer column = matrixHeader.get(columnHeader);
        if (null == column)
            return null;
        return findValues(column, key);
    }

    /**
     * 查找
     *
     * @param column 列
     * @param key    搜索键
     * @return 返回所有满足key的行的集合
     */
    public List<Row<T>> findRows(int column, T key) {
        List<Row<T>> result = new ArrayList<>();

        MultiValueMap<T, Integer> indexMap = index.get(column);
        if (null != indexMap) {
            // 索引查找
            List<Integer> rowIndexes = indexMap.get(key);
            for (Integer rowIndex : rowIndexes) {
                result.add(rows.get(rowIndex));
            }
        } else {
            // 无索引，全搜索
            rows.stream().filter(r -> null == key ? r.get(column) == null : key.equals(r.get(column))).forEach(result::add);
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * 查找
     *
     * @param columnHeader 列名
     * @param key          搜索键
     * @return 返回所有满足key的行的集合
     */
    public List<Row<T>> findRows(String columnHeader, T key) {
        Integer column = matrixHeader.get(columnHeader);
        if (null == column)
            return null;
        return findRows(column, key);
    }

    /**
     * 通过主键查找。第一列为主键
     * <p>主键允许重复</p>
     *
     * @param pk 主键
     */
    public List<Row<T>> findByPrimaryKey(T pk) {
        return findRows(0, pk);
    }

    /**
     * 向表中插入一条记录
     */
    @SafeVarargs
    public final Matrix<T> insert(T... values) {
        if (null == values || values.length != columnNumber) {
            throw new IllegalArgumentException("插入数据的列数与定义不一致 需要" + columnNumber + "列");
        }
        ArrayRow<T> row = new ArrayRow<>(Arrays.asList(values));
        row.setUpdateLestener(this);
        synchronized (this.rows) {
            // 插入数据
            rows.add(row);
            row.setRownum(rows.size() - 1);
        }
        // 构建索引
        for (Map.Entry<Integer, MultiValueMap<T, Integer>> indexEntry : this.index.entrySet()) {
            MultiValueMap<T, Integer> indexMap = indexEntry.getValue();
            int indexCol = indexEntry.getKey();
            indexMap.add(values[indexCol], row.rownum());
        }
        return this;
    }

    @SafeVarargs
    public final Matrix<T> update(int column, T key, T... values) {
        Assert.isTrue(column < columnNumber, "列索引超出范围");
        // TODO 更新操作
        return this;
    }

    public final Matrix<T> update(int column, T key, int updateColumn, T values) {
        Assert.isTrue(column < columnNumber && updateColumn < columnNumber, "列索引超出范围");
        // TODO 更新操作
        return this;
    }

    @SafeVarargs
    public final Matrix<T> update(String columnHeader, T key, T... values) {
        Integer column = matrixHeader.get(columnHeader);
        Assert.notNull(column, "指定的列名不存在");
        return update(column, key, values);
    }

    public final Matrix<T> update(String columnHeader, T key, String updateColumnHeader, T value) {
        Integer column = matrixHeader.get(columnHeader);
        Integer updateColumn = matrixHeader.get(updateColumnHeader);
        Assert.notNull(column, "指定的列名不存在");
        Assert.notNull(updateColumn, "指定的列名不存在");
        return update(column, key, updateColumn, value);
    }

    /**
     * 从表中删除记录
     *
     * @param column 列编号，从0开始
     * @param key    索引
     */
    public ArrayRow<T> delete(int column, T key) {
        Assert.isTrue(column < columnNumber, "列索引超出范围");
        // TODO 删除操作
        return null;
    }

    /**
     * 从表中删除记录
     *
     * @param columnHeader 列名
     * @param key          索引
     */
    public ArrayRow<T> delete(String columnHeader, T key) {
        Integer column = matrixHeader.get(columnHeader);
        Assert.notNull(column, "指定的列名不存在");
        return delete(column, key);
    }

    public final Matrix<T> setHeader(int column, String columnHeader) {
        Assert.isTrue(column < columnNumber, "列索引超出范围");
        Assert.hasText(columnHeader, "列名不能为空");
        matrixHeader.put(columnHeader, column);
        return this;
    }

    public final Matrix<T> createIndex(int column) {
        // TODO 在column列上创建索引
        return this;
    }

    public final Matrix<T> createIndex(String columnHeader) {
        Integer column = matrixHeader.get(columnHeader);
        Assert.notNull(column, "指定的列名不存在");
        return createIndex(column);
    }

    @Override
    public void updated(RowUpdateEvent event) {
        // TODO 数据被更新时，更新索引
        //noinspection unchecked
        buildIndex((Row<T>) event.getSource(), event.getUpdatedCols());
    }

    /**
     * 构建索引
     */
    private void buildIndex(Row<T> row) {
        for (Map.Entry<Integer, MultiValueMap<T, Integer>> indexEntry : this.index.entrySet()) {
            MultiValueMap<T, Integer> indexMap = indexEntry.getValue();
            int indexCol = indexEntry.getKey();
            indexMap.add(row.get(indexCol), row.rownum());
        }
    }

    /**
     * 构建索引
     */
    private void buildIndex(Row<T> row, int... cols) {
        for (int indexCol : cols) {
            MultiValueMap<T, Integer> indexMap = index.get(indexCol);
            if (null != indexMap) {
                indexMap.add(row.get(indexCol), row.rownum());
            }
        }
    }

    /**
     * 构建索引
     */
    private void buildIndex(Row<T> row, Collection<Integer> cols) {
        for (int indexCol : cols) {
            MultiValueMap<T, Integer> indexMap = index.get(indexCol);
            if (null != indexMap) {
                indexMap.add(row.get(indexCol), row.rownum());
            }
        }
    }
}
