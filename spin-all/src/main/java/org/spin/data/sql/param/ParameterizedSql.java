package org.spin.data.sql.param;

import org.spin.core.Assert;
import org.spin.core.throwable.SQLException;
import org.spin.data.sql.SqlSource;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * 参数化SQL
 * <p>Created by xuweinan on 2018/4/3.</p>
 *
 * @author xuweinan
 */
public class ParameterizedSql implements Serializable {
    private static final long serialVersionUID = -3841234687808576992L;
    private static final char[][] START_SKIP = new char[][]{{'\''}, {'"'}, {'-', '-'}, {'/', '*'}};
    private static final char[][] STOP_SKIP = new char[][]{{'\''}, {'"'}, {'\n'}, {'*', '/'}};
    private static final char[] PARAMETER_SEPARATORS = {'"', '\'', ':', '&', ',', ';', '(', ')', '|', '=', '+', '-', '*', '%', '/', '\\', '<', '>', '^'};
    private static final boolean[] separatorIndex = new boolean[128];

    static {
        for (char c : PARAMETER_SEPARATORS) {
            separatorIndex[c] = true;
        }
    }

    private SqlSource originalSql;

    private SqlSource actualSql;

    private List<SqlParameter> namedParameters = new LinkedList<>();

    private int namedParameterCount;

    private int unnamedParameterCount;

    private int totalParameterCount;


    public ParameterizedSql(SqlSource originalSql) {
        this.originalSql = originalSql;
        parseSqlStatement();
    }

    public String getId() {
        return originalSql.getId();
    }

    public SqlSource getOriginalSql() {
        return originalSql;
    }

    public SqlSource getActualSql() {
        return actualSql;
    }

    public List<SqlParameter> getNamedParameters() {
        return namedParameters;
    }

    public int getNamedParameterCount() {
        return namedParameterCount;
    }

    public int getUnnamedParameterCount() {
        return unnamedParameterCount;
    }

    public int getTotalParameterCount() {
        return totalParameterCount;
    }

    /**
     * Exposes the original SQL String.
     */
    @Override
    public String toString() {
        return this.originalSql.getSql();
    }

    private void parseSqlStatement() {
        Assert.notNull(originalSql, "SQL must not be null");

        String sqlToUse = Assert.notEmpty(originalSql.getSql(), "SQL must not be empty");
        StringBuilder actualSqlStr = new StringBuilder(sqlToUse.length());

        char[] statement = sqlToUse.toCharArray();

        // 当前位置
        int i = 0;
        while (i < statement.length) {
            int skipToPosition, s = i;
            while (i < statement.length) {
                skipToPosition = skipCommentsAndQuotes(statement, i);
                if (i == skipToPosition) {
                    break;
                } else {
                    i = skipToPosition;
                }
            }
            actualSqlStr.append(sqlToUse, s, i);
            if (i >= statement.length) {
                break;
            }
            char c = statement[i];
            // :或&开头，命名参数
            if (c == ':' || c == '&') {
                // 参数名称起始
                int j = i + 1;
                if (j < statement.length && statement[j] == ':' && c == ':') {
                    // Postgres-style "::" casting operator should be skipped
                    i = i + 2;
                    actualSqlStr.append("::");
                    continue;
                }
                String parameter = null;
                if (j < statement.length && c == ':' && statement[j] == '{') {
                    // :{x}形式的命名参数
                    while (j < statement.length && '}' != statement[j]) {
                        ++j;
                        if (':' == statement[j] || '{' == statement[j]) {
                            throw new SQLException(SQLException.SQL_EXCEPTION, String.format("命名参数在索引%d发现非法字符'%c'%n原始SQL:%s", i, statement[j], sqlToUse));
                        }
                    }
                    if (j >= statement.length) {
                        throw new SQLException(SQLException.SQL_EXCEPTION, String.format("命名参数声明在%d处未正确结束%n原始SQL:%s ", i, sqlToUse));
                    }
                    if (j - i > 3) {
                        parameter = sqlToUse.substring(i + 2, j);
                        namedParameters.add(new SqlParameter(parameter, ++totalParameterCount, i, j + 1));
                        ++namedParameterCount;
                        actualSqlStr.append('?');
                    }
                    j++;
                } else {
                    // :x或&x形式的命名参数
                    while (j < statement.length && !isParameterTerminate(statement[j])) {
                        j++;
                    }
                    if (j - i > 1) {
                        parameter = sqlToUse.substring(i + 1, j);
                        namedParameters.add(new SqlParameter(parameter, ++totalParameterCount, i, j));
                        ++namedParameterCount;
                        actualSqlStr.append('?');
                    }
                }
                i = j - 1;
            } else {
                if (c == '\\') {
                    // 遇到转义的\: 跳过参数解析
                    actualSqlStr.append(c);
                    int j = i + 1;
                    if (j < statement.length && statement[j] == ':') {
                        // escaped ":" should be skipped
                        i += 2;
                        actualSqlStr.append(':');
                        continue;
                    }
                }
                if (c == '?') {
                    actualSqlStr.append(c);
                    int j = i + 1;
                    if (j < statement.length && (statement[j] == '?' || statement[j] == '|' || statement[j] == '&')) {
                        // Postgres-style "??", "?|", "?&" operator should be skipped
                        i += 2;
                        actualSqlStr.append(statement[j]);
                        continue;
                    }
                    unnamedParameterCount++;
                    totalParameterCount++;
                }
            }
            if (c != ':' && c != '?' && c != '&') {
                actualSqlStr.append(c);
            }
            i++;
        }
        actualSql = new SqlSource(originalSql.getId(), actualSqlStr.toString());
    }

    /**
     * 从当前位置跳过注释与字符串
     *
     * @param statement 语句
     * @param position  当前位置
     * @return 从当前位置后的第一个非注释与字符串的位置
     */
    private int skipCommentsAndQuotes(char[] statement, int position) {
        for (int i = 0; i < START_SKIP.length; i++) {
            if (statement[position] == START_SKIP[i][0] && (i < 2 || statement[position + 1] == START_SKIP[i][1])) {
                int offset = START_SKIP[i].length;
                for (int m = position + offset; m < statement.length; m++) {
                    if (statement[m] == STOP_SKIP[i][0]) {
                        boolean endMatch = true;
                        int endPos = m;
                        for (int n = 1; n < STOP_SKIP[i].length; n++) {
                            if (m + n >= statement.length) {
                                // last comment not closed properly
                                return statement.length;
                            }
                            if (statement[m + n] != STOP_SKIP[i][n]) {
                                endMatch = false;
                                break;
                            }
                            endPos = m + n;
                        }
                        if (endMatch) {
                            // found character sequence ending comment or quote
                            return endPos + 1;
                        }
                    }
                }
                // character sequence ending comment or quote not found
                return statement.length;
            }
        }
        return position;
    }

    private boolean isParameterTerminate(char c) {
        return (c < 128 && separatorIndex[c]) || Character.isWhitespace(c);
    }
}
