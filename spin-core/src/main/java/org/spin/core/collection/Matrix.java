package org.spin.core.collection;

import org.spin.core.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 带有索引的二维表结构
 * <p>Created by xuweinan on 2017/3/2.</p>
 *
 * @author xuweinan
 */
public class Matrix<T> implements RowUpdateListener {

    /**
     * 数据容器
     */
    private final List<Row<T>> rows = new ArrayList<>();

    /**
     * 索引（列编号-[值-行编号集合]）
     */
    private final Map<Integer, MultiDiffValueMap<T, Integer>> index = new HashMap<>();

    /**
     * 列名-列编号的映射
     */
    private final Map<String, Integer> matrixHeader = new HashMap<>();

    /**
     * 列总数
     */
    private int columnNumber;

    /**
     * 构造IndexedTable实例
     *
     * @param columnNumber 列数
     * @param indexColumns 需要建索引的列，列编号从0开始
     */
    public Matrix(int columnNumber, int... indexColumns) {
        this.columnNumber = columnNumber;
        if (null != indexColumns && indexColumns.length > 0) {
            for (int i : indexColumns) {
                index.put(i, new HashMultiDiffValueMap<>());
            }
        }
    }

    /**
     * 查找
     *
     * @param column     列编号
     * @param key        搜索的值
     * @param projection 投影列编号
     * @return 返回所有第column列的值为key的行的第projection列的集合
     */
    public Set<T> findValues(int column, T key, int projection) {
        rangeCheck(projection);
        return findRows(column, key).stream().map(r -> r.get(projection)).collect(Collectors.toSet());
    }

    /**
     * 查找
     *
     * @param columnHeader  列标题
     * @param key           搜索的值
     * @param projectHeader 投影列标题
     * @return 返回所有columnHeader列的值为key的行的projection列的集合
     */
    public Set<T> findValues(String columnHeader, T key, String projectHeader) {
        Integer column = matrixHeader.get(columnHeader);
        Integer projection = matrixHeader.get(projectHeader);
        if (null == column || null == projection)
            return null;
        return findValues(column, key, projection);
    }

