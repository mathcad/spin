package org.spin.data.sql.param;

import org.spin.core.Assert;
import org.spin.core.throwable.SimplifiedException;
import org.spin.data.sql.SqlSource;

import java.util.LinkedList;
import java.util.List;

/**
 * 参数化SQL
 */
public class ParameterizedSql {
    /**
     * Set of characters that qualify as comment or quotes starting characters.
     */
    private static final char[][] START_SKIP = new char[][]{{'\''}, {'"'}, {'-', '-'}, {'/', '*'}};

    /**
     * Set of characters that at are the corresponding comment or quotes ending characters.
     */
    private static final char[][] STOP_SKIP = new char[][]{{'\''}, {'"'}, {'\n'}, {'*', '/'}};

    /**
     * Set of characters that qualify as parameter separators,
     * indicating that a parameter name in a SQL String has ended.
     */
    private static final String PARAMETER_SEPARATORS = "\"':&,;()|=+-*%/\\<>^";

    /**
     * An index with separator flags per character code.
     * Technically only needed between 34 and 124 at this point.
     */
    private static final boolean[] separatorIndex = new boolean[128];

    static {
        for (char c : PARAMETER_SEPARATORS.toCharArray()) {
            separatorIndex[c] = true;
        }
    }

    private SqlSource originalSql;

    private SqlSource actualSql;

    private List<SqlParameter> parameters = new LinkedList<>();

    private int namedParameterCount;

    private int unnamedParameterCount;

    private int totalParameterCount;


    ParameterizedSql(SqlSource originalSql) {
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

    public List<SqlParameter> getParameters() {
        return parameters;
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
        StringBuilder actualSql = new StringBuilder(sqlToUse.length());

        char[] statement = sqlToUse.toCharArray();

        int escapes = 0;
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
            actualSql.append(sqlToUse, s, i);
            if (i >= statement.length) {
                break;
            }
            char c = statement[i];
            if (c == ':' || c == '&') {
                int j = i + 1;
                if (j < statement.length && statement[j] == ':' && c == ':') {
                    // Postgres-style "::" casting operator should be skipped
                    i = i + 2;
                    continue;
                }
                String parameter = null;
                if (j < statement.length && c == ':' && statement[j] == '{') {
                    // :{x} style parameter
                    while (j < statement.length && '}' != statement[j]) {
                        j++;
                        if (':' == statement[j] || '{' == statement[j]) {
                            throw new SimplifiedException("Parameter name contains invalid character '" +
                                statement[j] + "' at position " + i + " in statement: " + sqlToUse);
                        }
                    }
                    if (j >= statement.length) {
                        throw new SimplifiedException(
                            "Non-terminated named parameter declaration at position " + i + " in statement: " + sqlToUse);
                    }
                    if (j - i > 3) {
                        parameter = sqlToUse.substring(i + 2, j);
                        parameters.add(new SqlParameter(parameter, i - escapes, j + 1 - escapes));
                        ++namedParameterCount;
                        ++totalParameterCount;
                        actualSql.append("? ");
                    }
                    j++;
                } else {
                    while (j < statement.length && !isParameterSeparator(statement[j])) {
                        j++;
                    }
                    if (j - i > 1) {
                        parameter = sqlToUse.substring(i + 1, j);
                        ++namedParameterCount;
                        ++totalParameterCount;
                        parameters.add(new SqlParameter(parameter, i - escapes, j + 1 - escapes));
                        actualSql.append("? ");
                    }
                }
                i = j - 1;
            } else {
                if (c == '\\') {
                    int j = i + 1;
                    if (j < statement.length && statement[j] == ':') {
                        // escaped ":" should be skipped
                        sqlToUse = sqlToUse.substring(0, i - escapes) + sqlToUse.substring(i - escapes + 1);
                        escapes++;
                        i = i + 2;
                        continue;
                    }
                }
                if (c == '?') {
                    int j = i + 1;
                    if (j < statement.length && (statement[j] == '?' || statement[j] == '|' || statement[j] == '&')) {
                        // Postgres-style "??", "?|", "?&" operator should be skipped
                        i = i + 2;
                        continue;
                    }
                    unnamedParameterCount++;
                    totalParameterCount++;
                }
            }
            i++;
        }
    }

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

    private boolean isParameterSeparator(char c) {
        return (c < 128 && separatorIndex[c]) || Character.isWhitespace(c);
    }
}
