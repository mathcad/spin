package org.spin.data.rs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.Assert;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.*;

public class ColumnVisitor {
    private static final Logger logger = LoggerFactory.getLogger(ColumnVisitor.class);
    private static final BigInteger LONG_MIN = BigInteger.valueOf(Long.MIN_VALUE);
    private static final BigInteger LONG_MAX = BigInteger.valueOf(Long.MAX_VALUE);

    private final ResultSet resultSet;
    private final int columnCount;
    private final String[] columnNames;

    public ColumnVisitor(ResultSet resultSet) throws SQLException {
        this.resultSet = resultSet;
        ResultSetMetaData rsmd = resultSet.getMetaData();
        columnCount = rsmd.getColumnCount();
        columnNames = lookupColumnNames(rsmd);
    }

    /**
     * Retrieve a JDBC column value from a ResultSet, using the most appropriate
     * value type. The returned value should be a detached value object, not having
     * any ties to the active ResultSet: in particular, it should not be a Blob or
     * Clob object but rather a byte array or String representation, respectively.
     * <p>Uses the {@code getObject(index)} method, but includes additional "hacks"
     * to get around Oracle 10g returning a non-standard object for its TIMESTAMP
     * datatype and a {@code java.sql.Date} for DATE columns leaving out the
     * time portion: These columns will explicitly be extracted as standard
     * {@code java.sql.Timestamp} object.
     *
     * @param columnIndex is the column index
     * @return the value object
     * @throws SQLException if thrown by the JDBC API
     * @see java.sql.Blob
     * @see java.sql.Clob
     * @see java.sql.Timestamp
     */
    public Object getColumnValue(int columnIndex) throws SQLException {
        Object obj = resultSet.getObject(columnIndex);
        String className = null;
        if (obj != null) {
            className = obj.getClass().getName();
        }
        if (obj instanceof Blob) {
            Blob blob = (Blob) obj;
            obj = blob.getBytes(1, (int) blob.length());
        } else if (obj instanceof Clob) {
            Clob clob = (Clob) obj;
            obj = clob.getSubString(1, (int) clob.length());
        } else if ("oracle.sql.TIMESTAMP".equals(className) || "oracle.sql.TIMESTAMPTZ".equals(className)) {
            obj = resultSet.getTimestamp(columnIndex);
        } else if (className != null && className.startsWith("oracle.sql.DATE")) {
            String metaDataClassName = resultSet.getMetaData().getColumnClassName(columnIndex);
            if ("java.sql.Timestamp".equals(metaDataClassName) || "oracle.sql.TIMESTAMP".equals(metaDataClassName)) {
                obj = resultSet.getTimestamp(columnIndex);
            } else {
                obj = resultSet.getDate(columnIndex);
            }
        } else if (obj instanceof java.sql.Date) {
            if ("java.sql.Timestamp".equals(resultSet.getMetaData().getColumnClassName(columnIndex))) {
                obj = resultSet.getTimestamp(columnIndex);
            }
        }
        return obj;
    }

    public Object getColumnValue(int columnIndex, Class<?> requiredType) throws SQLException {
        if (requiredType == null) {
            return getColumnValue(columnIndex);
        }

        Object value;

        // Explicitly extract typed value, as far as possible.
        if (String.class == requiredType) {
            return resultSet.getString(columnIndex);
        } else if (boolean.class == requiredType || Boolean.class == requiredType) {
            value = resultSet.getBoolean(columnIndex);
        } else if (byte.class == requiredType || Byte.class == requiredType) {
            value = resultSet.getByte(columnIndex);
        } else if (short.class == requiredType || Short.class == requiredType) {
            value = resultSet.getShort(columnIndex);
        } else if (int.class == requiredType || Integer.class == requiredType) {
            value = resultSet.getInt(columnIndex);
        } else if (long.class == requiredType || Long.class == requiredType) {
            value = resultSet.getLong(columnIndex);
        } else if (float.class == requiredType || Float.class == requiredType) {
            value = resultSet.getFloat(columnIndex);
        } else if (double.class == requiredType || Double.class == requiredType ||
            Number.class == requiredType) {
            value = resultSet.getDouble(columnIndex);
        } else if (BigDecimal.class == requiredType) {
            return resultSet.getBigDecimal(columnIndex);
        } else if (java.sql.Date.class == requiredType) {
            return resultSet.getDate(columnIndex);
        } else if (java.sql.Time.class == requiredType) {
            return resultSet.getTime(columnIndex);
        } else if (java.sql.Timestamp.class == requiredType || java.util.Date.class == requiredType) {
            return resultSet.getTimestamp(columnIndex);
        } else if (byte[].class == requiredType) {
            return resultSet.getBytes(columnIndex);
        } else if (Blob.class == requiredType) {
            return resultSet.getBlob(columnIndex);
        } else if (Clob.class == requiredType) {
            return resultSet.getClob(columnIndex);
        } else if (requiredType.isEnum()) {
            // Enums can either be represented through a String or an enum index value:
            // leave enum type conversion up to the caller (e.g. a ConversionService)
            // but make sure that we return nothing other than a String or an Integer.
            Object obj = resultSet.getObject(columnIndex);
            if (obj instanceof String) {
                return obj;
            } else if (obj instanceof Number) {
                // Defensively convert any Number to an Integer (as needed by our
                // ConversionService's IntegerToEnumConverterFactory) for use as index
                return convertNumberToTargetClass((Number) obj, Integer.class);
            } else {
                // e.g. on Postgres: getObject returns a PGObject but we need a String
                return resultSet.getString(columnIndex);
            }
        } else {
            // Some unknown type desired -> rely on getObject.
            try {
                return resultSet.getObject(columnIndex, requiredType);
            } catch (AbstractMethodError err) {
                logger.debug("JDBC driver does not implement JDBC 4.1 'getObject(int, Class)' method", err);
            } catch (SQLFeatureNotSupportedException ex) {
                logger.debug("JDBC driver does not support JDBC 4.1 'getObject(int, Class)' method", ex);
            } catch (SQLException ex) {
                logger.debug("JDBC driver has limited support for JDBC 4.1 'getObject(int, Class)' method", ex);
            }

            // Corresponding SQL types for JSR-310 / Joda-Time types, left up
            // to the caller to convert them (e.g. through a ConversionService).
            String typeName = requiredType.getSimpleName();
            if ("LocalDate".equals(typeName)) {
                return resultSet.getDate(columnIndex);
            } else if ("LocalTime".equals(typeName)) {
                return resultSet.getTime(columnIndex);
            } else if ("LocalDateTime".equals(typeName)) {
                return resultSet.getTimestamp(columnIndex);
            }

            // Fall back to getObject without type specification, again
            // left up to the caller to convert the value if necessary.
            return getColumnValue(columnIndex);
        }

        // Perform was-null check if necessary (for results that the JDBC driver returns as primitives).
        return (resultSet.wasNull() ? null : value);
    }

