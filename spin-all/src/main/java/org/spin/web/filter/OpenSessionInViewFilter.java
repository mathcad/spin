package org.spin.web.filter;

import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.spin.data.core.DataSourceContext;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.orm.hibernate5.SessionFactoryUtils;
import org.springframework.orm.hibernate5.SessionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.async.CallableProcessingInterceptor;
import org.springframework.web.context.request.async.WebAsyncManager;
import org.springframework.web.context.request.async.WebAsyncUtils;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class OpenSessionInViewFilter extends OncePerRequestFilter {

    public static final String DEFAULT_SESSION_FACTORY_BEAN_NAME = "sessionFactory";

    private String sessionFactoryBeanName = DEFAULT_SESSION_FACTORY_BEAN_NAME;


    /**
     * Set the bean name of the SessionFactory to fetch from Spring's
     * root application context. Default is "sessionFactory".
     *
     * @param sessionFactoryBeanName sesssionFactory名称
     * @see #DEFAULT_SESSION_FACTORY_BEAN_NAME
     */
    public void setSessionFactoryBeanName(String sessionFactoryBeanName) {
        this.sessionFactoryBeanName = sessionFactoryBeanName;
    }

    /**
     * Return the bean name of the SessionFactory to fetch from Spring's
     * root application context.
     *
     * @return sesssionFactory名称
     */
    protected String getSessionFactoryBeanName() {
        return this.sessionFactoryBeanName;
    }


    /**
     * Returns "false" so that the filter may re-bind the opened Hibernate
     * {@code Session} to each asynchronously dispatched thread and postpone
     * closing it until the very last asynchronous dispatch.
     */
    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        return false;
    }

    /**
     * Returns "false" so that the filter may provide a Hibernate
     * {@code Session} to each error dispatches.
     */
    @Override
    protected boolean shouldNotFilterErrorDispatch() {
        return false;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {

        SessionFactory sessionFactory = lookupSessionFactory(request);
        boolean participate = false;

        WebAsyncManager asyncManager = WebAsyncUtils.getAsyncManager(request);
        String key = getAlreadyFilteredAttributeName();

        if (TransactionSynchronizationManager.hasResource(sessionFactory)) {
            // Do not modify the Session: just set the participate flag.
            participate = true;
        } else {
            boolean isFirstRequest = !isAsyncDispatch(request);
            if (isFirstRequest || !applySessionBindingInterceptor(asyncManager, key)) {
                logger.debug("Opening Hibernate Session in OpenSessionInViewFilter");
                Session session = openSession(sessionFactory);
                SessionHolder sessionHolder = new SessionHolder(session);
                TransactionSynchronizationManager.bindResource(sessionFactory, sessionHolder);

                AsyncRequestInterceptor interceptor = new AsyncRequestInterceptor(sessionFactory, sessionHolder);
                asyncManager.registerCallableInterceptor(key, interceptor);
                asyncManager.registerDeferredResultInterceptor(key, interceptor);
            }
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            if (!participate) {
                // 切换回默认Schema
                DataSourceContext.restoreSchema();
                SessionHolder sessionHolder =
                    (SessionHolder) TransactionSynchronizationManager.unbindResource(sessionFactory);
                if (!isAsyncStarted(request)) {
                    logger.debug("Closing Hibernate Session in OpenSessionInViewFilter");

                    SessionFactoryUtils.closeSession(sessionHolder.getSession());
                }
            }
        }
    }

    /**
     * Look up the SessionFactory that this filter should use,
     * taking the current HTTP request as argument.
     * <p>The default implementation delegates to the {@link #lookupSessionFactory()}
     * variant without arguments.
     *
     * @param request the current request
     * @return the SessionFactory to use
     */
    protected SessionFactory lookupSessionFactory(HttpServletRequest request) {
        return lookupSessionFactory();
    }

    /**
     * Look up the SessionFactory that this filter should use.
     * <p>The default implementation looks for a bean with the specified name
     * in Spring's root application context.
     *
     * @return the SessionFactory to use
     * @see #getSessionFactoryBeanName
     */
    protected SessionFactory lookupSessionFactory() {
        if (logger.isDebugEnabled()) {
            logger.debug("Using SessionFactory '" + getSessionFactoryBeanName() + "' for OpenSessionInViewFilter");
        }
        WebApplicationContext wac = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
        return wac.getBean(getSessionFactoryBeanName(), SessionFactory.class);
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
    @SuppressWarnings("deprecation")
    protected Session openSession(SessionFactory sessionFactory) throws DataAccessResourceFailureException {
        try {
            Session session = sessionFactory.openSession();
            session.setFlushMode(FlushMode.MANUAL);
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

}
