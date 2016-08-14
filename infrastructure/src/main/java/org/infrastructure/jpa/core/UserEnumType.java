package org.infrastructure.jpa.core;

import org.hibernate.AssertionFailure;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.internal.util.ReflectHelper;
import org.hibernate.internal.util.config.ConfigurationHelper;
import org.hibernate.usertype.DynamicParameterizedType;
import org.hibernate.usertype.EnhancedUserType;
import org.hibernate.usertype.LoggableUserType;
import org.infrastructure.sys.EnumUtils;
import org.infrastructure.util.ClassUtils;
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
 * 用户枚举类型(专用于 包含 int类型value字段的Enum) 类型标题
 *
 * @version V1.0
 */
public class UserEnumType<E extends Enum<E>> implements EnhancedUserType, DynamicParameterizedType, LoggableUserType, Serializable {
    private static final long serialVersionUID = 228011828908024965L;
    private static final Logger LOGGER = LoggerFactory.getLogger(UserEnumType.class);

    public static final String ENUM = "enumClass";
    public static final String NAMED = "useNamed";
    public static final String TYPE = "type";

    private Class<E> enumClass;
    private EnumValueMapper<E> enumValueMapper;
    private int sqlType = Types.INTEGER;

    @Override
    public int[] sqlTypes() {
        return new int[]{sqlType};
    }

    @Override
    public Class<E> returnedClass() {
        return enumClass;
    }

    @Override
    public boolean equals(Object x, Object y) throws HibernateException {
        return x == y;
    }

    @Override
    public int hashCode(Object x) throws HibernateException {
        return x == null ? 0 : x.hashCode();
    }

    @Override
    public Object nullSafeGet(ResultSet rs, String[] names, SharedSessionContractImplementor session, Object owner)
            throws HibernateException, SQLException {
        if (enumValueMapper == null) {
            this.resolveEnumValueMapper(rs, names[0]);
        }
        return enumValueMapper.getValue(rs, names);
    }

    private void resolveEnumValueMapper(ResultSet rs, String name) {
        if (enumValueMapper == null) {
            try {
                this.resolveEnumValueMapper(rs.getMetaData().getColumnType(rs.findColumn(name)));
            } catch (Exception e) {
                LOGGER.debug("JDBC driver threw exception calling java.sql.ResultSetMetaData.getColumnType; "
                        + "using fallback determination [%s] : %s", enumClass.getName(), e.getMessage());
                try {
                    Object value = rs.getObject(name);
                    if (Number.class.isInstance(value)) {
                        treatAsOrdinal();
                    } else {
                        throw new RuntimeException("不支持字符型值枚举列");
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
            throw new RuntimeException("不支持非数值枚举列");
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void nullSafeSet(PreparedStatement st, Object value, int index, SharedSessionContractImplementor session)
            throws HibernateException, SQLException {
        if (enumValueMapper == null) {
            resolveEnumValueMapper(st, index);
        }
        enumValueMapper.setValue(st, (E) value, index);
    }

    private void resolveEnumValueMapper(PreparedStatement st, int index) {
        if (enumValueMapper == null) {
            try {
                resolveEnumValueMapper(st.getParameterMetaData().getParameterType(index));
            } catch (Exception e) {
                LOGGER.debug(
                        "JDBC driver threw exception calling java.sql.ParameterMetaData#getParameterType; "
                                + "falling back to ordinal-based enum mapping [%s] : %s",
                        enumClass.getName(), e.getMessage());
                treatAsOrdinal();
            }
        }
    }

    @Override
    public Object deepCopy(Object value) throws HibernateException {
        return value;
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public Serializable disassemble(Object value) throws HibernateException {
        return (Serializable) value;
    }

    @Override
    public Object assemble(Serializable cached, Object owner) throws HibernateException {
        return cached;
    }

    @Override
    public Object replace(Object original, Object target, Object owner) throws HibernateException {
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
            if (enumType == null) {
                isOrdinal = true;
            } else if (javax.persistence.EnumType.ORDINAL.equals(enumType)) {
                isOrdinal = true;
            } else if (javax.persistence.EnumType.STRING.equals(enumType)) {
                isOrdinal = false;
            } else {
                throw new AssertionFailure("Unknown EnumType: " + enumType);
            }

            if (isOrdinal) {
                treatAsOrdinal();
            } else {
                throw new RuntimeException("不支持字符型值枚举列");
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
                    throw new RuntimeException("不支持字符型值枚举列");
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
            this.enumValueMapper = new OrdinalEnumValueMapper<E>();
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
        return enumValueMapper.objectToSQLString((E) value);
    }

    @Override
    public String toXMLString(Object value) {
        return enumValueMapper.toXMLString((E) value);
    }

    @Override
    public Object fromXMLString(String xmlValue) {
        return enumValueMapper.fromXMLString(xmlValue);
    }

    @Override
    public String toLoggableString(Object value, SessionFactoryImplementor factory) {
        if (enumValueMapper != null) {
            return enumValueMapper.toXMLString((E) value);
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

    private class OrdinalEnumValueMapper<T extends Enum<T>> extends EnumValueMapperSupport<T> {
        private static final long serialVersionUID = -1329754149883071778L;

        @Override
        public int getSqlType() {
            return Types.INTEGER;
        }

        @SuppressWarnings("unchecked")
        @Override
        public T getValue(ResultSet rs, String[] names) throws SQLException {
            if (rs.wasNull()) {
                LOGGER.trace(String.format("Returning null as column [%s]", names[0]));
                return null;
            }
            int ordinal = rs.getInt(names[0]);
            T enumValue = (T) EnumUtils.getEnum(enumClass, ordinal);
            LOGGER.trace(String.format("Returning [%s] as column [%s]", enumValue, names[0]));
            return enumValue;
        }

        @Override
        public String objectToSQLString(T value) {
            return toXMLString(value);
        }

        @Override
        public String toXMLString(T value) {
            return ClassUtils.getFieldValue(value, "value").get("value").toString();
        }

        @SuppressWarnings("unchecked")
        @Override
        public T fromXMLString(String xml) {
            return (T) EnumUtils.getEnum(enumClass, Integer.parseInt(xml));
        }

        @Override
        protected Object extractJdbcValue(T value) {
            return ClassUtils.getFieldValue(value, "value").get("value");
        }
    }
}

interface EnumValueMapper<E extends Enum<E>> extends Serializable {
    int getSqlType();

    E getValue(ResultSet rs, String[] names) throws SQLException;

    void setValue(PreparedStatement st, E value, int index) throws SQLException;

    String objectToSQLString(E value);

    String toXMLString(E value);

    E fromXMLString(String xml);
}

abstract class EnumValueMapperSupport<E extends Enum<E>> implements EnumValueMapper<E> {
    private static final long serialVersionUID = -3165220578977760483L;
    private static final Logger LOG = LoggerFactory.getLogger(EnumValueMapperSupport.class);

    protected abstract Object extractJdbcValue(E value);

    @Override
    public void setValue(PreparedStatement st, E value, int index) throws SQLException {
        final Object jdbcValue = value == null ? null : extractJdbcValue(value);

        if (jdbcValue == null) {
            LOG.trace(String.format("Binding null to parameter: [%s]", index));
            st.setNull(index, getSqlType());
            return;
        }

        LOG.trace(String.format("Binding [%s] to parameter: [%s]", jdbcValue, index));
        st.setObject(index, jdbcValue, this.getSqlType());
    }
}
