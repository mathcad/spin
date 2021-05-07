package org.spin.datasource.ds;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.util.StringUtils;
import org.spin.datasource.CurrentDatasourceInfo;
import org.spin.datasource.Ds;
import org.spin.datasource.toolkit.DynamicDataSourceContextHolder;
import org.spin.datasource.tx.ConnectionFactory;
import org.spin.datasource.tx.ConnectionProxy;
import org.spin.datasource.tx.TransactionContext;
import org.springframework.jdbc.datasource.AbstractDataSource;
import org.springframework.lang.NonNull;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 抽象动态获取数据源
 *
 * @author TaoYu
 * @since 2.2.0
 */
public abstract class AbstractRoutingDataSource extends AbstractDataSource {
    private static final Logger logger = LoggerFactory.getLogger(AbstractRoutingDataSource.class);

    protected abstract DataSource determineDataSource();

    @Override
    public Connection getConnection() throws SQLException {
        String xid = TransactionContext.getXID();
        Connection connection;
        CurrentDatasourceInfo ds = DynamicDataSourceContextHolder.peek();
        if (StringUtils.isEmpty(xid) || null == ds) {
            connection = determineDataSource().getConnection();
        } else {
            connection = ConnectionFactory.getConnection(ds.getDatasource());
            if (null == connection) {
                connection = getConnectionProxy(ds.getDatasource(), determineDataSource().getConnection());
            }
        }
        String poolName = null == ds ? Ds.getPrimaryDataSource() : ds.getDatasource();
        String catalog = null == ds || null == ds.getCatalog() ? Ds.getDefaultCatalog(poolName) : ds.getCatalog();
        if (null != catalog) {
            logger.info("Get connection from Datasource[{}], using catalog [{}]", poolName, catalog);
            connection.setCatalog(catalog);
        }

        return connection;
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        String xid = TransactionContext.getXID();
        CurrentDatasourceInfo ds = DynamicDataSourceContextHolder.peek();
        DataSource dataSource = determineDataSource();
        Connection connection;
        if (StringUtils.isEmpty(xid) || null == ds) {
            connection = dataSource.getConnection(username, password);
        } else {
            connection = ConnectionFactory.getConnection(ds.getDatasource());
            if (null == connection) {
                connection = getConnectionProxy(ds.getDatasource(), dataSource.getConnection(username, password));
            }

        }
        String poolName = null == ds ? Ds.getPrimaryDataSource() : ds.getDatasource();
        String catalog = null == ds || null == ds.getCatalog() ? Ds.getDefaultCatalog(poolName) : ds.getCatalog();
        if (null != catalog) {
            logger.info("Get connection from Datasource[{}], using catalog [{}]", poolName, catalog);
            connection.setCatalog(catalog);
        }

        return connection;
    }

    private Connection getConnectionProxy(String ds, Connection connection) {
        ConnectionProxy connectionProxy = new ConnectionProxy(connection, ds);
        ConnectionFactory.putConnection(ds, connectionProxy);
        return connectionProxy;
    }

    @Override
    @NonNull
    @SuppressWarnings("unchecked")
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (iface.isInstance(this)) {
            return (T) this;
        }
        return determineDataSource().unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return (iface.isInstance(this) || determineDataSource().isWrapperFor(iface));
    }
}
