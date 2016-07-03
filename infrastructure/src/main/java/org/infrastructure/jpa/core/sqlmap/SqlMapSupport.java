package org.infrastructure.jpa.core.sqlmap;

import org.infrastructure.jpa.api.QueryParam;
import org.infrastructure.jpa.dto.Page;
import org.infrastructure.throwable.BizException;
import org.infrastructure.util.HashUtils;
import org.infrastructure.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * sqlMap 支撑者
 */
public class SqlMapSupport {
    private static final Logger logger = LoggerFactory.getLogger(SqlMapSupport.class);

    @Autowired
    private SqlLoader loader;

    private String $sqlName = "$sql";

    private NamedParameterJdbcTemplate nameJt;

    private JdbcTemplate jt;

    private DataSource dataSource;

    /**
     * 利用Datasource 初始化jdbcTemplate和NamedParameterJdbcTemplate
     *
     * @param dataSource 数据源
     */
    public void initDataSource(DataSource dataSource) {
        this.initDataSource(dataSource, "$sql");
    }

    /**
     * 利用Datasource 初始化jdbcTemplate和NamedParameterJdbcTemplate
     *
     * @param dataSource 数据源
     * @param $sqlName   默认sqlName
     */
    public void initDataSource(DataSource dataSource, String $sqlName) {
        this.dataSource = dataSource;
        this.nameJt = new NamedParameterJdbcTemplate(dataSource);
        this.jt = new JdbcTemplate(dataSource);
        this.$sqlName = $sqlName;
    }

    /**
     * 通过命令文件查询 + 参数Map数组（HashUtils.getMap构建参数数组）
     *
     * @param cmd       命令名称
     * @param mapParams key1,value1,key2,value2,key3,value3 ...
     * @return 查询结果
     */
    public List<Map<String, Object>> findList(String cmd, Object... mapParams) {
        return findList(cmd, HashUtils.getMap(mapParams));
    }

    /**
     * 获取sqlmap中的语句
     *
     * @param cmd      sql命令path
     * @param paramMap 参数
     * @return sqlmap中的语句
     */
    public String getFromSqlMap(String cmd, Map<String, ?> paramMap) {
        return loader.getSql(cmd, paramMap);
    }

    /**
     * 通过命令文件查询
     *
     * @param cmd      命令名称
     * @param paramMap 参数map
     * @return 查询结果
     */
    public List<Map<String, Object>> findList(String cmd, Map<String, ?> paramMap) {
        String sqlTxt = loader.getSql(cmd, paramMap);
        try {
            return this.nameJt.queryForList(sqlTxt, paramMap);
        } catch (Exception ex) {
            logger.error("执行查询出错：" + cmd);
            logger.error(sqlTxt);
            throw new BizException("执行查询出错：", ex);
        }
    }

    /**
     * 查找单行Map
     *
     * @param cmd 命令名称
     * @param paramMap 参数
     */
    public Map<String, Object> findMap(String cmd, Map<String, ?> paramMap) {
        List<Map<String, Object>> list = this.findList(cmd, paramMap);
        if (list.size() > 0) {
            return list.get(0);
        } else
            return null;
    }

    /**
     * 通过cmd命令查询总数
     *
     * @param cmd 命令名称
     * @param paramMap 参数
     */
    public Long findLong(String cmd, Map<String, ?> paramMap) {
        String sqlTxt = loader.getSql(cmd, paramMap);
        Number number = this.nameJt.queryForObject(sqlTxt, paramMap, Long.class);
        return number != null ? number.longValue() : 0;
    }

    /**
     * 执行update/insert命令
     *
     * @param cmd      命令名
     * @param paramMap 参数
     * @return 影响条目
     */
    public int execute(String cmd, Map<String, ?> paramMap) {
        String sqlTxt = loader.getSql(cmd, paramMap);
        return this.nameJt.update(sqlTxt, paramMap);
    }

    /**
     * 通过分页查询
     *
     * @param cmd sql模板path
     * @param qp  查询参数
     * @return 分页查询结果
     */
    public Page<Map<String, Object>> findPage(String cmd, QueryParam qp) {
        String sqlTxt = loader.getSql(cmd, qp.q);
        String sortInfo = parseOrder(qp);
        // 替换排序为自定义
        if (StringUtils.isNotEmpty(sortInfo)) {
            sqlTxt = removeLastOrderBy(sqlTxt);
            sqlTxt = sqlTxt + sortInfo;
        }

        String pageSqlTxt = loader.getSql($sqlName + "." + "findPage",
                HashUtils.getMap("sqlTxt", sqlTxt, "start", qp.start, "limit", qp.limit));

        List<Map<String, Object>> list = nameJt.queryForList(pageSqlTxt, qp.q);

        // 增加总数统计 去除排序语句
        String totalSqlTxt = loader.getSql($sqlName + "." + "findTotal", HashUtils.getMap("sqlTxt", sqlTxt));
        SqlParameterSource params = new MapSqlParameterSource(qp.q);
        Number number = this.nameJt.queryForObject(totalSqlTxt, params, Long.class);
        Long total = (number != null ? number.longValue() : 0);
        return new Page<>(list, total);
    }

    private String removeLastOrderBy(String sql) {
        int sortStart = -1;
        int sortEnd = 0;
        String patter = "(order\\s+by[^\\)]+)";
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

    private String parseOrder(QueryParam p) {
        List<String> orders = new ArrayList<>();

        if (StringUtils.isNotEmpty(p.sort)) {
            for (String s : p.sort.split(",")) {
                String[] so = s.split("__");
                String field = so[0];// .indexOf(".")>-1 ? "'" + so[0]+"'" :
                // so[0];
                if (so.length == 1) {
                    orders.add(field + " asc");
                } else {
                    orders.add(so[1].equalsIgnoreCase("desc") ? field + " desc" : field + " asc");
                }
            }
        }

        if (orders.size() > 0) {
            return "order by " + StringUtils.join(orders.toArray(), ",");
        }
        return null;
    }

    /**
     * 批量更新执行
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void batchExec(String cmd, List<Map> argsMap) {
        String sqlTxt = loader.getSql(cmd, null);
        this.nameJt.batchUpdate(sqlTxt, argsMap.toArray(new Map[]{}));
    }

    public JdbcTemplate getJt() {
        return jt;
    }

    public NamedParameterJdbcTemplate getNameJt() {
        return nameJt;
    }

    public DataSource getDataSource() {
        return this.dataSource;
    }
}
