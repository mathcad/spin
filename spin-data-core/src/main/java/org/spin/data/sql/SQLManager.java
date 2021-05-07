package org.spin.data.sql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.gson.reflect.TypeToken;
import org.spin.core.throwable.SimplifiedException;
import org.spin.core.util.CollectionUtils;
import org.spin.core.util.MapUtils;
import org.spin.core.util.StringUtils;
import org.spin.data.core.Page;
import org.spin.data.core.PageRequest;
import org.spin.data.rs.MapRowMapper;
import org.spin.data.rs.RowMapper;
import org.spin.data.rs.RowMappers;
import org.spin.data.sql.param.ParameterizedSql;
import org.spin.data.sql.resolver.TemplateResolver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * SQL支撑管理类
 * <p>Created by xuweinan on 2016/8/14.</p>
 *
 * @author xuweinan
 * @version 1.5
 */
public class SQLManager {
    private static final Logger logger = LoggerFactory.getLogger(SQLManager.class);
    private static final String COUNT_SQL = "SELECT COUNT(1) FROM (%s) OUT_ALIAS";
    private static final String QUERY_ERROR = "执行查询出错";
    private static final String SQL_LOG = "sqlId: %s\nsqlText: %s";
    private static final int DEFAULT_CACHE_LIMIT = 256;
    private static final RowMapper<Map<String, Object>> DEFAULT_ROW_MAPPER = new MapRowMapper();

    /**
     * SQL缓存区容量
     */
    private volatile int cacheLimit = DEFAULT_CACHE_LIMIT;
    private final Map<String, ParameterizedSql> parsedSqlCache =
        new LinkedHashMap<>(DEFAULT_CACHE_LIMIT, 0.75f, true) {
            private static final long serialVersionUID = 3202723764687686913L;

            @Override
            protected boolean removeEldestEntry(Map.Entry<String, ParameterizedSql> eldest) {
                return size() > cacheLimit;
            }
        };
    private final SQLLoader sqlLoader;

    /**
     * 构造方法
     *
     * @param loaderClass SQLLoader类
     * @param rootUri     sql文件根路径
     * @param resolver    sql模板解析器
     */
    public SQLManager(Class<? extends SQLLoader> loaderClass, String rootUri, TemplateResolver resolver) {
        try {
            SQLLoader loader = loaderClass.getDeclaredConstructor().newInstance();
            if (StringUtils.isNotEmpty(rootUri)) {
                loader.setRootUri(rootUri);
            }
            loader.setTemplateResolver(resolver);
            sqlLoader = loader;
        } catch (Exception e) {
            throw new SimplifiedException("Can not create SQLLoader instance:" + loaderClass.getName());
        }
    }

    /**
     * 查找单个对象
     *
     * @param connection jdbc连接
     * @param sqlId      sqlId
     * @param paramMap   命名参数
     * @return 查询结果
     */
    public Optional<Map<String, Object>> findOneAsMap(Connection connection, String sqlId, Map<String, ?> paramMap) {
        ParameterizedSql parsedSql = getParsedSql(sqlLoader.getSQL(sqlId, paramMap));
        return executeQueryForOneRow(connection, parsedSql, paramMap, DEFAULT_ROW_MAPPER);
    }

    /**
     * 查找单个对象
     *
     * @param connection  jdbc连接
     * @param sqlId       sqlId
     * @param entityClazz 查询的实体类型
     * @param paramMap    命名参数
     * @param <T>         实体类型
     * @return 查询结果
     */
    public <T> Optional<T> findOne(Connection connection, String sqlId, Class<T> entityClazz, Map<String, ?> paramMap) {
        ParameterizedSql parsedSql = getParsedSql(sqlLoader.getSQL(sqlId, paramMap));
        return executeQueryForOneRow(connection, parsedSql, paramMap, RowMappers.getMapper(TypeToken.get(entityClazz)));
    }

    /**
     * 通过命令文件查询
     *
     * @param connection jdbc连接
     * @param sqlId      sqlId
     * @param mapParams  参数
     * @return 查询结果
     */
    public List<Map<String, Object>> listAsMap(Connection connection, String sqlId, Object... mapParams) {
        return listAsMap(connection, sqlId, MapUtils.ofMap(mapParams));
    }

