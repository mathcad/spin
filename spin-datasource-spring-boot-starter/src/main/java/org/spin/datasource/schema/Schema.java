package org.spin.datasource.schema;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.Assert;
import org.spin.core.function.ExceptionalHandler;
import org.spin.core.function.ExceptionalSupplier;
import org.spin.core.util.Util;
import org.spin.datasource.Ds;
import org.spin.datasource.toolkit.DynamicDataSourceContextHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.sql.SQLException;
import java.util.function.Consumer;

/**
 * Schema上下文工具类
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2021/1/28</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class Schema extends Util {
    private static final Logger logger = LoggerFactory.getLogger(Schema.class);

    private static SchemaDataSourceProvider dataSourceProvider;

    private static TransactionalSyncConnectionProvider transactionSyncConnectionProvider;

    public static void using(String schema, ExceptionalHandler<Exception> handler) {
        using(schema, () -> {
            handler.handle();
            return null;
        }, null);
    }

    public static void using(String schema, ExceptionalHandler<Exception> handler, Consumer<Exception> exceptionConsumer) {
        using(schema, () -> {
            handler.handle();
            return null;
        }, exceptionConsumer);
    }

    public static <T> T using(String schema, ExceptionalSupplier<T, Exception> handler) {
        return using(schema, handler, null);
    }

    public static <T> T using(String schema, ExceptionalSupplier<T, Exception> handler, Consumer<Exception> exceptionConsumer) {
        String dataSource = schema;
        String currentDs = null == DynamicDataSourceContextHolder.peek() ?
            Ds.getPrimaryDataSource() : DynamicDataSourceContextHolder.peek().getDatasource();
        if (dataSource.indexOf('@') < 0) {
            if (dataSourceProvider != null) {
                String target = Assert.notEmpty(dataSourceProvider.determineDataSource(schema), "无法确定Schema[" + schema + "]所属的数据源");
                dataSource = schema + "@" + target;

            } else {
                dataSource = schema + "@" + currentDs;
            }
        }
        if (null == handler) {
            return null;
        }

        DynamicDataSourceContextHolder.push(dataSource);
        boolean inCurrentDs = DynamicDataSourceContextHolder.peek().getDatasource().equals(currentDs)
            && null != transactionSyncConnectionProvider
            && TransactionSynchronizationManager.isActualTransactionActive();

        if (inCurrentDs) {
            try {
                transactionSyncConnectionProvider.currentConnection().setCatalog(DynamicDataSourceContextHolder.peek().getCatalog());
            } catch (SQLException e) {
                logger.error("切换Schema失败: ", e);
            }
        }
        try {
            return handler.get();
        } catch (Exception e) {
            if (null != exceptionConsumer) {
                exceptionConsumer.accept(e);
            } else {
                logger.error("切换Schema后业务执行异常: ", e);
            }
            return null;
        } finally {
            DynamicDataSourceContextHolder.poll();
            if (inCurrentDs) {
                String lastSchema;
                if (null != DynamicDataSourceContextHolder.peek()) {
                    lastSchema = DynamicDataSourceContextHolder.peek().getCatalog();
                } else {
                    lastSchema = Ds.getDefaultCatalog(currentDs);
                }
                try {
                    transactionSyncConnectionProvider.currentConnection().setCatalog(lastSchema);
                } catch (SQLException e) {
                    logger.error("还原Schema失败", e);
                }
            }
        }
    }

    public static void setDataSourceProvider(SchemaDataSourceProvider dataSourceProvider) {
        Schema.dataSourceProvider = dataSourceProvider;
    }

    public static void setTransactionSyncConnectionProvider(TransactionalSyncConnectionProvider transactionSyncConnectionProvider) {
        Schema.transactionSyncConnectionProvider = transactionSyncConnectionProvider;
    }
}
