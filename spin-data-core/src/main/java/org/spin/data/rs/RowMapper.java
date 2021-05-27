package org.spin.data.rs;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * ResultSet数据行转换器
 * <p>Created by xuweinan on 2018/3/22.</p>
 *
 * @param <R> 转换类型
 * @author xuweinan
 */
@FunctionalInterface
public interface RowMapper<R> {

    /**
     * 将ResultSet中的当前行转换为指定类型
     *
     * @param columnVisitor column访问者
     * @param rowIdx        当前行的索引
     * @return 转换后的对象
     * @throws SQLException SQLException
     */
    R apply(ColumnVisitor columnVisitor, int rowIdx) throws SQLException;

    /**
     * 从ResultSet中抽取数据到List中
     *
     * @param rs ResultSet
     * @return 结果列表
     * @throws SQLException sql异常
     */
    default List<R> extractData(ResultSet rs) throws SQLException {
        return extractData(rs, Integer.MAX_VALUE);
    }

    /**
     * 从ResultSet中抽取指定n行的数据到List中
     *
     * @param rs   ResultSet
     * @param rows 行数
     * @return 结果列表
     * @throws SQLException sql异常
     */
    default List<R> extractData(ResultSet rs, int rows) throws SQLException {
        List<R> results = new LinkedList<>();
        int rowNum = 0;
        ColumnVisitor columnVisitor = new ColumnVisitor(rs);
        while (rs.next() && rowNum < rows) {
            results.add(apply(columnVisitor, rowNum++));
        }
        return results;
    }

//    default Flux<R> extractDataReactive(ResultSet rs) throws SQLException {
//        return extractDataReactive(rs, Integer.MAX_VALUE);
//    }
//
//    default Flux<R> extractDataReactive(ResultSet rs, int rows) throws SQLException {
//        ColumnVisitor columnVisitor = new ColumnVisitor(rs);
//        return Flux.generate(AtomicInteger::new, (state, sink) -> {
//            int r = state.getAndIncrement();
//            try {
//                if (rs.next() && r < rows) {
//                    sink.next(apply(columnVisitor, r));
//                } else {
//                    sink.complete();
//                }
//            } catch (SQLException e) {
//                sink.error(e);
//            }
//            return state;
//        });
//    }

    /**
     * Returns a composed function that first applies this function to
     * its input, and then applies the {@code after} function to the result.
     * If evaluation of either function throws an exception, it is relayed to
     * the caller of the composed function.
     *
     * @param <V>   the type of output of the {@code after} function, and of the
     *              composed function
     * @param after the function to apply after this function is applied
     * @return a composed function that first applies this function and then
     * applies the {@code after} function
     */
    default <V> RowMapper<V> andThen(Function<? super R, ? extends V> after) {
        Objects.requireNonNull(after);
        return (ColumnVisitor cv, int r) -> after.apply(apply(cv, r));
    }
}