    /**
     * 通过命令文件查询
     *
     * @param connection jdbc连接
     * @param sqlId      sqlId
     * @param paramMap   命名参数
     * @return 查询结果
     */
    public List<Map<String, Object>> listAsMap(Connection connection, String sqlId, Map<String, ?> paramMap) {
        ParameterizedSql parsedSql = getParsedSql(sqlLoader.getSQL(sqlId, paramMap));
        return executeQuery(connection, parsedSql, paramMap, DEFAULT_ROW_MAPPER);
    }

    /**
     * 分页查询
     *
     * @param connection  jdbc连接
     * @param sqlId       sqlId
     * @param paramMap    命名参数
     * @param pageRequest 分页参数
     * @return 查询结果
     */
    public Page<Map<String, Object>> listAsPageMap(Connection connection, String sqlId, Map<String, ?> paramMap, PageRequest pageRequest) {
        ParameterizedSql parsedPageSql = getParsedSql(sqlLoader.getPagedSQL(JdbcUtils.getDbType(connection), sqlId, paramMap, pageRequest));
        ParameterizedSql countSql = getParsedSql(sqlLoader.getSQL(sqlId, paramMap));

        List<Map<String, Object>> res = executeQuery(connection, parsedPageSql, paramMap, DEFAULT_ROW_MAPPER);
        long total = total(connection, countSql, paramMap, false);
        return new Page<>(res, pageRequest.getCurrent(), total, pageRequest.getSize());
    }

    /**
     * 分页查询
     *
     * @param connection  jdbc连接
     * @param sqlId       sqlId
     * @param countSqlId  总数查询sqlId
     * @param paramMap    命名参数
     * @param pageRequest 分页参数
     * @return 查询结果
     */
    public Page<Map<String, Object>> listAsPageMap(Connection connection, String sqlId, String countSqlId, Map<String, ?> paramMap, PageRequest pageRequest) {
        ParameterizedSql parsedPageSql = getParsedSql(sqlLoader.getPagedSQL(JdbcUtils.getDbType(connection), sqlId, paramMap, pageRequest));
        ParameterizedSql countSql = getParsedSql(sqlLoader.getSQL(countSqlId, paramMap));

        List<Map<String, Object>> res = executeQuery(connection, parsedPageSql, paramMap, DEFAULT_ROW_MAPPER);
        long total = total(connection, countSql, paramMap, true);
        return new Page<>(res, pageRequest.getCurrent(), total, pageRequest.getSize());
    }

    /**
     * 通过命令文件查询
     *
     * @param connection  jdbc连接
     * @param sqlId       sqlId
     * @param entityClazz 查询实体类型
     * @param mapParams   参数
     * @param <T>         实体类型
     * @return 查询结果
     */
    public <T> List<T> list(Connection connection, String sqlId, Class<T> entityClazz, Object... mapParams) {
        return list(connection, sqlId, entityClazz, MapUtils.ofMap(mapParams));
    }

    /**
     * 通过命令文件查询
     *
     * @param connection  jdbc连接
     * @param sqlId       sqlId
     * @param entityClazz 查询实体类型
     * @param paramMap    命名参数
     * @param <T>         实体类型
     * @return 查询结果
     */
    public <T> List<T> list(Connection connection, String sqlId, Class<T> entityClazz, Map<String, ?> paramMap) {
        ParameterizedSql parsedSql = getParsedSql(sqlLoader.getSQL(sqlId, paramMap));

        return executeQuery(connection, parsedSql, paramMap, RowMappers.getMapper(TypeToken.get(entityClazz)));
    }

