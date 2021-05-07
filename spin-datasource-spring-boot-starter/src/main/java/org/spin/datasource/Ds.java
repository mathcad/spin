package org.spin.datasource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.Assert;
import org.spin.core.function.ExceptionalHandler;
import org.spin.core.util.Util;
import org.spin.datasource.toolkit.DynamicDataSourceContextHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 数据源工具类
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2021/1/28</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class Ds extends Util {
    private static final Logger logger = LoggerFactory.getLogger(Ds.class);
    private static final Map<String, String> DEFAULT_SCHEMA = new HashMap<>();
    private static String primaryDataSource;

    /**
     * 使用特定数据源执行操作
     *
     * @param ds      数据源名称(可以包括schema, 如schemaName@DatasourceName)
     * @param handler 操作
     */
    public static void using(String ds, ExceptionalHandler<Exception> handler) {
        using(ds, handler, null);
    }

    /**
     * 使用特定数据源执行操作
     *
     * @param ds                数据源名称(可以包括schema, 如schemaName@DatasourceName)
     * @param handler           操作
     * @param exceptionConsumer 异常处理
     */
    public static void using(String ds, ExceptionalHandler<Exception> handler, Consumer<Exception> exceptionConsumer) {
        if (null == handler) {
            return;
        }

        CurrentDatasourceInfo dsInfo = new CurrentDatasourceInfo(Assert.notEmpty(ds, "数据源名称不能为空"));
        DynamicDataSourceContextHolder.push(dsInfo);
        try {
            handler.handle();
        } catch (Exception e) {
            if (null != exceptionConsumer) {
                exceptionConsumer.accept(e);
            } else {
                logger.error("切换数据源后操作异常: ", e);
            }
        } finally {
            DynamicDataSourceContextHolder.poll();
        }
    }

    public static void putDefaultCatalog(String ds, String defaultCatalog) {
        DEFAULT_SCHEMA.put(ds, defaultCatalog);
    }

    public static String getDefaultCatalog(String ds) {
        return DEFAULT_SCHEMA.get(ds);
    }

    public static String getPrimaryDataSource() {
        return primaryDataSource;
    }

    public static void setPrimaryDataSource(String primaryDataSource) {
        Ds.primaryDataSource = primaryDataSource;
    }
}
