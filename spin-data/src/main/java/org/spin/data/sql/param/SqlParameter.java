package org.spin.data.sql.param;

/**
 * SQL命名参数
 * <p>Created by xuweinan on 2018/4/3.</p>
 *
 * @author xuweinan
 */
public class SqlParameter {
    private final String parameterName;

    private final int startIndex;

    private final int endIndex;

    private int sqlType = ParameterUtils.TYPE_UNKNOWN;

    // Used for types that are user-named like: STRUCT, DISTINCT, JAVA_OBJECT, named array types
    private String typeName;

    // The scale to apply in case of a NUMERIC or DECIMAL type, if any
    private Integer scale;

    public SqlParameter(String parameterName, int startIndex, int endIndex) {
        this.parameterName = parameterName;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }

    public String getParameterName() {
        return this.parameterName;
    }

    public int getStartIndex() {
        return this.startIndex;
    }

    public int getEndIndex() {
        return this.endIndex;
    }

    public int getSqlType() {
        return sqlType;
    }

    public void setSqlType(int sqlType) {
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
