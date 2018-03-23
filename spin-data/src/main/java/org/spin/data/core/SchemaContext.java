package org.spin.data.core;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.spin.core.util.ClassUtils;

import java.util.HashMap;
import java.util.Map;

public class SchemaContext {
    private static final Map<Integer, String> DEFAULT_SCHEMAS = new HashMap<>();
    private static final SessionFactory[] SESSION_FACTORIES = new SessionFactory[20];
    private static int cur = 0;

    private static final Object lock = new Object();

    public static void restoreSchema(Session session) {
        int idx = lookupIndex(session);
        if (idx == -1) {
            init(session);
        } else {
            setSchemaToSession(session, DEFAULT_SCHEMAS.get(idx));
        }
    }

    public static void setSchema(Session session, String schema) {
        int idx = lookupIndex(session);
        if (idx == -1) {
            init(session);
        }
        setSchemaToSession(session, schema);
    }

    public static String getCurrentSchema(Session session) {
        return getSchemaFromSession(session);
    }

    public static String getDefaultSchema(Session session) {
        int idx = lookupIndex(session);
        if (idx == -1) {
            return init(session);
        }
        return DEFAULT_SCHEMAS.get(idx);
    }

    private static String getSchemaFromSession(Session session) {
        // TODO:
        ClassUtils.getFieldValue(session, "");
        return null;
    }

    private static void setSchemaToSession(Session session, String schema) {
        // TODO:
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
}
