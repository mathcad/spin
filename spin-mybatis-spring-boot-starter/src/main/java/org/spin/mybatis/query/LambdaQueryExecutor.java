package org.spin.mybatis.query;

import com.baomidou.mybatisplus.core.conditions.AbstractLambdaWrapper;
import com.baomidou.mybatisplus.core.conditions.ISqlSegment;
import com.baomidou.mybatisplus.core.conditions.SharedString;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.Query;
import com.baomidou.mybatisplus.core.conditions.segments.MergeSegments;
import com.baomidou.mybatisplus.core.enums.SqlKeyword;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.*;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import org.spin.cloud.vo.CurrentUser;
import org.spin.cloud.vo.DataPermInfo;
import org.spin.core.util.ClassUtils;
import org.spin.core.util.CollectionUtils;
import org.spin.mybatis.entity.AbstractDataPermEntity;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import static java.util.stream.Collectors.joining;

/**
 * MyBatis Plus lambda query执行器
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2021/1/4</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class LambdaQueryExecutor<T> extends AbstractLambdaWrapper<T, LambdaQueryExecutor<T>>
    implements Query<LambdaQueryExecutor<T>, T, SFunction<T, ?>>, MappedExecutor<T, BaseMapper<T>> {

    private boolean hasDataPerm = false;
    private BaseMapper<T> mapper;

    @Override
    public BaseMapper<T> repo() {
        return mapper;
    }

    @Override
    public Wrapper<T> getQuery() {
        return this;
    }

    public void setRepo(BaseMapper<T> mapper) {
        this.mapper = mapper;
    }


    /**
     * 查询字段
     */
    private SharedString sqlSelect = new SharedString();

    public LambdaQueryExecutor(Class<T> entityClass) {
        super.setEntityClass(entityClass);
        super.initNeed();
    }

    /**
     * 不建议直接 new 该实例，使用 Wrappers.lambdaQuery(...)
     */
    LambdaQueryExecutor(T entity, Class<T> entityClass, SharedString sqlSelect, AtomicInteger paramNameSeq,
                        Map<String, Object> paramNameValuePairs, MergeSegments mergeSegments,
                        SharedString lastSql, SharedString sqlComment, SharedString sqlFirst) {
        super.setEntity(entity);
        super.setEntityClass(entityClass);
        this.paramNameSeq = paramNameSeq;
        this.paramNameValuePairs = paramNameValuePairs;
        this.expression = mergeSegments;
        this.sqlSelect = sqlSelect;
        this.lastSql = lastSql;
        this.sqlComment = sqlComment;
        this.sqlFirst = sqlFirst;
    }

    /**
     * SELECT 部分 SQL 设置
     *
     * @param columns 查询字段
     */
    @SafeVarargs
    @Override
    public final LambdaQueryExecutor<T> select(SFunction<T, ?>... columns) {
        if (ArrayUtils.isNotEmpty(columns)) {
            this.sqlSelect.setStringValue(columnsToString(false, columns));
        }
        return typedThis;
    }

    /**
     * SELECT 部分 SQL 设置
     *
     * @param columns 查询字段
     * @return executor
     */
    public final LambdaQueryExecutor<T> select(String... columns) {
        if (ArrayUtils.isNotEmpty(columns)) {
            this.sqlSelect.setStringValue(org.spin.core.util.StringUtils.join(columns, ","));
        }
        return typedThis;
    }

    /**
     * 过滤查询的字段信息(主键除外!)
     * <p>例1: 只要 java 字段名以 "test" 开头的             -&gt; select(i -&gt; i.getProperty().startsWith("test"))</p>
     * <p>例2: 只要 java 字段属性是 CharSequence 类型的     -&gt; select(TableFieldInfo::isCharSequence)</p>
     * <p>例3: 只要 java 字段没有填充策略的                 -&gt; select(i -&gt; i.getFieldFill() == FieldFill.DEFAULT)</p>
     * <p>例4: 要全部字段                                   -&gt; select(i -&gt; true)</p>
     * <p>例5: 只要主键字段                                 -&gt; select(i -&gt; false)</p>
     *
     * @param predicate 过滤方式
     * @return this
     */
    @Override
    public LambdaQueryExecutor<T> select(Class<T> entityClass, Predicate<TableFieldInfo> predicate) {
        if (entityClass == null) {
            entityClass = getEntityClass();
        } else {
            setEntityClass(entityClass);
        }
        Assert.notNull(entityClass, "entityClass can not be null");
        this.sqlSelect.setStringValue(TableInfoHelper.getTableInfo(entityClass).chooseSelect(predicate));
        return typedThis;
    }

    @Override
    public String getSqlSelect() {
        return sqlSelect.getStringValue();
    }

    /**
     * 开启数据权限控制
     *
     * @return 当前查询对象
     */
    public LambdaQueryExecutor<T> dataPerm() {
        if (hasDataPerm) {
            return this;
        }
        hasDataPerm = true;

        if (null == getEntityClass() || !ClassUtils.isAssignable(getEntityClass(), AbstractDataPermEntity.class)) {
            return this;
        }

        DataPermInfo dataPermInfo = CurrentUser.getCurrentNonNull().getDataPermInfo();
        if (null == dataPermInfo) {
            return this;
        }

        if (Boolean.TRUE.equals(dataPermInfo.getHimself())) {
            and(i -> i.cond(true, "create_by", SqlKeyword.EQ, CurrentUser.getCurrent().getId()));
        } else {
            and(i -> i.or().cond(null == dataPermInfo.getHimself(), "create_by", SqlKeyword.EQ, CurrentUser.getCurrent().getId())
                .or().cond(CollectionUtils.isNotEmpty(dataPermInfo.getDeptIds()), "department_id", SqlKeyword.IN, dataPermInfo.getDeptIds())
                .or().cond(CollectionUtils.isNotEmpty(dataPermInfo.getStationIds()), "station_id", SqlKeyword.IN, dataPermInfo.getStationIds())
            );
        }

        return this;
    }

    /**
     * 用于生成嵌套 sql
     * <p>故 sqlSelect 不向下传递</p>
     */
    @Override
    protected LambdaQueryExecutor<T> instance() {
        return new LambdaQueryExecutor<>(getEntity(), getEntityClass(), null, paramNameSeq, paramNameValuePairs,
            new MergeSegments(), SharedString.emptyString(), SharedString.emptyString(), SharedString.emptyString());
    }


    @Override
    public void clear() {
        super.clear();
        sqlSelect.toNull();
    }

    private LambdaQueryExecutor<T> cond(boolean condition, String column, SqlKeyword keyword, Object val) {
        ISqlSegment statement;
        switch (keyword) {
            case EQ:
                statement = () -> formatSql("{0}", val);
                break;
            case IN:
            case NOT_IN:
                statement = inExpression((Collection<?>) val);
                break;
            default:
                throw new IllegalArgumentException("不支持的操作符:" + keyword.name());
        }
        return doIt(condition, () -> column, keyword, statement);
    }

    public ISqlSegment inExpression(Collection<?> value) {

        if (CollectionUtils.isEmpty(value)) {
            return () -> "()";
        }
        return () -> value.stream().map(i -> formatParam(null, i))
            .collect(joining(StringPool.COMMA, StringPool.LEFT_BRACKET, StringPool.RIGHT_BRACKET));
    }

    /**
     * 对sql片段进行组装
     *
     * @param condition   是否执行
     * @param sqlSegments sql片段数组
     * @return children
     */
    protected LambdaQueryExecutor<T> doIt(boolean condition, ISqlSegment... sqlSegments) {
        if (condition) {
            expression.add(sqlSegments);
        }
        return typedThis;
    }

    protected final String formatSql(String sqlStr, Object... params) {
        return formatSqlIfNeed(true, sqlStr, params);
    }

    protected final String formatSqlIfNeed(boolean need, String sqlStr, Object... params) {
        if (!need || StringUtils.isBlank(sqlStr)) {
            return null;
        }
        if (ArrayUtils.isNotEmpty(params)) {
            for (int i = 0; i < params.length; ++i) {
                String genParamName = Constants.WRAPPER_PARAM + paramNameSeq.incrementAndGet();
                sqlStr = sqlStr.replace(String.format("{%s}", i),
                    String.format("#{%s.paramNameValuePairs.%s}", Constants.WRAPPER, genParamName));
                paramNameValuePairs.put(genParamName, params[i]);
            }
        }
        return sqlStr;
    }
}
