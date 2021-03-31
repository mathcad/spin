package org.spin.datasource.schema;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.Assert;
import org.spin.core.function.ExceptionalHandler;
import org.spin.core.util.Util;
import org.spin.datasource.Ds;
import org.spin.datasource.toolkit.DynamicDataSourceContextHolder;

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

    public static void using(String schema, ExceptionalHandler<Exception> handler) {
        using(schema, handler, null);
    }

    public static void using(String schema, ExceptionalHandler<Exception> handler, Consumer<Exception> exceptionConsumer) {
        String dataSource = schema;
        if (dataSource.indexOf('@') < 0) {
            if (dataSourceProvider != null) {
                dataSource = schema + "@" + Assert.notEmpty(dataSourceProvider.determinDataSource(schema), "无法确定Schema[" + schema + "]所属的数据源");
            } else if (null != DynamicDataSourceContextHolder.peek()) {
                dataSource = schema + "@" + DynamicDataSourceContextHolder.peek().getDatasource();
            } else {
                dataSource = schema + "@" + Ds.getPrimaryDataSource();
            }
        }
        if (null == handler) {
            return;
        }

        DynamicDataSourceContextHolder.push(dataSource);
        try {
            handler.handle();
        } catch (Exception e) {
            if (null != exceptionConsumer) {
                exceptionConsumer.accept(e);
            } else {
                logger.error("切换Schema时操作异常: ", e);
            }
        } finally {
            DynamicDataSourceContextHolder.poll();
        }
    }

    public static void setDataSourceProvider(SchemaDataSourceProvider dataSourceProvider) {
        Schema.dataSourceProvider = dataSourceProvider;
    }
}
