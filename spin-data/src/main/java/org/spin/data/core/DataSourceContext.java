package org.spin.data.core;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.engine.jdbc.spi.JdbcCoordinator;
import org.hibernate.internal.AbstractSessionImpl;
import org.hibernate.internal.AbstractSharedSessionContract;
import org.hibernate.internal.SessionImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.Assert;
import org.spin.core.throwable.SimplifiedException;
import org.spin.core.util.ReflectionUtils;
import org.spin.data.extend.RepositoryContext;

import javax.persistence.TransactionRequiredException;
import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 数据源上下文
 * <p>提供数据源的管理功能。多数据源的切换，当前数据源内的schema切换（不影响当前事务）</p>
 * <strong>手动开启Session，Transaction，切换数据源与Schema是线程上的全局操作，会影响所有从{@link ARepository}继承的持久化类与{@link RepositoryContext}</strong>
 * <pre>
 *     1.数据源切换只针对当前线程，新的线程仍是默认数据源
 *     2.切换schema只针对当前数据源的当前活动{@link Session}的生命周期，当通过openSession或openTransaction
 *       强制开启新{@link Session}时，仍是默认schema；当{@link Session}关闭时，指定的schema也将失效。
 * </pre>
 * <p>Created by xuweinan on 2018/3/25.</p>
 *
 * @author xuweinan
 */
public final class DataSourceContext {
    private static final Logger logger = LoggerFactory.getLogger(DataSourceContext.class);
    private static final Map<String, DataSource> DATA_SOURCE_MAP = new HashMap<>();
    private static final Map<String, SessionFactory> SESSION_FACTORY_MAP = new HashMap<>();
    private static final Map<String, String> DEFAULT_SCHEMA_MAP = new HashMap<>();
    private static final ThreadLocal<String> currentDataSourceName = new ThreadLocal<>();
    private static final ThreadLocal<Map<String, String>> currentSchema = new ThreadLocal<>();
    private static final ThreadLocal<Deque<Session>> THREADLOCAL_SESSIONS = new ThreadLocal<Deque<Session>>() {
    };
    private static final Object jdbcCoordLock = new Object();

    private static String primaryDataSourceName;
    private static Field jdbcCoordinatorField = null;

    private DataSourceContext() {
    }

    public static void setPrimaryDataSourceName(String dataSourceName) {
        primaryDataSourceName = dataSourceName;
    }

    /**
     * 注册数据源，应该在调用SQLManager构造方法之前完成所有数据源的注册
     *
     * @param name       数据源名称
     * @param dataSource 数据源
     */
    public static void registDataSource(String name, DataSource dataSource) {
        DATA_SOURCE_MAP.put(name, dataSource);
        try (Connection conn = dataSource.getConnection()) {
            DEFAULT_SCHEMA_MAP.put(name, conn.getCatalog());
        } catch (SQLException e) {
            throw new SimplifiedException("数据源注册失败", e);
        }
    }

    public static DataSource getDataSource(String name) {
        return DATA_SOURCE_MAP.get(name);
    }

    /**
     * 切换数据源
     *
     * @param name 数据源名称
     */
    public static void switchDataSource(String name) {
        if (!DATA_SOURCE_MAP.containsKey(name)) {
            throw new SimplifiedException("切换的数据源不存在:" + name);
        }
        currentDataSourceName.set(name);
    }

    /**
     * 切换到默认数据源
     */
    public static void usePrimaryDataSource() {
        switchDataSource(primaryDataSourceName);
    }

    /**
     * 获取当前数据源名称
     *
     * @return 数据源名称
     */
    public static String getCurrentDataSourceName() {
        if (Objects.isNull(currentDataSourceName.get())) {
            return primaryDataSourceName;
        }
        return currentDataSourceName.get();
    }

    /**
     * 注册SessionFactory，应该在使用任何Repository持久化方法之前完成所有SessionFactory的注册
     *
     * @param name           名称
     * @param sessionFactory 注册SessionFactory
     */
    public static void registSessionFactory(String name, SessionFactory sessionFactory) {
        SESSION_FACTORY_MAP.put(name, sessionFactory);
    }

    public static SessionFactory getCurrentSessionFactory() {
        return SESSION_FACTORY_MAP.get(getCurrentDataSourceName());
    }


    /**
     * 获得当前线程的session 如果Thread Local变量中有绑定，返回该session
     * 否则，调用sessFactory的getCurrentSession
     *
     * @return 打开的Session
     */
    public static Session getSession() {
        Session sess = peekThreadSession();
        if (sess == null) {
            sess = DataSourceContext.getCurrentSessionFactory().getCurrentSession();
        }
        if (null == sess) {
            logger.warn("当前线程没有绑定数据库会话，请检查是否开启了openSessionInView." +
                " 如果确认不开启，请使用openSession手动开启会话，并在恰当的时机手动关闭");
            throw new SimplifiedException("当前线程没有绑定数据库会话");
        }
        return sess;
    }

    /**
     * 打开一个新Session，如果线程上有其他Session，则返回最后一个Session
     *
     * @return 打开的Session
     */
    public static Session openSession() {
        return openSession(false);
    }

    /**
     * 在当前线程上手动打开一个Session，其他的Thread local事务可能会失效
     *
     * @param requiredNew 强制打开新Session
     * @return 打开的Session
     */
    public static Session openSession(boolean requiredNew) {
        Session session = peekThreadSession();
        if (requiredNew || session == null) {
            session = DataSourceContext.getCurrentSessionFactory().openSession();
            pushTreadSession(session);
        }
        return session;
    }

