package org.spin.datasource.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.util.StringUtils;
import org.spin.datasource.spring.boot.autoconfigure.DataSourceProperty;
import org.springframework.jdbc.support.JdbcUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

/**
 * JDBC数据源提供者(抽象)
 *
 * @author TaoYu
 * @since 2.1.2
 */
public abstract class AbstractJdbcDataSourceProvider extends AbstractDataSourceProvider implements DynamicDataSourceProvider {
    private static final Logger logger = LoggerFactory.getLogger(AbstractJdbcDataSourceProvider.class);

    /**
     * JDBC driver
     */
    private final String driverClassName;
    /**
     * JDBC url 地址
     */
    private final String url;
    /**
     * JDBC 用户名
     */
    private final String username;
    /**
     * JDBC 密码
     */
    private final String password;


    public AbstractJdbcDataSourceProvider(String url, String username, String password) {
        this(null, url, username, password);
    }


    public AbstractJdbcDataSourceProvider(String driverClassName, String url, String username, String password) {
        this.driverClassName = driverClassName;
        this.url = url;
        this.username = username;
        this.password = password;
    }

    @Override
    public Map<String, DataSource> loadDataSources() {
        Connection conn = null;
        Statement stmt = null;
        try {
            // 由于 SPI 的支持，现在已无需显示加载驱动了
            // 但在用户显示配置的情况下，进行主动加载
            if (StringUtils.isNotEmpty(driverClassName)) {
                Class.forName(driverClassName);
                logger.info("成功加载数据库驱动程序");
            }
            conn = DriverManager.getConnection(url, username, password);
            logger.info("成功获取数据库连接");
            stmt = conn.createStatement();
            Map<String, DataSourceProperty> dataSourcePropertiesMap = executeStmt(stmt);
            return createDataSourceMap(dataSourcePropertiesMap);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            JdbcUtils.closeConnection(conn);
            JdbcUtils.closeStatement(stmt);
        }
        return null;
    }

    /**
     * 执行语句获得数据源参数
     *
     * @param statement 语句
     * @return 数据源参数
     * @throws SQLException sql异常
     */
    protected abstract Map<String, DataSourceProperty> executeStmt(Statement statement)
        throws SQLException;
}