    /**
     * 分页查询
     *
     * @param connection  jdbc连接
     * @param sqlId       sqlId
     * @param entityClazz 查询实体类型
     * @param paramMap    命名参数
     * @param pageRequest 分页参数
     * @param <T>         实体类型
     * @return 查询结果
     */
    public <T> Page<T> listAsPage(Connection connection, String sqlId, Class<T> entityClazz, Map<String, ?> paramMap, PageRequest pageRequest) {
        ParameterizedSql parsedSql = getParsedSql(sqlLoader.getSQL(sqlId, paramMap));
        ParameterizedSql parsedPageSql = getParsedSql(sqlLoader.getPagedSQL(JdbcUtils.getDbType(connection), sqlId, paramMap, pageRequest));

        List<T> res = executeQuery(connection, parsedPageSql, paramMap, RowMappers.getMapper(TypeToken.get(entityClazz)));
        long total = total(connection, parsedSql, paramMap, false);
        return new Page<>(res, pageRequest.getCurrent(), total, pageRequest.getSize());
    }

    /**
     * 分页查询
     *
     * @param connection  jdbc连接
     * @param sqlId       sqlId
     * @param countSqlId  总数查询sqlId
     * @param entityClazz 查询实体类型
     * @param paramMap    命名参数
     * @param pageRequest 分页参数
     * @param <T>         实体类型
     * @return 查询结果
     */
    public <T> Page<T> listAsPage(Connection connection, String sqlId, String countSqlId, Class<T> entityClazz, Map<String, ?> paramMap, PageRequest pageRequest) {
        ParameterizedSql parsedSql = getParsedSql(sqlLoader.getSQL(countSqlId, paramMap));
        ParameterizedSql parsedPageSql = getParsedSql(sqlLoader.getPagedSQL(JdbcUtils.getDbType(connection), sqlId, paramMap, pageRequest));

        List<T> res = executeQuery(connection, parsedPageSql, paramMap, RowMappers.getMapper(TypeToken.get(entityClazz)));
        long total = total(connection, parsedSql, paramMap, true);
        return new Page<>(res, pageRequest.getCurrent(), total, pageRequest.getSize());
    }

    /**
     * 通过sqlId查询总数
     *
     * @param connection jdbc连接
     * @param sqlId      命令名称
     * @param paramMap   命名参数
     * @return 记录总数
     */
    public Long count(Connection connection, String sqlId, Map<String, ?> paramMap) {
        ParameterizedSql parsedSql = getParsedSql(sqlLoader.getSQL(sqlId, paramMap));
        return total(connection, parsedSql, paramMap, false);
    }

    /**
     * 执行update/insert/delete命令
     *
     * @param connection jdbc连接
     * @param sqlId      命令名称
     * @param paramMap   命名参数
     * @return 受影响行数
     */
    public int executeCUD(Connection connection, String sqlId, Map<String, ?> paramMap) {
        ParameterizedSql parsedSql = getParsedSql(sqlLoader.getSQL(sqlId, paramMap));
        if (logger.isDebugEnabled()) {
            logger.debug(String.format(SQL_LOG, sqlId, parsedSql.getActualSql()));
        }
        try (PreparedStatement ps = connection.prepareStatement(parsedSql.getActualSql().getSql())) {
            JdbcUtils.setParameterValues(ps, parsedSql.getNamedParameters(), paramMap);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new SimplifiedException(QUERY_ERROR, e);
        }
    }

    /**
     * 批量更新
     *
     * @param connection jdbc连接
     * @param sqlId      sqlId
     * @param paramMaps  命名参数
     * @return 受影响行数
     */
    public int[] executeBatch(Connection connection, String sqlId, List<Map<String, ?>> paramMaps) {
        ParameterizedSql parsedSql = getParsedSql(sqlLoader.getSQL(sqlId, null));

        if (logger.isDebugEnabled()) {
            logger.debug(String.format(SQL_LOG, sqlId, parsedSql.getActualSql()));
        }
        try (PreparedStatement ps = connection.prepareStatement(parsedSql.getActualSql().getSql())) {
            if (JdbcUtils.supportsBatchUpdates(connection)) {
                for (Map<String, ?> paramMap : paramMaps) {
                    JdbcUtils.setParameterValues(ps, parsedSql.getNamedParameters(), paramMap);
                    ps.addBatch();
                }
                return ps.executeBatch();
            } else {
                int[] rowsAffected = new int[paramMaps.size()];
                for (int i = 0; i < paramMaps.size(); i++) {
                    JdbcUtils.setParameterValues(ps, parsedSql.getNamedParameters(), paramMaps.get(i));
                    rowsAffected[i] = ps.executeUpdate();
                }
                return rowsAffected;
            }
        } catch (SQLException e) {
            throw new SimplifiedException(QUERY_ERROR, e);
        }
    }

