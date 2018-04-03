package org.spin.data.sql.param;

/**
 * SQL命名参数
 */
public class SqlParameter {
    private final String parameterName;

    private final int startIndex;

    private final int endIndex;

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
}
