package org.spin.data.sql;

import org.hibernate.cfg.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.throwable.SimplifiedException;
import org.spin.core.util.MapUtils;
import org.spin.data.core.Page;
import org.spin.data.core.PageRequest;
import org.spin.data.core.SQLLoader;
import org.spin.data.sql.dbtype.MySQLDatabaseType;
import org.spin.data.sql.dbtype.OracleDatabaseType;
import org.spin.data.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.orm.hibernate5.LocalSessionFactoryBuilder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SQL管理类
 * <p>Created by xuweinan on 2016/8/14.</p>
 *
 * @author xuweinan
 * @version 1.2
 */
@Component
public class SQLManager {
    private static final Logger logger = LoggerFactory.getLogger(SQLManager.class);
    private SQLLoader loader;
    private NamedParameterJdbcTemplate nameJt;
    private DataSource dataSource;

    @Autowired
    public SQLManager(LocalSessionFactoryBean sessFactory, SQLLoader loader) {
        this.loader = loader;
        LocalSessionFactoryBuilder cfg = (LocalSessionFactoryBuilder) sessFactory.getConfiguration();
        DataSource ds = (DataSource) cfg.getProperties().get(Environment.DATASOURCE);
        dataSource = ds;
        try (Connection conn = ds.getConnection()) {
            String vender = conn.getMetaData().getDatabaseProductName();
            if ("MySQL".equalsIgnoreCase(vender))
                loader.setDbType(new MySQLDatabaseType());
            else if ("Oracle".equalsIgnoreCase(vender))
                loader.setDbType(new OracleDatabaseType());
            if (loader.getDbType() == null)
                throw new SimplifiedException("Unsupported Database vender:" + vender);
        } catch (SQLException e) {
            logger.error("Can not fetch Database metadata", e);
        }
        this.nameJt = new NamedParameterJdbcTemplate(ds);
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
        String sqlTxt = loader.getSQL(sqlId, paramMap).getTemplate();
        if (logger.isDebugEnabled())
            logger.debug(sqlId + ":\n" + sqlTxt);
        try {
            return nameJt.queryForList(sqlTxt, paramMap);
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
        SQLSource sql = loader.getSQL(sqlId, paramMap);
        SQLSource pageSql = loader.getPagedSQL(sqlId, paramMap, pageRequest);
        List<Map<String, Object>> list;
        if (logger.isDebugEnabled())
            logger.debug(sqlId + ":\n" + pageSql.getTemplate());
        try {
            list = nameJt.queryForList(pageSql.getTemplate(), paramMap);
        } catch (Exception ex) {
            logger.error("执行查询出错：" + sqlId);
            logger.error(pageSql.getTemplate());
            throw new SimplifiedException("执行查询出错：", ex);
        }
        // 增加总数统计 去除排序语句
        String totalSqlTxt = "SELECT COUNT(1) FROM (" + sql.getTemplate() + ") out_alias";
//        SqlParameterSource params = new MapSqlParameterSource(paramMap);
        Long total = nameJt.queryForObject(totalSqlTxt, paramMap, Long.class);
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
        SQLSource sql = loader.getSQL(sqlId, paramMap);
        SQLSource pageSql = loader.getPagedSQL(sqlId, paramMap, pageRequest);
        List<Map<String, Object>> list;
        if (logger.isDebugEnabled())
            logger.debug(sqlId + ":\n" + pageSql.getTemplate());
        try {
            list = nameJt.queryForList(pageSql.getTemplate(), paramMap);
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
        String totalSqlTxt = "SELECT COUNT(1) FROM (" + sql.getTemplate() + ")";
//        SqlParameterSource params = new MapSqlParameterSource(paramMap);
        Long total = nameJt.queryForObject(totalSqlTxt, paramMap, Long.class);
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
        return loader.getSQL(sqlId, paramMap);
    }

    /**
     * 通过sqlId查询总数
     *
     * @param sqlId    命令名称
     * @param paramMap 参数
     */
    public Long count(String sqlId, Map<String, ?> paramMap) {
        String sqlTxt = loader.getSQL(sqlId, paramMap).getTemplate();
        sqlTxt = "SELECT COUNT(1) FROM (" + sqlTxt + ")";
        if (logger.isDebugEnabled())
            logger.debug(sqlId + ":\n" + sqlTxt);
        Long number = nameJt.queryForObject(sqlTxt, paramMap, Long.class);
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
        String sqlTxt = loader.getSQL(sqlId, paramMap).getTemplate();
        if (logger.isDebugEnabled())
            logger.debug(sqlId + ":\n" + sqlTxt);
        return nameJt.update(sqlTxt, paramMap);
    }

    /**
     * 批量更新
     */
    @SuppressWarnings({"unchecked"})
    public void batchExec(String sqlId, List<Map<String, ?>> argsMap) {
        String sqlTxt = loader.getSQL(sqlId, null).getTemplate();
        if (logger.isDebugEnabled())
            logger.debug(sqlId + ":\n" + sqlTxt);
        nameJt.batchUpdate(sqlTxt, argsMap.toArray(new Map[]{}));
    }

    public NamedParameterJdbcTemplate getNameJt() {
        return nameJt;
    }

    public DataSource getDataSource() {
        return dataSource;
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
