package org.spin.data.sql.param;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.util.DateUtils;
import org.spin.core.util.StringUtils;

import java.io.StringReader;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public abstract class JdbcUtils {
    private static final Logger logger = LoggerFactory.getLogger(JdbcUtils.class);

    public static final int TYPE_UNKNOWN = -100;

    private JdbcUtils() {
    }


    /**
     * Return whether the given JDBC driver supports JDBC 2.0 batch updates.
     * <p>Typically invoked right before execution of a given set of statements:
     * to decide whether the set of SQL statements should be executed through
     * the JDBC 2.0 batch mechanism or simply in a traditional one-by-one fashion.
     * <p>Logs a warning if the "supportsBatchUpdates" methods throws an exception
     * and simply returns {@code false} in that case.
     *
     * @param con the Connection to check
     * @return whether JDBC 2.0 batch updates are supported
     * @see java.sql.DatabaseMetaData#supportsBatchUpdates()
     */
    public static boolean supportsBatchUpdates(Connection con) {
        try {
            DatabaseMetaData dbmd = con.getMetaData();
            if (dbmd != null) {
                if (dbmd.supportsBatchUpdates()) {
                    logger.debug("JDBC driver supports batch updates");
                    return true;
                } else {
                    logger.debug("JDBC driver does not support batch updates");
                }
            }
        } catch (SQLException ex) {
            logger.debug("JDBC driver 'supportsBatchUpdates' method threw exception", ex);
        }
        return false;
    }

    public static void setParameterValues(PreparedStatement ps, List<SqlParameter> parameters, Map<String, ?> model) throws SQLException {
        for (int i = 0; i < parameters.size(); i++) {
            setParameterValue(ps, i, model.get(parameters.get(i).getParameterName()));
        }
    }

    public static void setParameterValue(PreparedStatement ps, int paramIndex, Object inValue) throws SQLException {
        String typeNameToUse = null;
        int sqlTypeToUse = -100;

        // override type info?
        if (inValue instanceof SqlParameter) {
            SqlParameter parameterValue = (SqlParameter) inValue;
            if (logger.isDebugEnabled()) {
                logger.debug("Overriding type info with runtime info from SqlParameterValue: column index " + paramIndex +
                    ", SQL type " + parameterValue.getSqlType() + ", type name " + parameterValue.getTypeName());
            }
            if (parameterValue.getSqlType() != TYPE_UNKNOWN) {
                sqlTypeToUse = parameterValue.getSqlType();
            }
            if (parameterValue.getTypeName() != null) {
                typeNameToUse = parameterValue.getTypeName();
            }
        }

        if (logger.isTraceEnabled()) {
            logger.trace("Setting SQL statement parameter value: column index " + paramIndex +
                ", parameter value [" + inValue +
                "], value class [" + (inValue != null ? inValue.getClass().getName() : "null") +
                "], SQL type " + (sqlTypeToUse == TYPE_UNKNOWN ? "unknown" : Integer.toString(sqlTypeToUse)));
        }

        if (inValue == null) {
            setNull(ps, paramIndex, sqlTypeToUse, typeNameToUse);
        } else {
            setValue(ps, paramIndex, sqlTypeToUse, typeNameToUse, null, inValue);
        }
    }

    private static void setNull(PreparedStatement ps, int paramIndex, int sqlType, String typeName)
        throws SQLException {

        if (sqlType == TYPE_UNKNOWN || sqlType == Types.OTHER) {
            boolean useSetObject = false;
            Integer sqlTypeToUse = null;
            try {
                sqlTypeToUse = ps.getParameterMetaData().getParameterType(paramIndex);
            } catch (SQLException ex) {
                if (logger.isDebugEnabled()) {
                    logger.debug("JDBC getParameterType call failed - using fallback method instead: " + ex);
                }
            }
            if (sqlTypeToUse == null) {
                // Proceed with database-specific checks
                sqlTypeToUse = Types.NULL;
                DatabaseMetaData dbmd = ps.getConnection().getMetaData();
                String jdbcDriverName = dbmd.getDriverName();
                String databaseProductName = dbmd.getDatabaseProductName();
                if (databaseProductName.startsWith("Informix") ||
                    (jdbcDriverName.startsWith("Microsoft") && jdbcDriverName.contains("SQL Server"))) {
                    // "Microsoft SQL Server JDBC Driver 3.0" versus "Microsoft JDBC Driver 4.0 for SQL Server"
                    useSetObject = true;
                } else if (databaseProductName.startsWith("DB2") ||
                    jdbcDriverName.startsWith("jConnect") ||
                    jdbcDriverName.startsWith("SQLServer") ||
                    jdbcDriverName.startsWith("Apache Derby")) {
                    sqlTypeToUse = Types.VARCHAR;
                }
            }
            if (useSetObject) {
                ps.setObject(paramIndex, null);
            } else {
                ps.setNull(paramIndex, sqlTypeToUse);
            }
        } else if (typeName != null) {
            ps.setNull(paramIndex, sqlType, typeName);
        } else {
            ps.setNull(paramIndex, sqlType);
        }
    }

    private static void setValue(PreparedStatement ps, int paramIndex, int sqlType, String typeName, Integer scale, Object inValue) throws SQLException {
        switch (sqlType) {
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
                ps.setString(paramIndex, inValue.toString());
                break;
            case Types.NVARCHAR:
            case Types.LONGNVARCHAR:
                ps.setString(paramIndex, inValue.toString());
                break;
            case Types.CLOB:
            case Types.NCLOB:
                if (StringUtils.isString(inValue)) {
                    String strVal = inValue.toString();
                    if (strVal.length() > 4000) {
                        // Necessary for older Oracle drivers, in particular when running against an Oracle 10 database.
                        // Should also work fine against other drivers/databases since it uses standard JDBC 4.0 API.
                        if (sqlType == Types.NCLOB) {
                            ps.setNClob(paramIndex, new StringReader(strVal), strVal.length());
                        } else {
                            ps.setClob(paramIndex, new StringReader(strVal), strVal.length());
                        }
                    } else {
                        // Fallback: setString or setNString binding
                        if (sqlType == Types.NCLOB) {
                            ps.setNString(paramIndex, strVal);
                        } else {
                            ps.setString(paramIndex, strVal);
                        }
                    }
                }
                break;
            case Types.DECIMAL:
            case Types.NUMERIC:
                if (inValue instanceof BigDecimal) {
                    ps.setBigDecimal(paramIndex, (BigDecimal) inValue);
                } else if (inValue instanceof Integer) {
                    ps.setInt(paramIndex, (int) inValue);
                } else if (inValue instanceof Long) {
                    ps.setLong(paramIndex, (long) inValue);
                } else if (inValue instanceof Float) {
                    ps.setFloat(paramIndex, (float) inValue);
                } else if (inValue instanceof Double) {
                    ps.setDouble(paramIndex, (double) inValue);
                } else if (inValue instanceof Byte) {
                    ps.setByte(paramIndex, (byte) inValue);
                } else if (StringUtils.isString(inValue)) {
                    ps.setBigDecimal(paramIndex, new BigDecimal(inValue.toString()));
                } else if (scale != null) {
                    ps.setObject(paramIndex, inValue, sqlType, scale);
                } else {
                    ps.setObject(paramIndex, inValue, sqlType);
                }
                break;
            case Types.BOOLEAN:
                if (inValue instanceof Boolean) {
                    ps.setBoolean(paramIndex, (Boolean) inValue);
                } else if (StringUtils.isString(inValue)) {
                    ps.setBoolean(paramIndex, Boolean.parseBoolean(inValue.toString()));
                } else {
                    ps.setObject(paramIndex, inValue, Types.BOOLEAN);
                }
                break;
            case Types.DATE:
                if (inValue instanceof java.util.Date) {
                    if (inValue instanceof java.sql.Date) {
                        ps.setDate(paramIndex, (java.sql.Date) inValue);
                    } else {
                        ps.setDate(paramIndex, new java.sql.Date(((java.util.Date) inValue).getTime()));
                    }
                } else if (StringUtils.isString(inValue)) {
                    ps.setDate(paramIndex, new java.sql.Date(DateUtils.toDate(inValue.toString()).getTime()));
                } else if (inValue instanceof LocalDate) {
                    java.sql.Date date = new java.sql.Date(((LocalDate) inValue).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli());
                    ps.setDate(paramIndex, date);
                } else if (inValue instanceof LocalDateTime) {
                    java.sql.Date date = new java.sql.Date(((LocalDateTime) inValue).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
                    ps.setDate(paramIndex, date);
                } else if (inValue instanceof Calendar) {
                    Calendar cal = (Calendar) inValue;
                    ps.setDate(paramIndex, new java.sql.Date(cal.getTime().getTime()), cal);
                } else {
                    ps.setObject(paramIndex, inValue, Types.DATE);
                }
                break;
            case Types.TIME:
                if (inValue instanceof java.util.Date) {
                    if (inValue instanceof java.sql.Time) {
                        ps.setTime(paramIndex, (java.sql.Time) inValue);
                    } else {
                        ps.setTime(paramIndex, new java.sql.Time(((java.util.Date) inValue).getTime()));
                    }
                } else if (StringUtils.isString(inValue)) {
                    ps.setTime(paramIndex, new java.sql.Time(DateUtils.toDate(inValue.toString()).getTime()));
                } else if (inValue instanceof LocalTime) {
                    java.sql.Time date = new java.sql.Time(((LocalTime) inValue).atDate(LocalDate.now()).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
                    ps.setTime(paramIndex, date);
                } else if (inValue instanceof LocalDateTime) {
                    java.sql.Time date = new java.sql.Time(((LocalDateTime) inValue).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
                    ps.setTime(paramIndex, date);
                } else if (inValue instanceof Calendar) {
                    Calendar cal = (Calendar) inValue;
                    ps.setTime(paramIndex, new java.sql.Time(cal.getTime().getTime()), cal);
                } else {
                    ps.setObject(paramIndex, inValue, Types.TIME);
                }
                break;
            case Types.TIMESTAMP:
                if (inValue instanceof java.util.Date) {
                    if (inValue instanceof java.sql.Timestamp) {
                        ps.setTimestamp(paramIndex, (java.sql.Timestamp) inValue);
                    } else {
                        ps.setTimestamp(paramIndex, new java.sql.Timestamp(((java.util.Date) inValue).getTime()));
                    }
                } else if (StringUtils.isString(inValue)) {
                    ps.setTimestamp(paramIndex, new java.sql.Timestamp(DateUtils.toDate(inValue.toString()).getTime()));
                } else if (inValue instanceof LocalDateTime) {
                    java.sql.Timestamp date = new java.sql.Timestamp(((LocalDateTime) inValue).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
                    ps.setTimestamp(paramIndex, date);
                } else if (inValue instanceof LocalDate) {
                    java.sql.Timestamp date = new java.sql.Timestamp(((LocalDate) inValue).atTime(LocalTime.MIN).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
                    ps.setTimestamp(paramIndex, date);
                } else if (inValue instanceof Calendar) {
                    Calendar cal = (Calendar) inValue;
                    ps.setTimestamp(paramIndex, new java.sql.Timestamp(cal.getTime().getTime()), cal);
                } else {
                    ps.setObject(paramIndex, inValue, Types.TIMESTAMP);
                }
                break;
            default:
                if (sqlType == TYPE_UNKNOWN || (sqlType == Types.OTHER && "Oracle".equals(ps.getConnection().getMetaData().getDatabaseProductName()))) {
                    if (StringUtils.isString(inValue)) {
                        ps.setString(paramIndex, inValue.toString());
                    } else if (inValue instanceof Boolean) {
                        ps.setBoolean(paramIndex, (Boolean) inValue);
                    } else if (inValue instanceof Integer) {
                        ps.setInt(paramIndex, (int) inValue);
                    } else if (inValue instanceof Long) {
                        ps.setLong(paramIndex, (long) inValue);
                    } else if (inValue instanceof Float) {
                        ps.setFloat(paramIndex, (float) inValue);
                    } else if (inValue instanceof Double) {
                        ps.setDouble(paramIndex, (double) inValue);
                    } else if (inValue instanceof Byte) {
                        ps.setByte(paramIndex, (byte) inValue);
                    } else if (inValue instanceof BigDecimal) {
                        ps.setBigDecimal(paramIndex, (BigDecimal) inValue);
                    } else if (isDateValue(inValue.getClass())) {
                        ps.setTimestamp(paramIndex, new java.sql.Timestamp(((java.util.Date) inValue).getTime()));
                    } else if (inValue instanceof LocalDate) {
                        java.sql.Date date = new java.sql.Date(((LocalDate) inValue).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli());
                        ps.setDate(paramIndex, date);
                    } else if (inValue instanceof LocalDateTime) {
                        java.sql.Timestamp date = new java.sql.Timestamp(((LocalDateTime) inValue).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
                        ps.setTimestamp(paramIndex, date);
                    } else if (inValue instanceof LocalTime) {
                        java.sql.Time date = new java.sql.Time(((LocalTime) inValue).atDate(LocalDate.now()).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
                        ps.setTime(paramIndex, date);
                    } else if (inValue instanceof java.sql.Date) {
                        ps.setDate(paramIndex, (java.sql.Date) inValue);
                    } else if (inValue instanceof java.sql.Timestamp) {
                        ps.setTimestamp(paramIndex, (java.sql.Timestamp) inValue);
                    } else if (inValue instanceof java.sql.Time) {
                        ps.setTime(paramIndex, (java.sql.Time) inValue);
                    } else if (inValue instanceof Calendar) {
                        Calendar cal = (Calendar) inValue;
                        ps.setTimestamp(paramIndex, new java.sql.Timestamp(cal.getTime().getTime()), cal);
                    } else {
                        // Fall back to generic setObject call without SQL type specified.
                        ps.setObject(paramIndex, inValue);
                    }
                } else {
                    // Fall back to generic setObject call with SQL type specified.
                    ps.setObject(paramIndex, inValue, sqlType);
                }
        }
    }

    private static boolean isDateValue(Class<?> inValueType) {
        return (java.util.Date.class.isAssignableFrom(inValueType) &&
            !(java.sql.Date.class.isAssignableFrom(inValueType) ||
                java.sql.Time.class.isAssignableFrom(inValueType) ||
                java.sql.Timestamp.class.isAssignableFrom(inValueType)));
    }
}
