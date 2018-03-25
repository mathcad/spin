package org.spin.data.core;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.engine.jdbc.spi.JdbcCoordinator;
import org.hibernate.internal.AbstractSessionImpl;
import org.hibernate.internal.AbstractSharedSessionContract;
import org.hibernate.internal.SessionImpl;
import org.spin.core.Assert;
import org.spin.core.throwable.SimplifiedException;
import org.spin.core.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Schema上下文
 * <p>Created by xuweinan on 2018/3/23.</p>
 *
 * @author xuweinan
 */
public final class SchemaContext {
    private static final Map<Integer, String> DEFAULT_SCHEMAS = new HashMap<>();
    private static final SessionFactory[] SESSION_FACTORIES = new SessionFactory[20];
    private static int cur = 0;
    private static Field jdbcCoordinatorField = null;

    private static final Object lock = new Object();
    private static final Object jdbcCoordLock = new Object();

    private SchemaContext() {
    }

    /**
     * 恢复连接的默认Schema
     *
     * @param session jdbc会话
     */
    public static void restoreSchema(Session session) {
        int idx = lookupIndex(session);
        if (idx == -1) {
            init(session);
        } else {
            setSchemaToSession(session, DEFAULT_SCHEMAS.get(idx));
        }
    }

    /**
     * 设置连接的当前Schema
     *
     * @param session jdbc会话
     * @param schema  schema名称
     */
    public static void setSchema(Session session, String schema) {
        int idx = lookupIndex(session);
        if (idx == -1) {
            init(session);
        }
        setSchemaToSession(session, schema);
    }

    /**
     * 获取连接的当前Schema
     *
     * @param session jdbc会话
     * @return 当前schema名称
     */
    public static String getCurrentSchema(Session session) {
        return getSchemaFromSession(session);
    }

    /**
     * 获取连接的默认Schema
     *
     * @param session jdbc会话
     * @return 默认schema名称
     */
    public static String getDefaultSchema(Session session) {
        int idx = lookupIndex(session);
        if (idx == -1) {
            return init(session);
        }
        return DEFAULT_SCHEMAS.get(idx);
    }

    /**
     * 从session中获取实际的schema名称
     *
     * @param session jdbc会话
     * @return schema名称
     */
    private static String getSchemaFromSession(Session session) {
        try {
            return extractConnection(session).getCatalog();
        } catch (SQLException e) {
            throw new SimplifiedException("获取当前Schema失败", e);
        }
    }

    private static void setSchemaToSession(Session session, String schema) {
        try {
            extractConnection(session).setCatalog(schema);
        } catch (SQLException e) {
            throw new SimplifiedException("切换Schema失败", e);
        }
    }

    private static String init(Session session) {
        String schema;
        synchronized (lock) {
            SESSION_FACTORIES[cur] = session.getSessionFactory();
            schema = getSchemaFromSession(session);
            DEFAULT_SCHEMAS.put(cur++, schema);
        }
        return schema;
    }

    private static int lookupIndex(Session session) {
        int idx = -1;
        for (int i = 0; i < SESSION_FACTORIES.length; i++) {
            if (SESSION_FACTORIES[i] == session.getSessionFactory()) {
                idx = i;
            }
        }
        return idx;
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
