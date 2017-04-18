package org.spin.jpa.extend;

import org.hibernate.AssertionFailure;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.internal.util.ReflectHelper;
import org.hibernate.internal.util.config.ConfigurationHelper;
import org.hibernate.usertype.DynamicParameterizedType;
import org.hibernate.usertype.EnhancedUserType;
import org.hibernate.usertype.LoggableUserType;
import org.spin.throwable.SimplifiedException;
import org.spin.util.ClassUtils;
import org.spin.util.EnumUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Enumerated;
import javax.persistence.MapKeyEnumerated;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Properties;

/**
 * 枚举映射
 * <p>Created by xuweinan on 2016/9/24.</p>
 *
 * @author xuweinan
 * @version V1.0
 */
public class UserEnumType implements EnhancedUserType, DynamicParameterizedType, LoggableUserType, Serializable {
    private static final long serialVersionUID = 228011828908024965L;
    private static final Logger logger = LoggerFactory.getLogger(UserEnumType.class);

    public static final String ENUM = "enumClass";
    public static final String NAMED = "useNamed";
    public static final String TYPE = "type";

    private Class<? extends Enum> enumClass;
    private EnumValueMapper enumValueMapper;
    private int sqlType = Types.INTEGER;

    @Override
    public int[] sqlTypes() {
        return new int[]{sqlType};
    }

    @Override
    public Class returnedClass() {
        return enumClass;
    }

    @Override
    public boolean equals(Object x, Object y) {
        return x == y;
    }

    @Override
    public int hashCode(Object x) {
        return x == null ? 0 : x.hashCode();
    }

    @Override
    public Object nullSafeGet(ResultSet rs, String[] names, SharedSessionContractImplementor session, Object owner) throws SQLException {
        if (enumValueMapper == null) {
            resolveEnumValueMapper(rs, names[0]);
        }
        return enumValueMapper.getValue(rs, names);
    }

    private void resolveEnumValueMapper(ResultSet rs, String name) {
        if (enumValueMapper == null) {
            try {
                resolveEnumValueMapper(rs.getMetaData().getColumnType(rs.findColumn(name)));
            } catch (Exception e) {
                logger.debug("JDBC driver threw exception calling java.sql.ResultSetMetaData.getColumnType; "
                    + "using fallback determination [%s] : %s", enumClass.getName(), e.getMessage());
                try {
                    Object value = rs.getObject(name);
                    if (Number.class.isInstance(value)) {
                        treatAsOrdinal();
                    } else {
                        throw new SimplifiedException("不支持字符型值枚举列");
                    }
                } catch (SQLException ignore) {
                    treatAsOrdinal();
                }
            }
        }
    }

