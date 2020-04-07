package org.spin.mybatis;

import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.spin.core.util.BeanUtils;

import java.sql.Connection;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2020/4/2</p>
 *
 * @author xuweinan
 * @version 1.0
 */
@Intercepts({@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})})
public class DataPermissionInterceptor implements Interceptor {

    private static final Pattern DATA_PERMISSION_PATTERN = Pattern.compile("##DataPerm-(\\w+)(-(.+))?##");

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
        BoundSql boundSql = statementHandler.getBoundSql();
        String sql = boundSql.getSql();

        Matcher matcher = DATA_PERMISSION_PATTERN.matcher(sql);

        boolean modified = false;
        while (matcher.find()) {
            modified = true;
            sql = matcher.replaceFirst(QueryBuilder.buildDataPermSql(matcher.group(3), matcher.group(1)));
            matcher = DATA_PERMISSION_PATTERN.matcher(sql);
        }
        if (modified) {
            BeanUtils.setFieldValue(boundSql, "sql", sql);
        }

        return invocation.proceed();
    }

    @Override
    public Object plugin(Object target) {
        return null;
    }

    @Override
    public void setProperties(Properties properties) {

    }
}
