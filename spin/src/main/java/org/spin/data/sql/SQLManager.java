package org.spin.data.sql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.throwable.SimplifiedException;
import org.spin.core.util.MapUtils;
import org.spin.core.util.StringUtils;
import org.spin.data.core.Page;
import org.spin.data.core.PageRequest;
import org.spin.data.core.SQLLoader;
import org.spin.data.extend.DataSourceConfig;
import org.spin.data.extend.MultiDataSourceConfig;
import org.spin.data.sql.dbtype.MySQLDatabaseType;
import org.spin.data.sql.dbtype.OracleDatabaseType;
import org.spin.data.sql.dbtype.PostgreSQLDatabaseType;
import org.spin.data.sql.dbtype.SQLServerDatabaseType;
import org.spin.data.sql.dbtype.SQLiteDatabaseType;
import org.spin.data.sql.resolver.TemplateResolver;
import org.spin.data.util.EntityUtils;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SQL管理类-支持多数据源
 * <p>Created by xuweinan on 2016/8/14.</p>
 *
 * @author xuweinan
 * @version 1.3
 */
public class SQLManager {
    private static final Logger logger = LoggerFactory.getLogger(SQLManager.class);
    private static final Map<String, DataSource> DATA_SOURCE_MAP = new HashMap<>();
    private final Map<String, SQLLoader> loaderMap = new HashMap<>();
    private final Map<String, NamedParameterJdbcTemplate> nameJtMap = new HashMap<>();
    private final String primaryDataSourceName;

    private ThreadLocal<String> currentDataSourceName = new ThreadLocal<>();
    private ThreadLocal<SQLLoader> currentLoader = new ThreadLocal<>();
    private ThreadLocal<NamedParameterJdbcTemplate> currentNameJt = new ThreadLocal<>();

