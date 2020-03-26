package org.spin.boot.datasource.interceptor;

import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.boot.datasource.annotation.Ds;
import org.spin.boot.datasource.filter.AsyncRequestInterceptor;
import org.spin.core.util.StringUtils;
import org.spin.data.core.DataSourceContext;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.orm.hibernate5.SessionFactoryUtils;
import org.springframework.orm.hibernate5.SessionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.context.request.async.CallableProcessingInterceptor;
import org.springframework.web.context.request.async.WebAsyncManager;
import org.springframework.web.context.request.async.WebAsyncUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2020/3/26</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class OpenSessionInViewInterceptor implements HandlerInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(OpenSessionInViewInterceptor.class);
    public static final String PARTICIPATE_SUFFIX = ".PARTICIPATE";
    private static final String DEFAULT_SESSION_FACTORY_BEAN_NAME = "SessionFactory";
    private static final ThreadLocal<SessionFactory> CURRENT_SF = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 是否调用方法
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        Method method = ((HandlerMethod) handler).getMethod();

        Ds ds = AnnotatedElementUtils.getMergedAnnotation(method, Ds.class);

        SessionFactory sessionFactory = lookupSessionFactory(ds);
        CURRENT_SF.set(sessionFactory);

        String key = getParticipateAttributeName(sessionFactory);
        WebAsyncManager asyncManager = WebAsyncUtils.getAsyncManager(request);
        if (asyncManager.hasConcurrentResult() && applySessionBindingInterceptor(asyncManager, key)) {
            return true;
        }

        if (TransactionSynchronizationManager.hasResource(sessionFactory)) {
            // Do not modify the Session: just mark the request accordingly.
            Integer count = (Integer) request.getAttribute(key);
            int newCount = (count != null ? count + 1 : 1);
            request.setAttribute(getParticipateAttributeName(sessionFactory), newCount);
        } else {
            logger.debug("Opening Hibernate Session in OpenSessionInViewFilter");
            Session session = openSession(sessionFactory);
            SessionHolder sessionHolder = new SessionHolder(session);
            TransactionSynchronizationManager.bindResource(sessionFactory, sessionHolder);

            AsyncRequestInterceptor interceptor = new AsyncRequestInterceptor(sessionFactory, sessionHolder);
            asyncManager.registerCallableInterceptor(key, interceptor);
            asyncManager.registerDeferredResultInterceptor(key, interceptor);
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        if (null != CURRENT_SF.get() && TransactionSynchronizationManager.hasResource(CURRENT_SF.get())) {
            SessionHolder sessionHolder = (SessionHolder) TransactionSynchronizationManager.unbindResource(CURRENT_SF.get());
            logger.debug("Closing Hibernate Session in OpenSessionInViewFilter");
            SessionFactoryUtils.closeSession(sessionHolder.getSession());
            // 切换回默认Schema
            DataSourceContext.restoreSchema();
            CURRENT_SF.remove();
        }
    }

    /**
     * Look up the SessionFactory that this filter should use.
     * <p>The default implementation looks for a bean with the specified name
     * in Spring's root application context.
     *
     * @return the SessionFactory to use
     */
    private SessionFactory lookupSessionFactory(Ds ds) {
        String dsName = null == ds || StringUtils.isEmpty(ds.value()) ? DataSourceContext.getPrimaryDataSourceName() : ds.value();
        DataSourceContext.switchDataSource(dsName);
        String sn = DataSourceContext.getCurrentDataSourceName() + DEFAULT_SESSION_FACTORY_BEAN_NAME;
        if (logger.isDebugEnabled()) {
            logger.debug("Using SessionFactory '" + sn + "' for OpenSessionInViewFilter");
        }
        return DataSourceContext.getCurrentSessionFactory();
    }

    /**
     * Open a Session for the SessionFactory that this filter uses.
     * <p>The default implementation delegates to the {@link SessionFactory#openSession}
     * method and sets the {@link Session}'s flush mode to "MANUAL".
     *
     * @param sessionFactory the SessionFactory that this filter uses
     * @return the Session to use
     * @throws DataAccessResourceFailureException if the Session could not be created
     * @see FlushMode#MANUAL
     */
    protected Session openSession(SessionFactory sessionFactory) throws DataAccessResourceFailureException {
        try {
            Session session = sessionFactory.openSession();
            session.setHibernateFlushMode(FlushMode.MANUAL);
            return session;
        } catch (HibernateException ex) {
            throw new DataAccessResourceFailureException("Could not open Hibernate Session", ex);
        }
    }

    private boolean applySessionBindingInterceptor(WebAsyncManager asyncManager, String key) {
        CallableProcessingInterceptor cpi = asyncManager.getCallableInterceptor(key);
        if (cpi == null) {
            return false;
        }
        ((AsyncRequestInterceptor) cpi).bindSession();
        return true;
    }

    private String getParticipateAttributeName(SessionFactory sf) {
        return sf.toString() + PARTICIPATE_SUFFIX;
    }

}