    private void resolveEnumValueMapper(int columnType) {
        if (isOrdinal(columnType)) {
            treatAsOrdinal();
        } else {
            throw new SimplifiedException("不支持非数值枚举列");
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void nullSafeSet(PreparedStatement st, Object value, int index, SharedSessionContractImplementor session) throws SQLException {
        if (enumValueMapper == null) {
            resolveEnumValueMapper(st, index);
        }
        enumValueMapper.setValue(st, (Enum) value, index);
    }

    private void resolveEnumValueMapper(PreparedStatement st, int index) {
        if (enumValueMapper == null) {
            try {
                resolveEnumValueMapper(st.getParameterMetaData().getParameterType(index));
            } catch (Exception e) {
                logger.debug(
                    "JDBC driver threw exception calling java.sql.ParameterMetaData#getParameterType; "
                        + "falling back to ordinal-based enum mapping [%s] : %s",
                    enumClass.getName(), e.getMessage());
                treatAsOrdinal();
            }
        }
    }

    @Override
    public Object deepCopy(Object value) {
        return value;
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public Serializable disassemble(Object value) {
        return (Serializable) value;
    }

    @Override
    public Object assemble(Serializable cached, Object owner) {
        return cached;
    }

    @Override
    public Object replace(Object original, Object target, Object owner) {
        return original;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setParameterValues(Properties parameters) {
        final ParameterType reader = (ParameterType) parameters.get(PARAMETER_TYPE);
        if (reader != null) {
            enumClass = reader.getReturnedClass().asSubclass(Enum.class);

            final boolean isOrdinal;
            final javax.persistence.EnumType enumType = getEnumType(reader);
            if (enumType == null || javax.persistence.EnumType.ORDINAL.equals(enumType)) {
                isOrdinal = true;
            } else if (javax.persistence.EnumType.STRING.equals(enumType)) {
                isOrdinal = false;
            } else {
                throw new AssertionFailure("Unknown EnumType: " + enumType);
            }

            if (isOrdinal) {
                treatAsOrdinal();
            } else {
                throw new SimplifiedException("不支持字符型值枚举列");
            }
            sqlType = enumValueMapper.getSqlType();
        } else {
            String enumClassName = (String) parameters.get(ENUM);
            try {
                enumClass = ReflectHelper.classForName(enumClassName, this.getClass()).asSubclass(Enum.class);
            } catch (ClassNotFoundException exception) {
                throw new HibernateException("Enum class not found", exception);
            }

            final Object useNamedSetting = parameters.get(NAMED);
            if (useNamedSetting != null) {
                final boolean useNamed = ConfigurationHelper.getBoolean(NAMED, parameters);
                if (useNamed) {
                    throw new SimplifiedException("不支持字符型值枚举列");
                } else {
                    treatAsOrdinal();
                }
                sqlType = enumValueMapper.getSqlType();
            }
        }

        final String type = (String) parameters.get(TYPE);
        if (type != null) {
            sqlType = Integer.decode(type);
        }
    }

    private void treatAsOrdinal() {
        if (this.enumValueMapper == null || !OrdinalEnumValueMapper.class.isInstance(this.enumValueMapper)) {
            this.enumValueMapper = new ValueEnumValueMapper();
            this.sqlType = this.enumValueMapper.getSqlType();
        }
    }

    private javax.persistence.EnumType getEnumType(ParameterType reader) {
        javax.persistence.EnumType enumType = null;
        if (reader.isPrimaryKey()) {
            MapKeyEnumerated enumAnn = getAnnotation(reader.getAnnotationsMethod(), MapKeyEnumerated.class);
            if (enumAnn != null) {
                enumType = enumAnn.value();
            }
        } else {
            Enumerated enumAnn = getAnnotation(reader.getAnnotationsMethod(), Enumerated.class);
            if (enumAnn != null) {
                enumType = enumAnn.value();
            }
        }
        return enumType;
    }

    @SuppressWarnings("unchecked")
    private <T extends Annotation> T getAnnotation(Annotation[] annotations, Class<T> anClass) {
        for (Annotation annotation : annotations) {
            if (anClass.isInstance(annotation)) {
                return (T) annotation;
            }
        }
        return null;
    }

    @Override
    public String objectToSQLString(Object value) {
        return enumValueMapper.objectToSQLString((Enum) value);
    }

    @Override
    @Deprecated
    public String toXMLString(Object value) {
        return enumValueMapper.toXMLString((Enum) value);
    }

    @Override
    @Deprecated
    public Object fromXMLString(String xmlValue) {
        return enumValueMapper.fromXMLString(xmlValue);
    }

    @Override
    public String toLoggableString(Object value, SessionFactoryImplementor factory) {
        if (enumValueMapper != null) {
            return enumValueMapper.toXMLString((Enum) value);
        }
        return value.toString();
    }

    public boolean isOrdinal() {
        return isOrdinal(sqlType);
    }

    private boolean isOrdinal(int paramType) {
        switch (paramType) {
            case Types.INTEGER:
            case Types.NUMERIC:
            case Types.SMALLINT:
            case Types.TINYINT:
            case Types.BIGINT:
            case Types.DECIMAL: // for Oracle Driver
            case Types.DOUBLE: // for Oracle Driver
            case Types.FLOAT:
                return true;
            case Types.CHAR:
            case Types.LONGVARCHAR:
            case Types.VARCHAR:
                return false;
            default:
                throw new HibernateException("Unable to persist an Enum in a column of SQL Type: " + paramType);
        }
    }

    private class OrdinalEnumValueMapper extends EnumValueMapperSupport implements EnumValueMapper, Serializable {
        private static final long serialVersionUID = 3425742296198497687L;
        private transient Enum[] enumsByOrdinal;

        @Override
        public int getSqlType() {
            return Types.INTEGER;
        }

        @Override
        public Enum getValue(ResultSet rs, String[] names) throws SQLException {
            final int ordinal = rs.getInt(names[0]);
            final boolean traceEnabled = logger.isTraceEnabled();
            if (rs.wasNull()) {
                if (traceEnabled) {
                    logger.trace(String.format("Returning null as column [%s]", names[0]));
                }
                return null;
            }

            final Enum enumValue = fromOrdinal(ordinal);
            if (traceEnabled) {
                logger.trace(String.format("Returning [%s] as column [%s]", enumValue, names[0]));
            }
            return enumValue;
        }

        private Enum fromOrdinal(int ordinal) {
            final Enum[] enumsByOrdinal = enumsByOrdinal();
            if (ordinal < 0 || ordinal >= enumsByOrdinal.length) {
                throw new IllegalArgumentException(
                    String.format(
                        "Unknown ordinal value [%s] for enum class [%s]",
                        ordinal,
                        enumClass.getName()
                    )
                );
            }
            return enumsByOrdinal[ordinal];

        }

        private Enum[] enumsByOrdinal() {
            if (enumsByOrdinal == null) {
                enumsByOrdinal = enumClass.getEnumConstants();
                if (enumsByOrdinal == null) {
                    throw new HibernateException("Failed to init enum values");
                }
            }
            return enumsByOrdinal;
        }

        @Override
        public String objectToSQLString(Enum value) {
            return toXMLString(value);
        }

        @Override
        public String toXMLString(Enum value) {
            return Integer.toString(value.ordinal());
        }

        @Override
        public Enum fromXMLString(String xml) {
            return fromOrdinal(Integer.parseInt(xml));
        }

        @Override
        protected Object extractJdbcValue(Enum value) {
            return value.ordinal();
        }
    }

    private class ValueEnumValueMapper extends EnumValueMapperSupport {
        private static final long serialVersionUID = -1329754149883071778L;

        @Override
        public int getSqlType() {
            return Types.INTEGER;
        }

        @Override
        public Enum getValue(ResultSet rs, String[] names) throws SQLException {
            final int value = rs.getInt(names[0]);
            if (rs.wasNull()) {
                if (logger.isDebugEnabled()) {
                    logger.debug(String.format("Returning null as column [%s]", names[0]));
                }
                return null;
            }
            @SuppressWarnings("unchecked")
            Enum enumValue = EnumUtils.getEnum(enumClass, value);
            logger.trace(String.format("Returning [%s] as column [%s]", enumValue, names[0]));
            return enumValue;
        }

        @Override
        public String objectToSQLString(Enum value) {
            return toXMLString(value);
        }

        @Override
        public String toXMLString(Enum value) {
            return ClassUtils.getFieldValue(value, "value").get("value").toString();
        }

        @Override
        public Enum fromXMLString(String xml) {
            //noinspection unchecked
            return EnumUtils.getEnum(enumClass, Integer.parseInt(xml));
        }

        @Override
        protected Object extractJdbcValue(Enum value) {
            return ClassUtils.getFieldValue(value, "value").get("value");
        }
    }

    private interface EnumValueMapper extends Serializable {
        int getSqlType();

        Enum getValue(ResultSet rs, String[] names) throws SQLException;

        void setValue(PreparedStatement st, Enum value, int index) throws SQLException;

        String objectToSQLString(Enum value);

        String toXMLString(Enum value);

        Enum fromXMLString(String xml);
    }

    public abstract class EnumValueMapperSupport implements EnumValueMapper {
        private static final long serialVersionUID = 1929721731025741707L;

        protected abstract Object extractJdbcValue(Enum value);

        @Override
        public void setValue(PreparedStatement st, Enum value, int index) throws SQLException {
            final Object jdbcValue = value == null ? null : extractJdbcValue(value);

            final boolean traceEnabled = logger.isTraceEnabled();
            if (jdbcValue == null) {
                if (traceEnabled) {
                    logger.trace(String.format("Binding null to parameter: [%s]", index));
                }
                st.setNull(index, getSqlType());
                return;
            }

            if (traceEnabled) {
                logger.trace(String.format("Binding [%s] to parameter: [%s]", jdbcValue, index));
            }
            st.setObject(index, jdbcValue, UserEnumType.this.sqlType);
        }
    }
}
