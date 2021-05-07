package org.spin.datasource.aop;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.spin.datasource.processor.DsProcessor;
import org.spin.datasource.support.DataSourceClassResolver;
import org.spin.datasource.toolkit.DynamicDataSourceContextHolder;
import org.springframework.lang.NonNull;

/**
 * Core Interceptor of Dynamic Datasource
 *
 * @author TaoYu
 * @since 1.2.0
 */
public class DynamicDataSourceAnnotationInterceptor implements MethodInterceptor {

    /**
     * The identification of SPEL.
     */
    private static final String DYNAMIC_PREFIX = "#";

    private final DataSourceClassResolver dataSourceClassResolver;
    private final DsProcessor dsProcessor;

    public DynamicDataSourceAnnotationInterceptor(Boolean allowedPublicOnly, DsProcessor dsProcessor) {
        dataSourceClassResolver = new DataSourceClassResolver(allowedPublicOnly);
        this.dsProcessor = dsProcessor;
    }

    @Override
    public Object invoke(@NonNull MethodInvocation invocation) throws Throwable {
        String dsKey = determineDatasourceKey(invocation);
        DynamicDataSourceContextHolder.push(dsKey);
        try {
            return invocation.proceed();
        } finally {
            DynamicDataSourceContextHolder.poll();
        }
    }

    private String determineDatasourceKey(MethodInvocation invocation) {
        String key = dataSourceClassResolver.findDSKey(invocation.getMethod(), invocation.getThis());
        return (!key.isEmpty() && key.startsWith(DYNAMIC_PREFIX)) ? dsProcessor.determineDatasource(invocation, key) : key;
    }
}
