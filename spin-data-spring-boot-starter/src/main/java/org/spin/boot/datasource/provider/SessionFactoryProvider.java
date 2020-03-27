package org.spin.boot.datasource.provider;

import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.util.StringUtils;
import org.spin.data.core.DataSourceContext;
import org.springframework.web.method.HandlerMethod;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2020/3/27</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public abstract class SessionFactoryProvider {
    private static final Logger logger = LoggerFactory.getLogger(SessionFactoryProvider.class);
    private static final String DEFAULT_SESSION_FACTORY_BEAN_NAME = "SessionFactory";

    public abstract SessionFactory getSessionFactory(HandlerMethod handlerMethod);

    /**
     * Look up the SessionFactory that this filter should use.
     * <p>The default implementation looks for a bean with the specified name
     * in Spring's root application context.
     *
     * @param dsName 数据源名称
     * @return the SessionFactory to use
     */
    protected SessionFactory lookupSessionFactory(String dsName) {
        dsName = StringUtils.isEmpty(dsName) ? DataSourceContext.getPrimaryDataSourceName() : dsName;
        DataSourceContext.switchDataSource(dsName);
        String sn = DataSourceContext.getCurrentDataSourceName() + DEFAULT_SESSION_FACTORY_BEAN_NAME;
        if (logger.isDebugEnabled()) {
            logger.debug("Using SessionFactory '" + sn + "' for OpenSessionInViewFilter");
        }
        return DataSourceContext.getCurrentSessionFactory();
    }
}
