package org.spin.data.extend;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.internal.util.ReflectHelper;
import org.hibernate.usertype.DynamicParameterizedType;
import org.hibernate.usertype.EnhancedUserType;
import org.hibernate.usertype.LoggableUserType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.Assert;
import org.spin.core.throwable.SpinException;
import org.spin.core.trait.Evaluatable;
import org.spin.core.util.BeanUtils;
import org.spin.core.util.EnumUtils;
import org.spin.core.util.StringUtils;
import org.spin.data.sql.JdbcUtils;

import javax.persistence.Enumerated;
import javax.persistence.MapKeyEnumerated;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Objects;
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
    public static final String FIELD = "field";

    private Class<? extends Enum> enumClass;
    private EnumValueMapper enumValueMapper;
    private String field;

    @Override
    public int[] sqlTypes() {
        return new int[]{Types.INTEGER};
    }

    @Override
    public Class<?> returnedClass() {
        return enumClass;
    }

    @Override
    public boolean equals(Object x, Object y) {
        return Objects.equals(x, y);
    }

    @Override
    public int hashCode(Object x) {
        return Objects.hashCode(x);
    }

    @Override
    public Object nullSafeGet(ResultSet rs, String[] names, SharedSessionContractImplementor session, Object owner) throws SQLException {
        if (enumValueMapper == null) {
            throw new SpinException("枚举映射尚未初始化");
        }
        return enumValueMapper.getValue(rs, names);
    }

    @Override
    public void nullSafeSet(PreparedStatement st, Object value, int index, SharedSessionContractImplementor session) throws SQLException {
        if (enumValueMapper == null) {
            throw new SpinException("枚举映射尚未初始化");
        }
        enumValueMapper.setValue(st, (Enum) value, index);
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

            final javax.persistence.EnumType enumType = getEnumType(reader);
            EnumMapType mapType = (EnumMapType) parameters.get("enumMapType");
            if (javax.persistence.EnumType.ORDINAL.equals(enumType) || EnumMapType.ORDINAL.equals(mapType)) {
                treatAsOrdinal();
            } else if (EnumMapType.NAME.equals(mapType)) {
                treatAsName();
            } else {
                treatAsValue(parameters);
            }
        } else {
            String enumClassName = (String) parameters.get(ENUM);
            try {
                enumClass = ReflectHelper.classForName(enumClassName, this.getClass()).asSubclass(Enum.class);
            } catch (ClassNotFoundException exception) {
                throw new HibernateException("Enum class not found", exception);
            }

            EnumMapType mapType = (EnumMapType) parameters.get("enumMapType");
            if (EnumMapType.ORDINAL.equals(mapType)) {
                treatAsOrdinal();
            } else if (EnumMapType.NAME.equals(mapType)) {
                treatAsName();
            } else {
                treatAsValue(parameters);
            }
        }
    }

    @Override
    public String objectToSQLString(Object value) {
        return enumValueMapper.toSQLString((Enum<?>) value);
    }

    @Override
    @Deprecated
    public String toXMLString(Object value) {
        return enumValueMapper.toSQLString((Enum<?>) value);
    }

    @Override
    @Deprecated
    public Object fromXMLString(String xmlValue) {
        return enumValueMapper.fromStringValue(xmlValue);
    }

    @Override
    public String toLoggableString(Object value, SessionFactoryImplementor factory) {
        if (enumValueMapper != null) {
            return enumValueMapper.toSQLString((Enum<?>) value);
        }
        return value.toString();
    }

    private void treatAsOrdinal() {
        if (!(enumValueMapper instanceof OrdinalEnumValueMapper)) {
            enumValueMapper = new OrdinalEnumValueMapper();
        }
    }

    private void treatAsValue(Properties parameters) {
        if (Evaluatable.class.isAssignableFrom(enumClass)) {
            if (!(enumValueMapper instanceof ValueEnumValueMapper)) {
                enumValueMapper = new ValueEnumValueMapper();
            }
        } else {
            field = Assert.notEmpty(parameters.getProperty(FIELD));
            treatAsName();
        }
    }

    private void treatAsName() {
        if (!(enumValueMapper instanceof NameEnumValueMapper)) {
            enumValueMapper = new NameEnumValueMapper();
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

    private class ValueEnumValueMapper extends EnumValueMapperSupport {

        @Override
        public Enum<?> getValue(ResultSet rs, String[] names) throws SQLException {
            final Object value = rs.getObject(names[0]);
            if (rs.wasNull()) {
                if (logger.isDebugEnabled()) {
                    logger.debug(String.format("Returning null as column [%s]", names[0]));
                }
                return null;
            }
            Enum<?> enumValue;
            if (StringUtils.isEmpty(field)) {
                //noinspection unchecked
                enumValue = EnumUtils.getEnum(enumClass, value);
            } else {
                //noinspection unchecked
                enumValue = EnumUtils.getEnum(enumClass, value, field);
            }
            if (logger.isDebugEnabled()) {
                logger.trace(String.format("Returning [%s] as column [%s]", enumValue, names[0]));
            }
            return enumValue;
        }

        @Override
        public String toSQLString(Enum<?> value) {
            return StringUtils.toString(extractJdbcValue(value));
        }

        @Override
        public Enum<?> fromStringValue(String string) {
            //noinspection unchecked
            return EnumUtils.getEnum(enumClass, string);
        }

        @Override
        protected Object extractJdbcValue(Enum<?> value) {
            if (value instanceof Evaluatable) {
                return ((Evaluatable) value).getValue();
            } else if (null == value) {
                return null;
            } else {
                return BeanUtils.getFieldValue(value, field);
            }
        }
    }

    private class OrdinalEnumValueMapper extends EnumValueMapperSupport {

        @Override
        public Enum<?> getValue(ResultSet rs, String[] names) throws SQLException {
            final int ordinal = rs.getInt(names[0]);
            final boolean traceEnabled = logger.isTraceEnabled();
            if (rs.wasNull()) {
                if (traceEnabled) {
                    logger.trace(String.format("Returning null as column [%s]", names[0]));
                }
                return null;
            }

            @SuppressWarnings("unchecked") final Enum<?> enumValue = EnumUtils.fromOrdinal(enumClass, ordinal);
            if (traceEnabled) {
                logger.trace(String.format("Returning [%s] as column [%s]", enumValue, names[0]));
            }
            return enumValue;
        }

        @Override
        public String toSQLString(Enum<?> value) {
            return StringUtils.toString(null == value ? null : value.ordinal());
        }

        @Override
        public Enum<?> fromStringValue(String string) {
            //noinspection unchecked
            return EnumUtils.fromOrdinal(enumClass, Integer.parseInt(string));
        }

        @Override
        protected Object extractJdbcValue(Enum<?> value) {
            return null == value ? null : value.ordinal();
        }
    }

    private class NameEnumValueMapper extends EnumValueMapperSupport {

        @Override
        public Enum<?> getValue(ResultSet rs, String[] names) throws SQLException {
            final String name = rs.getString(names[0]);
            final boolean traceEnabled = logger.isTraceEnabled();
            if (rs.wasNull()) {
                if (traceEnabled) {
                    logger.trace(String.format("Returning null as column [%s]", names[0]));
                }
                return null;
            }

            @SuppressWarnings("unchecked") final Enum<?> enumValue = EnumUtils.fromName(enumClass, name);
            if (traceEnabled) {
                logger.trace(String.format("Returning [%s] as column [%s]", enumValue, names[0]));
            }
            return enumValue;
        }

        @Override
        public String toSQLString(Enum<?> value) {
            return null == value ? null : value.name();
        }

        @Override
        public Enum<?> fromStringValue(String string) {
            //noinspection unchecked
            return EnumUtils.fromName(enumClass, string);
        }

        @Override
        protected Object extractJdbcValue(Enum<?> value) {
            return null == value ? null : value.name();
        }
    }
}

/**
 * 枚举-jdbc数据类型转换器
 */
interface EnumValueMapper {

    /**
     * 从ResultSet中解析枚举
     *
     * @param rs    结果集
     * @param names 字段名称
     * @return 解析出的枚举
     * @throws SQLException 异常时抛出
     */
    Enum<?> getValue(ResultSet rs, String[] names) throws SQLException;

    /**
     * 将枚举解析为数据库支持的类型并设置到Statement中
     *
     * @param st    语句
     * @param value 枚举值
     * @param index 参数在语句中的索引
     * @throws SQLException 异常时抛出
     */
    void setValue(PreparedStatement st, Enum<?> value, int index) throws SQLException;

    /**
     * 将枚举转换为字符串
     *
     * @param value 枚举值
     * @return 转换后的字符串
     */
    String toSQLString(Enum<?> value);

    /**
     * 将字符串转换为枚举
     *
     * @param string 字符串
     * @return 转换后的枚举
     */
    Enum<?> fromStringValue(String string);
}

abstract class EnumValueMapperSupport implements EnumValueMapper {
    private static final Logger logger = LoggerFactory.getLogger(EnumValueMapperSupport.class);

    /**
     * 将枚举解析成jdbc类型
     *
     * @param value 枚举值
     * @return 解析后的jdbc类型数据
     */
    protected abstract Object extractJdbcValue(Enum<?> value);

    @Override
    public void setValue(PreparedStatement st, Enum<?> value, int index) throws SQLException {
        final Object jdbcValue = extractJdbcValue(value);

        final boolean traceEnabled = logger.isTraceEnabled();
        if (jdbcValue == null) {
            JdbcUtils.setParameterValue(st, index, null);
            if (traceEnabled) {
                logger.trace(String.format("Binding null to parameter: [%s]", index));
            }
        } else {
            JdbcUtils.setParameterValue(st, index, jdbcValue);
            if (traceEnabled) {
                logger.trace(String.format("Binding [%s] to parameter: [%s]", jdbcValue.toString(), index));
            }
        }
    }
}
