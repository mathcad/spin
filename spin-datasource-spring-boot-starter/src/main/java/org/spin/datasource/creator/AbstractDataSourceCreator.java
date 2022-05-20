package org.spin.datasource.creator;

import com.p6spy.engine.spy.P6DataSource;
import io.seata.rm.datasource.DataSourceProxy;
import io.seata.rm.datasource.xa.DataSourceProxyXA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.util.StringUtils;
import org.spin.datasource.ds.ItemDataSource;
import org.spin.datasource.enums.SeataMode;
import org.spin.datasource.spring.boot.autoconfigure.DataSourceProperty;
import org.spin.datasource.spring.boot.autoconfigure.DynamicDataSourceProperties;
import org.spin.datasource.support.ScriptRunner;

import javax.sql.DataSource;

/**
 * 默认创建数据源无参的调用有参的
 *
 * @author ls9527
 */
public abstract class AbstractDataSourceCreator {
    private static final Logger logger = LoggerFactory.getLogger(AbstractDataSourceCreator.class);

    protected final DynamicDataSourceProperties dynamicDataSourceProperties;

    protected AbstractDataSourceCreator(DynamicDataSourceProperties dynamicDataSourceProperties) {
        this.dynamicDataSourceProperties = dynamicDataSourceProperties;
    }

    public abstract DataSource doCreateDataSource(DataSourceProperty dataSourceProperty);

    public DataSource createDataSource(DataSourceProperty dataSourceProperty) {
        String publicKey = dataSourceProperty.getPublicKey();
        if (StringUtils.isEmpty(publicKey)) {
            publicKey = dynamicDataSourceProperties.getPublicKey();
            dataSourceProperty.setPublicKey(publicKey);
        }

        Boolean lazy = dataSourceProperty.getLazy();
        if (lazy == null) {
            lazy = dynamicDataSourceProperties.getLazy();
            dataSourceProperty.setLazy(lazy);
        }
        DataSource dataSource = doCreateDataSource(dataSourceProperty);
        this.runScrip(dataSource, dataSourceProperty);
        return wrapDataSource(dataSource, dataSourceProperty);
    }

    private void runScrip(DataSource dataSource, DataSourceProperty dataSourceProperty) {
        String schema = dataSourceProperty.getSchema();
        String data = dataSourceProperty.getData();
        if (StringUtils.isNotBlank(schema) || StringUtils.isNotBlank(data)) {
            ScriptRunner scriptRunner = new ScriptRunner(dataSourceProperty.isContinueOnError(), dataSourceProperty.getSeparator());
            if (StringUtils.isNotBlank(schema)) {
                scriptRunner.runScript(dataSource, schema);
            }
            if (StringUtils.isNotBlank(data)) {
                scriptRunner.runScript(dataSource, data);
            }
        }
    }

    private DataSource wrapDataSource(DataSource dataSource, DataSourceProperty dataSourceProperty) {
        String name = dataSourceProperty.getPoolName();
        DataSource targetDataSource = dataSource;

        Boolean enabledP6spy = dynamicDataSourceProperties.getP6spy() && dataSourceProperty.getP6spy();
        if (enabledP6spy) {
            targetDataSource = new P6DataSource(dataSource);
            logger.debug("dynamic-datasource [{}] wrap p6spy plugin", name);
        }

        Boolean enabledSeata = dynamicDataSourceProperties.getSeata() && dataSourceProperty.getSeata();
        SeataMode seataMode = dynamicDataSourceProperties.getSeataMode();
        if (enabledSeata) {
            if (SeataMode.XA == seataMode) {
                targetDataSource = new DataSourceProxyXA(dataSource);
            } else {
                targetDataSource = new DataSourceProxy(dataSource);
            }
            logger.debug("dynamic-datasource [{}] wrap seata plugin transaction mode ", name);
        }
        return new ItemDataSource(name, dataSource, targetDataSource, enabledP6spy, enabledSeata, seataMode);
    }
}
