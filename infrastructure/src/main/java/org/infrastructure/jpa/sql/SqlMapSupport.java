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
import org.springframework.util.ReflectionUtils;

import javax.sql.DataSource;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * sqlMap 支撑类
 *
 * @author xuweinan
 * @version 1.2
 */
public class SqlMapSupport<T extends IEntity> {
    private static final Logger logger = LoggerFactory.getLogger(SqlMapSupport.class);

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
        return loader.getSQL(cmd, paramMap).getTemplate();
    }

    /**
     * 通过命令文件查询
     *
     * @param cmd      命令名称
     * @param paramMap 参数map
     * @return 查询结果
     */
    public List<Map<String, Object>> findList(String cmd, Map<String, ?> paramMap) {
        String sqlTxt = loader.getSQL(cmd, paramMap).getTemplate();
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
     * @param cmd      命令名称
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
     * @param cmd      命令名称
     * @param paramMap 参数
     */
    public Long findLong(String cmd, Map<String, ?> paramMap) {
        String sqlTxt = loader.getSQL(cmd, paramMap).getTemplate();
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
        String sqlTxt = loader.getSQL(cmd, paramMap).getTemplate();
        return this.nameJt.update(sqlTxt, paramMap);
    }

    /**
     * 分页查询
     *
     * @param cmd sql模板path
     * @param qp  查询参数
     * @return 分页查询结果
     */
    public Page<Map<String, Object>> findPage(String cmd, QueryParam qp) {
        String sqlTxt = loader.getSQL(cmd, qp.q).getTemplate();
        String sortInfo = parseOrder(qp);
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

    private String parseOrder(QueryParam p) {
        List<String> orders = new ArrayList<>();
        if (StringUtils.isNotEmpty(p.sort)) {
            for (String s : p.sort.split(",")) {
                String[] so = s.split("__");
                String field = so[0];
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
        return StringUtils.EMPTY;
    }

    /**
     * 批量更新执行
     */
    @SuppressWarnings({"unchecked"})
    public void batchExec(String cmd, List<Map> argsMap) {
        String sqlTxt = loader.getSQL(cmd, null).getTemplate();
        this.nameJt.batchUpdate(sqlTxt, argsMap.toArray(new Map[]{}));
    }

    /**
     * 将Map形式的查询结果转换为实体
     * <p>
     * 复合属性，请在语句中指定别名为实体属性的路径。如createUser.id对应createUser的id属性。<br>
     * 如果Map中存在某些Key不能与实体的属性对应，将被舍弃。
     * </p>
     *
     * @param entityValues 行对象
     * @return 返回Transient瞬态的VO
     */
    public T convertMapToVo(Map<String, Object> entityValues) {
        T t = null;
        try {
            t = this.entityClazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e1) {
            e1.printStackTrace();
        }
        for (String key : entityValues.keySet()) {
            try {
                Object keyObj = entityValues.get(key);
                if (keyObj == null)
                    continue;
                if (key.contains(".")) {
                    // 关联字段，生成关联对象
                    String[] refKeys = key.split("\\.");
                    Field refField = ReflectionUtils.findField(this.entityClazz, refKeys[0]);
                    ReflectionUtils.makeAccessible(refField);
                    Object refObj = ReflectionUtils.getField(refField, t);
                    if (refObj == null) {
                        Class refFieldClass = refField.getType();
                        refObj = refFieldClass.newInstance();
                        ReflectionUtils.makeAccessible(refField);
                        ReflectionUtils.setField(refField, t, refObj);
                    }
                    // 赋值关联对象字段的值
                    Field refObjField = ReflectionUtils.findField(refField.getType(), refKeys[1]);
                    ReflectionUtils.makeAccessible(refObjField);
                    ReflectionUtils.setField(refObjField, refObj, keyObj);
                } else {
                    // 简单字段
                    Field field = ReflectionUtils.findField(this.entityClazz, key);
                    ReflectionUtils.makeAccessible(field);
                    ReflectionUtils.setField(field, t, keyObj);
                }
            } catch (Exception e) {
                logger.error(key, e);
                throw new BizException("转化查询结果到实体错误:" + key, e);
            }
        }
        return t;
    }

    /**
     * 将Map形式的查询结果转换为实体
     * <p>
     * 复合属性，请在语句中指定别名为实体属性的路径。如createUser.id对应createUser的id属性。<br>
     * 如果Map中存在某些Key不能与实体的属性对应，将被舍弃。
     * </p>
     *
     * @param entityValues 行对象
     * @return 返回瞬态的实体bean
     */
    public T convertMapToBean(Map<String, Object> entityValues) throws IntrospectionException, IllegalAccessException, InstantiationException, InvocationTargetException {
        BeanInfo beanInfo = Introspector.getBeanInfo(entityClazz, IEntity.class);
        T entity = entityClazz.newInstance();

        PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
        for (PropertyDescriptor descriptor : propertyDescriptors) {
            String propertyName = descriptor.getName();

            if (entityValues.containsKey(propertyName)) {
                // 下面一句可以 try 起来，这样当一个属性赋值失败的时候就不会影响其他属性赋值。
                Object value = entityValues.get(propertyName);

                Object[] args = new Object[1];
                args[0] = value;

                descriptor.getWriteMethod().invoke(entity, args);
            }
        }
        return entity;
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
