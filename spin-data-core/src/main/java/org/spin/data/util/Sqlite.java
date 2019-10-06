package org.spin.data.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.function.FinalConsumer;
import org.spin.core.gson.reflect.TypeToken;
import org.spin.core.throwable.SpinException;
import org.spin.core.util.CollectionUtils;
import org.spin.data.rs.RowMapper;
import org.spin.data.rs.RowMappers;
import org.spin.data.throwable.SQLError;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Sqlite工具类
 * <p>Created by xuweinan on 2019/10/6.</p>
 *
 * @author xuweinan
 */
public class Sqlite implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(Sqlite.class);
    private static final String MEMORY_MODE = ":memory:";
    private static final String CONN_STR_PREFIX = "jdbc:sqlite:";

    private boolean inMemory = false;
    private boolean closed = false;
    private Connection connection;
    private String dbFilePath;

    private Sqlite(String dbFilePath) {
        this.dbFilePath = dbFilePath;
        inMemory = MEMORY_MODE.equals(dbFilePath);
        connection = getConnection();
    }

    public static Sqlite inMemoryMode() {
        return new Sqlite(MEMORY_MODE);
    }

    public static Sqlite inFileMode(String dbFilePath) {
        return new Sqlite(dbFilePath);
    }

    public boolean execute(String sql, FinalConsumer<PreparedStatement> statementProcessor) {
        Connection connection = getConnection();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            if (null != statementProcessor) {
                statementProcessor.accept(stmt);
            }
            return stmt.execute();
        } catch (SQLException e) {
            throw new org.spin.data.throwable.SQLException(SQLError.SQL_EXCEPTION, "SQL执行失败", e);
        }
    }

    public int update(String sql, FinalConsumer<PreparedStatement> statementProcessor) {
        Connection connection = getConnection();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            if (null != statementProcessor) {
                statementProcessor.accept(stmt);
            }
            return stmt.executeUpdate();
        } catch (SQLException e) {
            throw new org.spin.data.throwable.SQLException(SQLError.SQL_EXCEPTION, "SQL执行失败", e);
        }
    }

    public <T> List<T> query(String sql, FinalConsumer<PreparedStatement> statementProcessor, RowMapper<T> rowMapper) {
        Connection connection = getConnection();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            if (null != statementProcessor) {
                statementProcessor.accept(stmt);
            }
            try (ResultSet resultSet = stmt.executeQuery()) {
                return rowMapper.extractData(resultSet);
            }
        } catch (SQLException e) {
            throw new org.spin.data.throwable.SQLException(SQLError.SQL_EXCEPTION, "SQL执行失败", e);
        }
    }

    public <T> List<T> query(String sql, FinalConsumer<PreparedStatement> statementProcessor, TypeToken<T> resultType) {
        return query(sql, statementProcessor, RowMappers.getMapper(resultType));
    }

    public <T> T queryFirst(String sql, FinalConsumer<PreparedStatement> statementProcessor, RowMapper<T> rowMapper) {
        Connection connection = getConnection();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            if (null != statementProcessor) {
                statementProcessor.accept(stmt);
            }
            try (ResultSet resultSet = stmt.executeQuery()) {
                List<T> ts = rowMapper.extractData(resultSet, 1);
                if (CollectionUtils.isEmpty(ts)) {
                    return null;
                } else {
                    return ts.get(0);
                }
            }
        } catch (SQLException e) {
            throw new org.spin.data.throwable.SQLException(SQLError.SQL_EXCEPTION, "SQL执行失败", e);
        }
    }

    public <T> T queryFirst(String sql, FinalConsumer<PreparedStatement> statementProcessor, TypeToken<T> resultType) {
        return queryFirst(sql, statementProcessor, RowMappers.getMapper(resultType));
    }

    @Override
    public void close() {
        if (!closed) {
            try {
                if (null != connection && !connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                connection = null;
            }
            closed = true;
        }
    }

    /**
     * 获取数据库连接
     *
     * @return 数据库连接
     */
    private Connection getConnection() {
        if (closed) {
            throw new org.spin.data.throwable.SQLException(SQLError.SQL_EXCEPTION, "连接已经关闭");
        }
        try {
            if (null == connection || connection.isClosed()) {
                connection = DriverManager.getConnection(CONN_STR_PREFIX + dbFilePath);
            }
        } catch (SQLException e) {
            throw new SpinException(SQLError.SQL_EXCEPTION, "创建连接失败");
        }
        return connection;
    }

}
