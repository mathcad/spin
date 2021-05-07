package org.spin.datasource.creator;

import com.p6spy.engine.spy.P6DataSource;
import io.seata.rm.datasource.DataSourceProxy;
import io.seata.rm.datasource.xa.DataSourceProxyXA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.datasource.ds.ItemDataSource;
import org.spin.datasource.enums.SeataMode;
import org.spin.datasource.spring.boot.autoconfigure.DataSourceProperty;
import org.spin.datasource.spring.boot.autoconfigure.DynamicDataSourceProperties;
import org.spin.datasource.support.ScriptRunner;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.util.List;

/**
 * 数据源创建器
 *
 * @author TaoYu
 * @since 2.3.0
 */
public class DefaultDataSourceCreator implements DataSourceCreator {
    private static final Logger logger = LoggerFactory.getLogger(DefaultDataSourceCreator.class);

    private DynamicDataSourceProperties properties;
    private List<DataSourceCreator> creators;

    @Override
    public DataSource createDataSource(DataSourceProperty dataSourceProperty) {
        return createDataSource(dataSourceProperty, properties.getPublicKey());
    }

    @Override
    public DataSource createDataSource(DataSourceProperty dataSourceProperty, String publicKey) {
        DataSourceCreator dataSourceCreator = null;
        for (DataSourceCreator creator : this.creators) {
            if (creator.support(dataSourceProperty)) {
                dataSourceCreator = creator;
                break;
            }
        }
        if (dataSourceCreator == null) {
            throw new IllegalStateException("creator must not be null,please check the DataSourceCreator");
        }
        DataSource dataSource = dataSourceCreator.createDataSource(dataSourceProperty, publicKey);
        this.runScrip(dataSource, dataSourceProperty);
        return wrapDataSource(dataSource, dataSourceProperty);
    }

    public void setProperties(DynamicDataSourceProperties properties) {
        this.properties = properties;
    }

    public void setCreators(List<DataSourceCreator> creators) {
        this.creators = creators;
    }

    private void runScrip(DataSource dataSource, DataSourceProperty dataSourceProperty) {
        String schema = dataSourceProperty.getSchema();
        String data = dataSourceProperty.getData();
        if (StringUtils.hasText(schema) || StringUtils.hasText(data)) {
            ScriptRunner scriptRunner = new ScriptRunner(dataSourceProperty.isContinueOnError(), dataSourceProperty.getSeparator());
            if (StringUtils.hasText(schema)) {
                scriptRunner.runScript(dataSource, schema);
            }
            if (StringUtils.hasText(data)) {
                scriptRunner.runScript(dataSource, data);
            }
        }
    }

    private DataSource wrapDataSource(DataSource dataSource, DataSourceProperty dataSourceProperty) {
        String name = dataSourceProperty.getPoolName();
        DataSource targetDataSource = dataSource;

        Boolean enabledP6spy = properties.getP6spy() && dataSourceProperty.getP6spy();
        if (enabledP6spy) {
            targetDataSource = new P6DataSource(dataSource);
            logger.debug("dynamic-datasource [{}] wrap p6spy plugin", name);
        }

        Boolean enabledSeata = properties.getSeata() && dataSourceProperty.getSeata();
        SeataMode seataMode = properties.getSeataMode();
        if (enabledSeata) {
            if (SeataMode.XA == seataMode) {
                targetDataSource = new DataSourceProxyXA(dataSource);
            } else {
                targetDataSource = new DataSourceProxy(dataSource);
            }
            logger.debug("dynamic-datasource [{}] wrap seata plugin transaction mode [{}]", name, seataMode);
        }
        return new ItemDataSource(name, dataSource, targetDataSource, enabledP6spy, enabledSeata, seataMode);
    }

    public void setDataSourceCreators(List<DataSourceCreator> dataSourceCreator) {
        this.creators = dataSourceCreator;
    }

    @Override
    public boolean support(DataSourceProperty dataSourceProperty) {
        return true;
    }
}
