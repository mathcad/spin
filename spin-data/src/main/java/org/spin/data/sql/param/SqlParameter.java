package org.spin.data.sql.param;

import org.spin.data.sql.JdbcType;

/**
 * SQL命名参数
 * <p>Created by xuweinan on 2018/4/3.</p>
 *
 * @author xuweinan
 */
public class SqlParameter {
    /**
     * 参数名称
     */
    private final String parameterName;

    /**
     * 参数位置
     */
    private final int paramIndex;

    /**
     * 参数在SQL中的起始索引
     */
    private final int startIndex;

    /**
     * 参数在SQL中的结束索引
     */
    private final int endIndex;

    /**
     * 参数类型
     */
    private JdbcType sqlType = JdbcType.UNKNOWN;

    // Used for types that are user-named like: STRUCT, DISTINCT, JAVA_OBJECT, named array types
    private String typeName;

    // The scale to apply in case of a NUMERIC or DECIMAL type, if any
    private Integer scale;

    public SqlParameter(String parameterName, int paramIndex, int startIndex, int endIndex) {
        this.parameterName = parameterName;
        this.paramIndex = paramIndex;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }

    public String getParameterName() {
        return this.parameterName;
    }

    public int getParamIndex() {
        return paramIndex;
    }

    public int getStartIndex() {
        return this.startIndex;
    }

    public int getEndIndex() {
        return this.endIndex;
    }

    public JdbcType getSqlType() {
        return sqlType;
    }

    public void setSqlType(JdbcType sqlType) {
        this.sqlType = sqlType;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public Integer getScale() {
        return scale;
    }

    public void setScale(Integer scale) {
        this.scale = scale;
    }
}
