package org.spin.data.sql.dbtype;

import org.spin.data.core.DatabaseType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据库类型
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2021/4/7</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class DbTypes {
    public static final Map<String, DatabaseType> DB_TYPES = new ConcurrentHashMap<>();

    static {
        register(new MySQLDatabaseType());
        register(new OracleDatabaseType());
        register(new PostgreSQLDatabaseType());
        register(new SQLiteDatabaseType());
        register(new SQLServerDatabaseType());
    }

    public static void register(DatabaseType databaseType) {
        DB_TYPES.put(databaseType.getProductName(), databaseType);
    }

    public static DatabaseType get(String productName) {
        return DB_TYPES.get(productName);
    }
}
