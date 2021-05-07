package org.spin.datasource.tx;

import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author funkye
 */
public class ConnectionFactory {

    private static final ThreadLocal<Map<String, ConnectionProxy>> CONNECTION_HOLDER =
        ThreadLocal.withInitial(ConcurrentHashMap::new);

    public static void putConnection(String ds, ConnectionProxy connection) {
        Map<String, ConnectionProxy> concurrentHashMap = CONNECTION_HOLDER.get();
        if (!concurrentHashMap.containsKey(ds)) {
            try {
                connection.setAutoCommit(false);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            concurrentHashMap.put(ds, connection);
        }
    }

    public static ConnectionProxy getConnection(String ds) {
        return CONNECTION_HOLDER.get().get(ds);
    }

    public static void notify(Boolean state) {
        try {
            Map<String, ConnectionProxy> concurrentHashMap = CONNECTION_HOLDER.get();
            for (ConnectionProxy connectionProxy : concurrentHashMap.values()) {
                connectionProxy.notify(state);
            }
        } finally {
            CONNECTION_HOLDER.remove();
        }
    }

}