    /**
     * 多数据源时的构造方法
     *
     * @param dsConfigs       多数据源配置
     * @param loaderClassName SQLLoader类名
     * @param rootUri         sql文件根路径
     * @param resolver        sql模板解析器
     */
    public SQLManager(MultiDataSourceConfig<?> dsConfigs, String loaderClassName, String rootUri, TemplateResolver resolver) throws ClassNotFoundException {
        @SuppressWarnings("unchecked")
        Class<SQLLoader> loaderClass = (Class<SQLLoader>) Class.forName(loaderClassName);

        primaryDataSourceName = dsConfigs.getPrimaryDataSource();
        dsConfigs.getDataSources().forEach((name, config) -> {
            try {
                SQLLoader loader = loaderClass.getDeclaredConstructor().newInstance();
                if (StringUtils.isEmpty(rootUri)) {
                    loader.setRootUri(rootUri);
                }
                loader.setTemplateResolver(resolver);
                switch (config.getVenderName()) {
                    case "MYSQL":
                        loader.setDbType(new MySQLDatabaseType());
                        break;
                    case "ORACLE":
                        loader.setDbType(new OracleDatabaseType());
                        break;
                    case "MICROSOFT":
                    case "SQLSERVER":
                        loader.setDbType(new SQLServerDatabaseType());
                        break;
                    case "POSTGRESQL":
                        loader.setDbType(new PostgreSQLDatabaseType());
                        break;
                    case "SQLITE":
                        loader.setDbType(new SQLiteDatabaseType());
                        break;
                    default:
                        throw new SimplifiedException("Unsupported Database vender:" + config.getVenderName());
                }
                loaderMap.put(name, loader);
                NamedParameterJdbcTemplate jt = new NamedParameterJdbcTemplate(DATA_SOURCE_MAP.get(name));
                nameJtMap.put(name, jt);
            } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
                throw new SimplifiedException("Can not create SQLLoader instance:" + loaderClassName);
            }
        });
        switchDataSource(primaryDataSourceName);
    }

    /**
     * 单数据源时的构造方法
     *
     * @param dsConfig        单数据源配置
     * @param loaderClassName SQLLoader类名
     * @param rootUri         sql文件根路径
     * @param resolver        sql模板解析器
     */
    public SQLManager(DataSourceConfig dsConfig, String loaderClassName, String rootUri, TemplateResolver resolver) throws ClassNotFoundException {
        @SuppressWarnings("unchecked")
        Class<SQLLoader> loaderClass = (Class<SQLLoader>) Class.forName(loaderClassName);

        String name = dsConfig.getName();
        if (StringUtils.isEmpty(name)) {
            name = "main";
            dsConfig.setName(name);
        }
        primaryDataSourceName = name;
        try {
            SQLLoader loader = loaderClass.getDeclaredConstructor().newInstance();
            if (StringUtils.isEmpty(rootUri)) {
                loader.setRootUri(rootUri);
            }
            loader.setTemplateResolver(resolver);
            switch (dsConfig.getVenderName()) {
                case "MYSQL":
                    loader.setDbType(new MySQLDatabaseType());
                    break;
                case "ORACLE":
                    loader.setDbType(new OracleDatabaseType());
                    break;
                case "MICROSOFT":
                case "SQLSERVER":
                    loader.setDbType(new SQLServerDatabaseType());
                    break;
                case "POSTGRESQL":
                    loader.setDbType(new PostgreSQLDatabaseType());
                    break;
                case "SQLITE":
                    loader.setDbType(new SQLiteDatabaseType());
                    break;
                default:
                    throw new SimplifiedException("Unsupported Database vender:" + dsConfig.getVenderName());
            }
            loaderMap.put(name, loader);
            NamedParameterJdbcTemplate jt = new NamedParameterJdbcTemplate(DATA_SOURCE_MAP.get(name));
            nameJtMap.put(name, jt);
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new SimplifiedException("Can not create SQLLoader instance:" + loaderClassName);
        }
        switchDataSource(primaryDataSourceName);
    }

    /**
     * 注册数据源，应该在调用SQLManager构造方法之前完成所有数据源的注册
     *
     * @param name       数据源名称
     * @param dataSource 数据源
     */
    public static void registDataSource(String name, DataSource dataSource) {
        DATA_SOURCE_MAP.put(name, dataSource);
    }

    /**
     * 查找单个对象
     */
    public Map<String, Object> findOneAsMap(String sqlId, Map<String, ?> paramMap) {
        List<Map<String, Object>> list = listAsMap(sqlId, paramMap);
        if (!list.isEmpty()) {
            return list.get(0);
        } else
            return null;
    }

    /**
     * 查找单个对象
     */
    public <T> T findOne(String sqlId, Class<T> entityClazz, Map<String, ?> paramMap) {
        List<Map<String, Object>> list = listAsMap(sqlId, paramMap);
        if (!list.isEmpty()) {
            try {
                return EntityUtils.wrapperMapToBean(entityClazz, list.get(0));
            } catch (Exception e) {
                logger.error("Entity vrappe error", e);
            }
        }
        return null;
    }

    /**
     * 通过命令文件查询
     */
    public List<Map<String, Object>> listAsMap(String sqlId, Object... mapParams) {
        return listAsMap(sqlId, MapUtils.ofMap(mapParams));
    }

    /**
     * 通过命令文件查询
     */
    public List<Map<String, Object>> listAsMap(String sqlId, Map<String, ?> paramMap) {
        String sqlTxt = getCurrentSqlLoader().getSQL(sqlId, paramMap).getTemplate();
        if (logger.isDebugEnabled())
            logger.debug(sqlId + ":\n" + sqlTxt);
        try {
            return getCurrentNamedJt().queryForList(sqlTxt, paramMap);
        } catch (Exception ex) {
            logger.error("执行查询出错：" + sqlId);
            logger.error(sqlTxt);
            throw new SimplifiedException("执行查询出错：", ex);
        }
    }

    /**
     * 分页查询
     */
    public Page<Map<String, Object>> listAsPageMap(String sqlId, Map<String, ?> paramMap, PageRequest pageRequest) {
        SQLSource sql = getCurrentSqlLoader().getSQL(sqlId, paramMap);
        SQLSource pageSql = getCurrentSqlLoader().getPagedSQL(sqlId, paramMap, pageRequest);
        List<Map<String, Object>> list;
        if (logger.isDebugEnabled())
            logger.debug(sqlId + ":\n" + pageSql.getTemplate());
        try {
            list = getCurrentNamedJt().queryForList(pageSql.getTemplate(), paramMap);
        } catch (Exception ex) {
            logger.error("执行查询出错：" + sqlId);
            logger.error(pageSql.getTemplate());
            throw new SimplifiedException("执行查询出错：", ex);
        }
        // 增加总数统计 去除排序语句
        String totalSqlTxt = "SELECT COUNT(1) FROM (" + sql.getTemplate() + ") OUT_ALIAS";
//        SqlParameterSource params = new MapSqlParameterSource(paramMap);
        Long total = getCurrentNamedJt().queryForObject(totalSqlTxt, paramMap, Long.class);
        return new Page<>(list, total != null ? total : 0, pageRequest.getPageSize());
    }

    /**
     * 通过命令文件查询
     */
    public <T> List<T> list(String sqlId, Class<T> entityClazz, Object... mapParams) {
        List<Map<String, Object>> maps = listAsMap(sqlId, MapUtils.ofMap(mapParams));
        List<T> res = new ArrayList<>();
        for (Map<String, Object> map : maps) {
            try {
                res.add(EntityUtils.wrapperMapToBean(entityClazz, map));
            } catch (Exception e) {
                logger.error("Entity vrappe error", e);
            }
        }
        return res;
    }

    /**
     * 通过命令文件查询
     */
    public <T> List<T> list(String sqlId, Class<T> entityClazz, Map<String, ?> paramMap) {
        List<Map<String, Object>> maps = listAsMap(sqlId, paramMap);
        List<T> res = new ArrayList<>();
        for (Map<String, Object> map : maps) {
            try {
                res.add(EntityUtils.wrapperMapToBean(entityClazz, map));
            } catch (Exception e) {
                logger.error("Entity vrappe error", e);
            }
        }
        return res;
    }

    /**
     * 分页查询
     */
    public <T> Page<T> listAsPage(String sqlId, Class<T> entityClazz, Map<String, ?> paramMap, PageRequest pageRequest) {
        SQLSource sql = getCurrentSqlLoader().getSQL(sqlId, paramMap);
        SQLSource pageSql = getCurrentSqlLoader().getPagedSQL(sqlId, paramMap, pageRequest);
        List<Map<String, Object>> list;
        if (logger.isDebugEnabled())
            logger.debug(sqlId + ":\n" + pageSql.getTemplate());
        try {
            list = getCurrentNamedJt().queryForList(pageSql.getTemplate(), paramMap);
        } catch (Exception ex) {
            logger.error("执行查询出错：" + sqlId);
            logger.error(pageSql.getTemplate());
            throw new SimplifiedException("执行查询出错：", ex);
        }
        List<T> res = new ArrayList<>();
        for (Map<String, Object> map : list) {
            try {
                res.add(EntityUtils.wrapperMapToBean(entityClazz, map));
            } catch (Exception e) {
                logger.error("Entity vrappe error", e);
            }
        }
        // 增加总数统计 去除排序语句
        String totalSqlTxt = "SELECT COUNT(1) FROM (" + sql.getTemplate() + ") OUT_ALIAS";
//        SqlParameterSource params = new MapSqlParameterSource(paramMap);
        Long total = getCurrentNamedJt().queryForObject(totalSqlTxt, paramMap, Long.class);
        return new Page<>(res, total != null ? total : 0, pageRequest.getPageSize());
    }

    /**
     * 获取sqlmap中的语句
     *
     * @param sqlId    sql命令path
     * @param paramMap 参数
     * @return sqlmap中的语句
     */
    public SQLSource getSQL(String sqlId, Map<String, ?> paramMap) {
        return getCurrentSqlLoader().getSQL(sqlId, paramMap);
    }

    /**
     * 通过sqlId查询总数
     *
     * @param sqlId    命令名称
     * @param paramMap 参数
     */
    public Long count(String sqlId, Map<String, ?> paramMap) {
        String sqlTxt = getCurrentSqlLoader().getSQL(sqlId, paramMap).getTemplate();
        sqlTxt = "SELECT COUNT(1) FROM (" + sqlTxt + ") OUT_ALIAS";
        if (logger.isDebugEnabled())
            logger.debug(sqlId + ":\n" + sqlTxt);
        Long number = getCurrentNamedJt().queryForObject(sqlTxt, paramMap, Long.class);
        return number != null ? number : 0;
    }

    /**
     * 执行update/insert/delete命令
     *
     * @param sqlId    命令名称
     * @param paramMap 参数
     * @return 影响条目
     */
    public int executeCUD(String sqlId, Map<String, ?> paramMap) {
        String sqlTxt = getCurrentSqlLoader().getSQL(sqlId, paramMap).getTemplate();
        if (logger.isDebugEnabled())
            logger.debug(sqlId + ":\n" + sqlTxt);
        return getCurrentNamedJt().update(sqlTxt, paramMap);
    }

    /**
     * 批量更新
     */
    @SuppressWarnings({"unchecked"})
    public void batchExec(String sqlId, List<Map<String, ?>> argsMap) {
        String sqlTxt = getCurrentSqlLoader().getSQL(sqlId, null).getTemplate();
        if (logger.isDebugEnabled())
            logger.debug(sqlId + ":\n" + sqlTxt);
        getCurrentNamedJt().batchUpdate(sqlTxt, argsMap.toArray(new Map[]{}));
    }

    /**
     * 切换数据源
     *
     * @param name 数据源名称
     */
    public void switchDataSource(String name) {
        if (!DATA_SOURCE_MAP.containsKey(name)) {
            throw new SimplifiedException("切换的数据源不存在:" + name);
        }
        currentDataSourceName.set(name);
        currentLoader.set(loaderMap.get(name));
        currentNameJt.set(nameJtMap.get(name));
    }

    /**
     * 切换到默认数据源
     */
    public void usePrimaryDataSource() {
        switchDataSource(primaryDataSourceName);
    }

    /**
     * 获取当前数据源名称
     *
     * @return 数据源名称
     */
    public String getCurrentDataSourceName() {
        if (Objects.isNull(currentDataSourceName.get())) {
            switchDataSource(primaryDataSourceName);
        }
        return currentDataSourceName.get();
    }

    private SQLLoader getCurrentSqlLoader() {
        if (Objects.isNull(currentLoader.get())) {
            switchDataSource(primaryDataSourceName);
        }
        return currentLoader.get();
    }

    private NamedParameterJdbcTemplate getCurrentNamedJt() {
        if (Objects.isNull(currentNameJt.get())) {
            switchDataSource(primaryDataSourceName);
        }
        return currentNameJt.get();
    }

    private String removeLastOrderBy(String sql) {
        int sortStart = -1;
        int sortEnd = 0;
        String patter = "(order\\s+by[^)]+)";
        Pattern p = Pattern.compile(patter, Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(sql);
        while (m.find()) {
            sortStart = m.start(0);
            sortEnd = m.end(0);
        }
        if (sortStart > -1 && sortEnd == sql.length()) {
            sql = sql.substring(0, sortStart);
        }
        return sql;
    }
}
