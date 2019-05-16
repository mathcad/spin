package org.spin.data.sql;

import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2018/12/1</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public enum JdbcType {
    BIT(Types.BIT),
    TINYINT(Types.TINYINT),
    SMALLINT(Types.SMALLINT),
    INTEGER(Types.INTEGER),
    BIGINT(Types.BIGINT),
    FLOAT(Types.FLOAT),
    REAL(Types.REAL),
    DOUBLE(Types.DOUBLE),
    NUMERIC(Types.NUMERIC),
    DECIMAL(Types.DECIMAL),
    CHAR(Types.CHAR),
    VARCHAR(Types.VARCHAR),
    LONGVARCHAR(Types.LONGVARCHAR),
    DATE(Types.DATE),
    TIME(Types.TIME),
    TIMESTAMP(Types.TIMESTAMP),
    BINARY(Types.BINARY),
    VARBINARY(Types.VARBINARY),
    LONGVARBINARY(Types.LONGVARBINARY),
    NULL(Types.NULL),
    OTHER(Types.OTHER),
    JAVA_OBJECT(Types.JAVA_OBJECT),
    DISTINCT(Types.DISTINCT),
    STRUCT(Types.STRUCT),
    ARRAY(Types.ARRAY),
    BLOB(Types.BLOB),
    CLOB(Types.CLOB),
    REF(Types.REF),
    DATALINK(Types.DATALINK),
    BOOLEAN(Types.BOOLEAN),
    ROWID(Types.ROWID), // JDK6
    NCHAR(Types.NCHAR), // JDK6
    NVARCHAR(Types.NVARCHAR), // JDK6
    LONGNVARCHAR(Types.LONGNVARCHAR), // JDK6
    NCLOB(Types.NCLOB), // JDK6
    SQLXML(Types.SQLXML), // JDK6
    REF_CURSOR(Types.REF_CURSOR), // JDK8
    TIME_WITH_TIMEZONE(Types.TIME_WITH_TIMEZONE), // JDK8
    TIMESTAMP_WITH_TIMEZONE(Types.TIMESTAMP_WITH_TIMEZONE), // JDK8
    DATETIMEOFFSET(-155), // SQL Server 2008
    UNKNOWN(-100000);

    public final int code;
    private static Map<Integer, JdbcType> codeLookup = new HashMap<>();

    static {
        for (JdbcType type : JdbcType.values()) {
            codeLookup.put(type.code, type);
        }
    }

    JdbcType(int code) {
        this.code = code;
    }

    public static JdbcType forCode(int code) {
        return codeLookup.get(code);
    }
}
