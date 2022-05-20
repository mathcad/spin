package org.spin.mybatis.query;

import com.baomidou.mybatisplus.core.conditions.AbstractLambdaWrapper;
import com.baomidou.mybatisplus.core.conditions.ISqlSegment;
import com.baomidou.mybatisplus.core.conditions.SharedString;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.segments.MergeSegments;
import com.baomidou.mybatisplus.core.conditions.update.Update;
import com.baomidou.mybatisplus.core.enums.SqlKeyword;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.ArrayUtils;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import org.spin.cloud.vo.CurrentUser;
import org.spin.cloud.vo.DataPermInfo;
import org.spin.core.util.ClassUtils;
import org.spin.core.util.CollectionUtils;
import org.spin.core.util.StringUtils;
import org.spin.mybatis.entity.AbstractDataPermEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * MyBatis Plus lambda update执行器
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2021/1/4</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class LambdaUpdateExecutor<T> extends AbstractLambdaWrapper<T, LambdaUpdateExecutor<T>>
    implements Update<LambdaUpdateExecutor<T>, SFunction<T, ?>>, MappedUpdateExecutor<T, BaseMapper<T>> {

    private boolean hasDataPerm = false;
    private BaseMapper<T> mapper;

    @Override
    public BaseMapper<T> repo() {
        return mapper;
    }

    @Override
    public Wrapper<T> getUpdate() {
        return this;
    }

    public void setRepo(BaseMapper<T> mapper) {
        this.mapper = mapper;
    }

    /**
     * SQL 更新字段内容，例如：name='1', age=2
     */
    private final List<String> sqlSet;

    public LambdaUpdateExecutor() {
        // 如果无参构造函数，请注意实体 NULL 情况 SET 必须有否则 SQL 异常
        this((T) null);
    }

    public LambdaUpdateExecutor(T entity) {
        super.setEntity(entity);
        super.initNeed();
        this.sqlSet = new ArrayList<>();
    }

    public LambdaUpdateExecutor(Class<T> entityClass) {
        super.setEntityClass(entityClass);
        super.initNeed();
        this.sqlSet = new ArrayList<>();
    }

    LambdaUpdateExecutor(T entity, Class<T> entityClass, List<String> sqlSet, AtomicInteger paramNameSeq,
                         Map<String, Object> paramNameValuePairs, MergeSegments mergeSegments, SharedString paramAlias,
                         SharedString lastSql, SharedString sqlComment, SharedString sqlFirst) {
        super.setEntity(entity);
        super.setEntityClass(entityClass);
        this.sqlSet = sqlSet;
        this.paramNameSeq = paramNameSeq;
        this.paramNameValuePairs = paramNameValuePairs;
        this.expression = mergeSegments;
        this.paramAlias = paramAlias;
        this.lastSql = lastSql;
        this.sqlComment = sqlComment;
        this.sqlFirst = sqlFirst;
    }


    /**
     * 开启数据权限控制
     *
     * @return 当前查询对象
     */
    public LambdaUpdateExecutor<T> dataPerm() {
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

    @Override
    public LambdaUpdateExecutor<T> set(boolean condition, SFunction<T, ?> column, Object val, String mapping) {
        return maybeDo(condition, () -> {
            String sql = formatParam(mapping, val);
            sqlSet.add(columnToString(column) + Constants.EQUALS + sql);
        });
    }

    @Override
    public LambdaUpdateExecutor<T> setSql(boolean condition, String sql) {
        if (condition && StringUtils.isNotBlank(sql)) {
            sqlSet.add(sql);
        }
        return typedThis;
    }

    @Override
    public String getSqlSet() {
        if (CollectionUtils.isEmpty(sqlSet)) {
            return null;
        }
        return String.join(Constants.COMMA, sqlSet);
    }

    @Override
    protected LambdaUpdateExecutor<T> instance() {
        return new LambdaUpdateExecutor<>(getEntity(), getEntityClass(), null, paramNameSeq, paramNameValuePairs,
            new MergeSegments(), paramAlias, SharedString.emptyString(), SharedString.emptyString(), SharedString.emptyString());
    }

    @Override
    public void clear() {
        super.clear();
        sqlSet.clear();
    }

    /**
     * 对sql片段进行组装
     *
     * @param condition   是否执行
     * @param sqlSegments sql片段数组
     * @return children
     */
    protected LambdaUpdateExecutor<T> doIt(boolean condition, ISqlSegment... sqlSegments) {
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

    private LambdaUpdateExecutor<T> cond(boolean condition, String column, SqlKeyword keyword, Object val) {
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
}