    public int getCacheLimit() {
        return cacheLimit;
    }

    public void setCacheLimit(int cacheLimit) {
        this.cacheLimit = cacheLimit;
    }

    /**
     * 通过sql执行查询
     *
     * @param connection jdbc连接
     * @param parsedSql  解析后的SQL
     * @param paramMap   命名参数
     * @param mapper     数据转换器
     * @return 数据列表
     */
    private <T> List<T> executeQuery(Connection connection, ParameterizedSql parsedSql, Map<String, ?> paramMap, RowMapper<T> mapper) {
        if (logger.isDebugEnabled()) {
            logger.debug(String.format(SQL_LOG, parsedSql.getId(), parsedSql.getActualSql()));
        }
        try (PreparedStatement ps = connection.prepareStatement(parsedSql.getActualSql().getSql())) {
            JdbcUtils.setParameterValues(ps, parsedSql.getNamedParameters(), paramMap);
            try (ResultSet rs = ps.executeQuery()) {
                return mapper.extractData(rs);
            }
        } catch (SQLException e) {
            throw new SimplifiedException(QUERY_ERROR, e);
        }
    }

    /**
     * 通过sql执行查询，返回查询结果的第一行
     *
     * @param connection jdbc连接
     * @param parsedSql  解析后的SQL
     * @param paramMap   命名参数
     * @param mapper     数据转换器
     * @return 第一条数据
     */
    private <T> Optional<T> executeQueryForOneRow(Connection connection, ParameterizedSql parsedSql, Map<String, ?> paramMap, RowMapper<T> mapper) {
        if (logger.isDebugEnabled()) {
            logger.debug(String.format(SQL_LOG, parsedSql.getId(), parsedSql.getActualSql()));
        }
        try (PreparedStatement ps = connection.prepareStatement(parsedSql.getActualSql().getSql())) {
            JdbcUtils.setParameterValues(ps, parsedSql.getNamedParameters(), paramMap);
            try (ResultSet rs = ps.executeQuery()) {
                List<T> tList = mapper.extractData(rs, 1);
                if (CollectionUtils.isEmpty(tList)) {
                    return Optional.empty();
                } else {
                    return Optional.ofNullable(tList.get(0));
                }
            }
        } catch (SQLException e) {
            throw new SimplifiedException(QUERY_ERROR, e);
        }
    }

    /**
     * 解析命名参数
     *
     * @param originSql 原始SQL
     * @return 解析后的SQL
     */
    private ParameterizedSql getParsedSql(SqlSource originSql) {
        if (getCacheLimit() <= 0) {
            return new ParameterizedSql(originSql);
        }
        ParameterizedSql parsedSql = parsedSqlCache.get(originSql.getSql());
        if (parsedSql == null) {
            synchronized (parsedSqlCache) {
                parsedSql = parsedSqlCache.get(originSql.getSql());
                if (parsedSql == null) {
                    parsedSql = new ParameterizedSql(originSql);
                    parsedSqlCache.put(originSql.getSql(), parsedSql);
                }
            }
        }
        return parsedSql;
    }

    private long total(Connection connection, ParameterizedSql parsedSql, Map<String, ?> paramMap, boolean isCntSql) {
        String totalSqlTxt = isCntSql ? parsedSql.getActualSql().getSql() : String.format(COUNT_SQL, parsedSql.getActualSql().getSql());
        try (PreparedStatement ps = connection.prepareStatement(totalSqlTxt)) {
            JdbcUtils.setParameterValues(ps, parsedSql.getNamedParameters(), paramMap);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                } else {
                    throw new SimplifiedException(QUERY_ERROR);
                }
            }
        } catch (SQLException e) {
            throw new SimplifiedException(QUERY_ERROR, e);
        }
    }
}
