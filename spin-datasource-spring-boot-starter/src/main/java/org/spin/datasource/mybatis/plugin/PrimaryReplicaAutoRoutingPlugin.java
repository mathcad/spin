package org.spin.datasource.mybatis.plugin;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.spin.datasource.CurrentDatasourceInfo;
import org.spin.datasource.spring.boot.autoconfigure.DynamicDataSourceProperties;
import org.spin.datasource.support.DbHealthIndicator;
import org.spin.datasource.support.DdConstants;
import org.spin.datasource.toolkit.DynamicDataSourceContextHolder;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Properties;

/**
 * Primary-Replica Separation Plugin with mybatis
 *
 * @author TaoYu
 * @since 2.5.1
 */
@Intercepts({
    @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
    @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})})
public class PrimaryReplicaAutoRoutingPlugin implements Interceptor {

    @Autowired
    private DynamicDataSourceProperties properties;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object[] args = invocation.getArgs();
        MappedStatement ms = (MappedStatement) args[0];
        boolean empty = true;
        try {
            CurrentDatasourceInfo datasourceInfo = DynamicDataSourceContextHolder.peek();
            empty = null == datasourceInfo || null == datasourceInfo.getDatasource();
            if (empty) {
                DynamicDataSourceContextHolder.push(getDataSource(ms));
            }
            return invocation.proceed();
        } finally {
            if (empty) {
                DynamicDataSourceContextHolder.clear();
            }
        }
    }

    /**
     * 获取动态数据源名称，重写注入 DbHealthIndicator 支持数据源健康状况判断选择
     *
     * @param mappedStatement mybatis MappedStatement
     * @return 获取真实的数据源名称
     */
    public String getDataSource(MappedStatement mappedStatement) {
        String replica = DdConstants.REPLICA;
        if (properties.isHealth()) {
            /*
             * 根据从库健康状况，判断是否切到主库
             */
            boolean health = DbHealthIndicator.getDbHealth(DdConstants.REPLICA);
            if (!health) {
                health = DbHealthIndicator.getDbHealth(DdConstants.PRIMARY);
                if (health) {
                    replica = DdConstants.PRIMARY;
                }
            }
        }
        return SqlCommandType.SELECT == mappedStatement.getSqlCommandType() ? replica : DdConstants.PRIMARY;
    }

    @Override
    public Object plugin(Object target) {
        return target instanceof Executor ? Plugin.wrap(target, this) : target;
    }

    @Override
    public void setProperties(Properties properties) {
    }
}
