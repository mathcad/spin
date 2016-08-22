/*
 *  Copyright 2002-2016 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.infrastructure.jpa.sql;

import org.infrastructure.jpa.api.QueryParam;
import org.infrastructure.jpa.core.IEntity;
import org.infrastructure.jpa.core.SQLLoader;
import org.infrastructure.jpa.core.Page;
import org.infrastructure.throwable.BizException;
import org.infrastructure.util.BeanUtils;
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
 * SQL管理类
 *
 * @author xuweinan
 * @version 1.2
 */
public class SQLManager<T extends IEntity> {
    private static final Logger logger = LoggerFactory.getLogger(SQLManager.class);

    @Autowired
    private SQLLoader loader;
    private String $sqlName = "$sql";
    private NamedParameterJdbcTemplate nameJt;
    private JdbcTemplate jt;
    private DataSource dataSource;
    protected Class<T> entityClazz;

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
     * @param sqlId     命令名称
     * @param mapParams key1,value1,key2,value2,key3,value3 ...
     * @return 查询结果
     */
    public List<Map<String, Object>> findListMap(String sqlId, Object... mapParams) {
        return findListMap(sqlId, HashUtils.getMap(mapParams));
    }

    /**
     * 通过命令文件查询
     *
     * @param sqlId    命令名称
     * @param paramMap 参数map
     * @return 查询结果
     */
    public List<Map<String, Object>> findListMap(String sqlId, Map<String, ?> paramMap) {
        String sqlTxt = loader.getSQL(sqlId, paramMap).getTemplate();
        try {
            return this.nameJt.queryForList(sqlTxt, paramMap);
        } catch (Exception ex) {
            logger.error("执行查询出错：" + sqlId);
            logger.error(sqlTxt);
            throw new BizException("执行查询出错：", ex);
        }
    }

    /**
     * 分页查询
     *
     * @param sqlId sql模板path
     * @param qp    查询参数
     * @return 分页查询结果
     */
    public Page<Map<String, Object>> findPageMap(String sqlId, QueryParam qp) {
        String sqlTxt = loader.getSQL(sqlId, qp.q).getTemplate();
        String sortInfo = qp.getOrderSql();
        // 替换排序为自定义
        if (StringUtils.isNotEmpty(sortInfo)) {
            sqlTxt = removeLastOrderBy(sqlTxt);
            sqlTxt = sqlTxt + sortInfo;
        }

        String pageSqlTxt = loader.getSQL($sqlName + "." + "findPage", HashUtils.getMap("sqlTxt", sqlTxt, "start", qp.start, "limit", qp.limit)).getTemplate();

        List<Map<String, Object>> list = nameJt.queryForList(pageSqlTxt, qp.q);

        // 增加总数统计 去除排序语句
        String totalSqlTxt = loader.getSQL($sqlName + "." + "findTotal", HashUtils.getMap("sqlTxt", sqlTxt)).getTemplate();
        SqlParameterSource params = new MapSqlParameterSource(qp.q);
        Number number = this.nameJt.queryForObject(totalSqlTxt, params, Long.class);
        Long total = (number != null ? number.longValue() : 0);
        return new Page<>(list, total);
    }

    /**
     * 查找单个对象
     *
     * @param sqlId    命令名称
     * @param paramMap 参数
     */
    public Map<String, Object> findOneMap(String sqlId, Map<String, ?> paramMap) {
        List<Map<String, Object>> list = this.findListMap(sqlId, paramMap);
        if (list.size() > 0) {
            return list.get(0);
        } else
            return null;
    }

    /**
     * 通过命令文件查询 + 参数Map数组（HashUtils.getMap构建参数数组）
     *
     * @param sqlId     命令名称
     * @param mapParams 参数map
     * @return 查询结果
     */
    public List<T> findList(String sqlId, Object... mapParams) {
        List<Map<String, Object>> maps = findListMap(sqlId, HashUtils.getMap(mapParams));
        List<T> res = new ArrayList<>();
        for (Map<String, Object> map : maps) {
            try {
                res.add(BeanUtils.wrapperMapToBean(this.entityClazz, map));
            } catch (Exception ignored) {
            }
        }
        return res;
    }

    /**
     * 通过命令文件查询 + 参数Map数组（HashUtils.getMap构建参数数组）
     *
     * @param sqlId    命令名称
     * @param paramMap key1,value1,key2,value2,key3,value3 ...
     * @return 查询结果
     */
    public List<T> findList(String sqlId, Map<String, ?> paramMap) {
        List<Map<String, Object>> maps = this.findListMap(sqlId, paramMap);
        List<T> res = new ArrayList<>();
        for (Map<String, Object> map : maps) {
            try {
                res.add(BeanUtils.wrapperMapToBean(this.entityClazz, map));
            } catch (Exception ignored) {
            }
        }
        return res;
    }

    /**
     * 查找单个对象
     *
     * @param sqlId    命令名称
     * @param paramMap 参数
     */
    public T findOne(String sqlId, Map<String, ?> paramMap) {
        List<Map<String, Object>> list = this.findListMap(sqlId, paramMap);
        if (list.size() > 0) {
            try {
                return BeanUtils.wrapperMapToBean(this.entityClazz, list.get(0));
            } catch (Exception ignored) {
            }
        }
        return null;
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
        Long number = this.nameJt.queryForObject(sqlTxt, paramMap, Long.class);
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
        return this.nameJt.update(sqlTxt, paramMap);
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

    /**
     * 批量更新
     */
    @SuppressWarnings({"unchecked"})
    public void batchExec(String sqlId, List<Map<String, ?>> argsMap) {
        String sqlTxt = loader.getSQL(sqlId, null).getTemplate();
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

    public Class<T> getEntityClazz() {
        return entityClazz;
    }
}