    /**
     * 关闭当前线程上手动开启的所有Session
     */
    public static void closeAllManualSession() {
        while (!THREADLOCAL_SESSIONS.get().isEmpty()) {
            closeManualSession();
        }
    }

    /**
     * 关闭当前线程上手动打开的最后一个Session，如果Session上有事务，提交之
     */
    public static void closeManualSession() {
        restoreSchema();
        Session session = popTreadSession();
        if (session != null && session.isOpen()) {
            Transaction tran = session.getTransaction();
            if (tran != null && tran.isActive()) {
                tran.commit();
                logger.info("commit before closeSession");
            }
            session.close();
        }
    }

    /**
     * 打开事务 如果线程已有事务就返回，不重复打开
     *
     * @return 打开的事务对象
     */
    public static Transaction openTransaction() {
        return openTransaction(false);
    }

    /**
     * 在当前线程上打开一个Session，并启动事务
     *
     * @param requiredNew 强制开启事务
     * @return 打开的事务对象
     */
    public static Transaction openTransaction(boolean requiredNew) {
        Session session = openSession(requiredNew);
        Transaction tran = session.getTransaction() == null ? session.beginTransaction() : session.getTransaction();
        tran.begin();
        return tran;
    }

    /**
     * 获取当前活动Session的默认Schema
     *
     * @return 默认schema名称
     */
    public static String getDefaultSchema() {
        return DEFAULT_SCHEMA_MAP.get(getCurrentDataSourceName());
    }

    /**
     * 获取当前活动Session的Schema
     *
     * @return 当前schema名称
     */
    public static String getCurrentSchema() {
        if (null == currentSchema.get() || null == currentSchema.get().get(getCurrentDataSourceName())) {
            return DEFAULT_SCHEMA_MAP.get(getCurrentDataSourceName());
        }
        return currentSchema.get().get(getCurrentDataSourceName());
    }

    /**
     * 恢复当前活动Session的默认Schema
     */
    public static void restoreSchema() {
        String currentDs = getCurrentDataSourceName();
        if (null == currentSchema.get() || null == currentSchema.get().get(currentDs)) {
            return;
        }
        switchSchema(DEFAULT_SCHEMA_MAP.get(currentDs));
    }

    /**
     * 设置当前活动Session的Schema
     *
     * @param schema schema名称
     */
    public static void switchSchema(String schema) {
        try {
            Session session = getSession();
            if (null == session) {
                return;
            }
            if (null == currentSchema.get()) {
                currentSchema.set(new HashMap<>());
            }
            if (schema.equals(currentSchema.get().get(getCurrentDataSourceName()))) {
                return;
            }
            session.flush();
            session.doWork(connection -> connection.setCatalog(schema));
            currentSchema.get().put(getCurrentDataSourceName(), schema);
        } catch (TransactionRequiredException ignore) {
            // do nothing
        } catch (Exception e) {
            throw new SimplifiedException("切换Schema失败", e);
        }
    }

    private static Session peekThreadSession() {
        Deque<Session> sessQueue = THREADLOCAL_SESSIONS.get();
        if (sessQueue != null && !sessQueue.isEmpty()) {
            return sessQueue.peek();
        }
        return null;
    }

    private static void pushTreadSession(Session session) {
        Deque<Session> sessQueue = THREADLOCAL_SESSIONS.get();
        if (sessQueue == null) {
            sessQueue = new ArrayDeque<>();
            THREADLOCAL_SESSIONS.set(sessQueue);
        }
        sessQueue.push(session);
    }

    private static Session popTreadSession() {
        Session session = null;
        Deque<Session> sessQueue = THREADLOCAL_SESSIONS.get();
        if (sessQueue != null && !sessQueue.isEmpty()) {
            session = sessQueue.pop();
        }
        if (sessQueue == null || sessQueue.isEmpty()) {
            THREADLOCAL_SESSIONS.remove();
            logger.info("remove THREADLOCAL_SESSIONS");
        }
        return session;
    }

    private static Connection extractConnection(Session session) {
        if (null == jdbcCoordinatorField) {
            extractJdbcCoordinatorField(session);
        }
        try {
            JdbcCoordinator jdbcCoordinator = (JdbcCoordinator) jdbcCoordinatorField.get(session);
            return jdbcCoordinator.getLogicalConnection().getPhysicalConnection();
        } catch (Exception e) {
            throw new SimplifiedException("无法访问Session中的连接");
        }
    }

    private static void extractJdbcCoordinatorField(Session session) {
        synchronized (jdbcCoordLock) {
            Class<?> cls;
            if (session instanceof SessionImpl) {
                cls = SessionImpl.class.getSuperclass().getSuperclass();
            } else if (session instanceof AbstractSessionImpl) {
                cls = AbstractSessionImpl.class.getSuperclass();
            } else if (session instanceof AbstractSharedSessionContract) {
                cls = AbstractSharedSessionContract.class;
            } else {
                throw new SimplifiedException("不支持的Session");
            }
            jdbcCoordinatorField = Assert.notNull(ReflectionUtils.findField(cls, "jdbcCoordinator"), "无法获取Session中的JdbcCoordinator");
            ReflectionUtils.makeAccessible(jdbcCoordinatorField);
        }
    }
}
