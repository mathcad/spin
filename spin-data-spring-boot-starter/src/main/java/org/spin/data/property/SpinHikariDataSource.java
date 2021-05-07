package org.spin.data.property;

import com.zaxxer.hikari.HikariDataSource;
import org.spin.data.extend.DataSourceConfig;

import java.util.Properties;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2020/3/24</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class SpinHikariDataSource extends HikariDataSource implements DataSourceConfig {

    @Override
    public String getName() {
        return getPoolName();
    }

    @Override
    public void setName(String name) {
        setPoolName(name);
    }

    @Override
    public String getUrl() {
        return getJdbcUrl();
    }

    @Override
    public void setUrl(String url) {
        setJdbcUrl(url);
    }

    @Override
    public int getMaxPoolSize() {
        return getMaximumPoolSize();
    }

    @Override
    public int getMinPoolSize() {
        return getMinimumIdle();
    }

    @Override
    public String getXaDataSourceClassName() {
        throw new UnsupportedOperationException("Hikari不支持XA数据源");
    }

    @Override
    public Properties toProperties(String prefix) {
        throw new UnsupportedOperationException("Hikari不支持该操作");
    }

    @Override
    public boolean supportXa() {
        return false;
    }
}