    /**
     * 查找
     *
     * @param column 列编号
     * @param key    列值
     * @return 返回所有第column列的值为key的行的集合
     */
    public List<Row<T>> findRows(int column, T key) {
        rangeCheck(column);
        List<Row<T>> result = new ArrayList<>();

        MultiDiffValueMap<T, Integer> indexMap = index.get(column);
        if (null != indexMap) {
            // 索引查找
            Set<Integer> rowIndexes = indexMap.get(key);
            if (null == rowIndexes) {
                return null;
            }
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
     * @param columnHeader 列标题
     * @param key          列值
     * @return 返回所有columnHeader列的值为key的行的集合
     */
    public List<Row<T>> findRows(String columnHeader, T key) {
        Integer column = matrixHeader.get(columnHeader);
        if (null == column)
            return null;
        return findRows(column, key);
    }

    /**
     * 向表中插入一条记录
     *
     * @param values 行数据
     * @return 当前Matrix对象
     */
    @SafeVarargs
    public final Matrix<T> insert(T... values) {
        if (null == values || values.length != columnNumber) {
            throw new IllegalArgumentException("插入数据的列数与定义不一致 需要" + columnNumber + "列");
        }

        ArrayRow<T> row = new ArrayRow<>(Arrays.asList(values));
        row.setUpdateLestener(this);
        // 插入数据
        rows.add(row);
        row.setRownum(rows.size() - 1);
        // 构建索引
        buildIndex(row);
        return this;
    }

    /**
     * 更新第column列为key的所有行
     * <p>更新所有满足条件的行的所有列，所以values必须提供所有列的新值</p>
     *
     * @param column 列编号
     * @param key    列值
     * @param values 新的行数据
     * @return 当前Matrix对象
     */
    @SafeVarargs
    public final Matrix<T> update(int column, T key, T... values) {
        Assert.isTrue(null != values && values.length == columnNumber, "更新数据的列数与定义不符 需要" + columnNumber + "列");
        // 更新操作
        List<Row<T>> row = findRows(column, key);
        for (Row<T> r : row) {
            for (int i = 0; i != columnNumber; ++i)
                r.set(i, values[i]);
        }
        return this;
    }

    /**
     * 更新第column列为key的所有行，将这些行的第updateColumn列的值为value
     *
     * @param column       列编号
     * @param key          列值
     * @param updateColumn 更新的列编号
     * @param value        更新的列值
     * @return 当前Matrix对象
     */
    public final Matrix<T> update(int column, T key, int updateColumn, T value) {
        rangeCheck(column);
        rangeCheck(updateColumn);

        List<Row<T>> row = findRows(column, key);
        for (Row<T> r : row) {
            r.set(updateColumn, value);
        }
        return this;
    }

    /**
     * 更新columnHeader列为key的所有行
     * <p>更新所有满足条件的行的所有列，所以values必须提供所有列的新值</p>
     *
     * @param columnHeader 列标题
     * @param key          列值
     * @param values       新的行数据
     * @return 当前Matrix对象
     */
    @SafeVarargs
    public final Matrix<T> update(String columnHeader, T key, T... values) {
        Integer column = matrixHeader.get(columnHeader);
        return update(Assert.notNull(column, "指定的列名不存在"), key, values);
    }

    /**
     * 更新columnHeader列为key的所有行，将这些行的updateColumnHeader列的值为value
     *
     * @param columnHeader       列标题
     * @param key                列值
     * @param updateColumnHeader 更新的列标题
     * @param value              更新的列值
     * @return 当前Matrix对象
     */
    public final Matrix<T> update(String columnHeader, T key, String updateColumnHeader, T value) {
        Integer column = matrixHeader.get(columnHeader);
        Integer updateColumn = matrixHeader.get(updateColumnHeader);
        return update(Assert.notNull(column, "指定的列名不存在"), key, Assert.notNull(updateColumn, "指定的列名不存在"), value);
    }

    /**
     * 删除所有第column列的值为key的行
     *
     * @param column 列编号，从0开始
     * @param key    列值
     */
    public boolean delete(int column, T key) {
        rangeCheck(column);
        Set<Integer> rownums = findRows(column, key).stream().map(Row::rownum).collect(Collectors.toSet());
        deleteRowIndex(rownums);
        return rows.removeIf(next -> rownums.contains(next.rownum()));
    }

    /**
     * 删除记录
     *
     * @param columnHeader 列编号
     * @param key          列值
     */
    public boolean delete(String columnHeader, T key) {
        Integer column = matrixHeader.get(columnHeader);
        return delete(Assert.notNull(column, "指定的列名不存在"), key);
    }

    public final Matrix<T> setHeader(int column, String columnHeader) {
        rangeCheck(column);
        matrixHeader.put(Assert.notBlank(columnHeader, "列名不能为空"), column);
        return this;
    }

    public final Matrix<T> createIndex(int column) {
        // 在column列上创建索引
        buildIndex(column);
        return this;
    }

    public final Matrix<T> createIndex(String columnHeader) {
        Integer column = matrixHeader.get(columnHeader);
        return createIndex(Assert.notNull(column, "指定的列标题不存在"));
    }

    @Override
    public void beforeUpdate(RowBeforeUpdateEvent event) {
        // 数据更新前，删除对应索引
        for (Integer c : event.getUpdateCols()) {
            MultiDiffValueMap<T, Integer> indexMap = index.get(c);
            //noinspection unchecked
            Row<T> row = (Row<T>) event.getSource();
            Set<Integer> rownums = indexMap.get(row.get(c));
            if (rownums.size() == 1) {
                indexMap.remove(row.get(c));
            } else {
                rownums.remove(row.rownum());
            }
        }
    }

    @Override
    public void afterUpdate(RowAfterUpdateEvent event) {
        // 数据被更新后，建立索引
        //noinspection unchecked
        buildIndex((Row<T>) event.getSource(), event.getUpdatedCols());
    }

    @Override
    public void onDelete(int rownum) {
        deleteRowIndex(rownum);
    }

    private void rangeCheck(int column) {
        Assert.isTrue(column < columnNumber && column >= 0, "列编号超出范围");
    }

    /**
     * 构建索引
     */
    private void buildIndex(int... cols) {
        for (int indexCol : cols) {
            MultiDiffValueMap<T, Integer> indexMap = index.get(indexCol);
            if (null != indexMap) {
                indexMap.clear();
                for (Row<T> row : rows) {
                    indexMap.add(row.get(indexCol), row.rownum());
                }
            }
        }
    }

    /**
     * 构建索引
     */
    private void buildIndex(Row<T> row) {
        for (Map.Entry<Integer, MultiDiffValueMap<T, Integer>> indexEntry : index.entrySet()) {
            MultiDiffValueMap<T, Integer> indexMap = indexEntry.getValue();
            int indexCol = indexEntry.getKey();
            indexMap.add(row.get(indexCol), row.rownum());
        }
    }

    /**
     * 构建索引
     */
    private void buildIndex(Row<T> row, int... cols) {
        for (int indexCol : cols) {
            MultiDiffValueMap<T, Integer> indexMap = index.get(indexCol);
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
            MultiDiffValueMap<T, Integer> indexMap = index.get(indexCol);
            if (null != indexMap) {
                indexMap.add(row.get(indexCol), row.rownum());
            }
        }
    }

    private void deleteRowIndex(Set<Integer> rownums) {
        for (MultiDiffValueMap<T, Integer> index : index.values()) {
            Iterator<Map.Entry<T, Set<Integer>>> iterator = index.entrySet().iterator();

            while (iterator.hasNext()) {
                Map.Entry<T, Set<Integer>> next = iterator.next();

                next.getValue().removeIf(rownums::contains);
                if (next.getValue().isEmpty()) {
                    iterator.remove();
                }
            }
        }
    }

    private void deleteRowIndex(Integer rownum) {
        for (MultiDiffValueMap<T, Integer> index : index.values()) {
            Iterator<Map.Entry<T, Set<Integer>>> iterator = index.entrySet().iterator();

            while (iterator.hasNext()) {
                Map.Entry<T, Set<Integer>> next = iterator.next();

                next.getValue().removeIf(rownum::equals);
                if (next.getValue().isEmpty()) {
                    iterator.remove();
                }
            }
        }
    }
}