    public int getColumnCount() {
        return columnCount;
    }

    public String[] getColumnNames() {
        return columnNames;
    }

    public String getColumnName(int columnIndex) {
        return columnNames[columnIndex];
    }

    /**
     * Determine the column name to use. The column name is determined based on a
     * lookup using ResultSetMetaData.
     * <p>This method implementation takes into account recent clarifications
     * expressed in the JDBC 4.0 specification:
     * <p><i>columnLabel - the label for the column specified with the SQL AS clause.
     * If the SQL AS clause was not specified, then the label is the name of the column</i>.
     *
     * @param resultSetMetaData the current meta data to use
     * @param columnIndex       the index of the column for the look up
     * @return the column name to use
     * @throws SQLException in case of lookup failure
     */
    public String lookupColumnName(ResultSetMetaData resultSetMetaData, int columnIndex) throws SQLException {
        String name = resultSetMetaData.getColumnLabel(columnIndex);
        if (name == null || name.length() < 1) {
            name = resultSetMetaData.getColumnName(columnIndex);
        }
        return name;
    }

    private String[] lookupColumnNames(ResultSetMetaData resultSetMetaData) throws SQLException {
        String[] names = new String[columnCount];
        for (int i = 1; i <= columnCount; i++) {
            String name = resultSetMetaData.getColumnLabel(i);
            if (name == null || name.length() < 1) {
                name = resultSetMetaData.getColumnName(i);
            }
            names[i - 1] = name;
        }
        return names;
    }

    private <T extends Number> T convertNumberToTargetClass(Number number, Class<T> targetClass)
        throws IllegalArgumentException {

        Assert.notNull(number, "Number must not be null");
        Assert.notNull(targetClass, "Target class must not be null");

        if (targetClass.isInstance(number)) {
            return (T) number;
        } else if (Byte.class == targetClass) {
            long value = checkedLongValue(number, targetClass);
            if (value < Byte.MIN_VALUE || value > Byte.MAX_VALUE) {
                raiseOverflowException(number, targetClass);
            }
            return (T) Byte.valueOf(number.byteValue());
        } else if (Short.class == targetClass) {
            long value = checkedLongValue(number, targetClass);
            if (value < Short.MIN_VALUE || value > Short.MAX_VALUE) {
                raiseOverflowException(number, targetClass);
            }
            return (T) Short.valueOf(number.shortValue());
        } else if (Integer.class == targetClass) {
            long value = checkedLongValue(number, targetClass);
            if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
                raiseOverflowException(number, targetClass);
            }
            return (T) Integer.valueOf(number.intValue());
        } else if (Long.class == targetClass) {
            long value = checkedLongValue(number, targetClass);
            return (T) Long.valueOf(value);
        } else if (BigInteger.class == targetClass) {
            if (number instanceof BigDecimal) {
                // do not lose precision - use BigDecimal's own conversion
                return (T) ((BigDecimal) number).toBigInteger();
            } else {
                // original value is not a Big* number - use standard long conversion
                return (T) BigInteger.valueOf(number.longValue());
            }
        } else if (Float.class == targetClass) {
            return (T) Float.valueOf(number.floatValue());
        } else if (Double.class == targetClass) {
            return (T) Double.valueOf(number.doubleValue());
        } else if (BigDecimal.class == targetClass) {
            // always use BigDecimal(String) here to avoid unpredictability of BigDecimal(double)
            // (see BigDecimal javadoc for details)
            return (T) new BigDecimal(number.toString());
        } else {
            throw new IllegalArgumentException("Could not convert number [" + number + "] of type [" +
                number.getClass().getName() + "] to unsupported target class [" + targetClass.getName() + "]");
        }
    }

    private long checkedLongValue(Number number, Class<? extends Number> targetClass) {
        BigInteger bigInt = null;
        if (number instanceof BigInteger) {
            bigInt = (BigInteger) number;
        } else if (number instanceof BigDecimal) {
            bigInt = ((BigDecimal) number).toBigInteger();
        }
        // Effectively analogous to JDK 8's BigInteger.longValueExact()
        if (bigInt != null && (bigInt.compareTo(LONG_MIN) < 0 || bigInt.compareTo(LONG_MAX) > 0)) {
            raiseOverflowException(number, targetClass);
        }
        return number.longValue();
    }

    private void raiseOverflowException(Number number, Class<?> targetClass) {
        throw new IllegalArgumentException("Could not convert number [" + number + "] of type [" +
            number.getClass().getName() + "] to target class [" + targetClass.getName() + "]: overflow");
    }
}
