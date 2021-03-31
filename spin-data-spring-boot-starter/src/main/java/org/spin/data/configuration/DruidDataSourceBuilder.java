package org.spin.data.configuration;

import com.alibaba.druid.filter.Filter;
import com.alibaba.druid.filter.config.ConfigFilter;
import com.alibaba.druid.filter.encoding.EncodingConvertFilter;
import com.alibaba.druid.filter.logging.CommonsLogFilter;
import com.alibaba.druid.filter.logging.Log4j2Filter;
import com.alibaba.druid.filter.logging.Log4jFilter;
import com.alibaba.druid.filter.logging.Slf4jLogFilter;
import com.alibaba.druid.filter.stat.StatFilter;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.wall.WallConfig;
import com.alibaba.druid.wall.WallFilter;
import org.spin.data.property.DruidDataSourceProperties;
import org.spin.data.property.MultiDruidDataSourceProperties;
import org.spin.data.DataSourceBuilder;
import org.spin.core.util.BeanUtils;
import org.spin.data.core.DataSourceContext;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import javax.sql.DataSource;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 * Druid数据源构建器
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/6/7</p>
 *
 * @author xuweinan
 * @version 1.0
 */
@AutoConfigureBefore(value = {DataSourceAutoConfiguration.class})
@EnableConfigurationProperties({MultiDruidDataSourceProperties.class})
public class DruidDataSourceBuilder implements DataSourceBuilder<DruidDataSource, DruidDataSourceProperties> {

    @Override
    public DataSource buildAtomikosDataSource(DefaultListableBeanFactory acf, DruidDataSourceProperties dbConfig) {
        String beanName = dbConfig.getName() + "AtomikosDataSource";

        BeanDefinitionBuilder bdb = BeanDefinitionBuilder.rootBeanDefinition(com.atomikos.jdbc.AtomikosDataSourceBean.class);
        bdb.addPropertyValue("xaDataSourceClassName", dbConfig.getXaDataSourceClassName());
        bdb.addPropertyValue("uniqueResourceName", dbConfig.getName());
        bdb.addPropertyValue("maxPoolSize", dbConfig.getMaxPoolSize());
        bdb.addPropertyValue("minPoolSize", dbConfig.getMinPoolSize());
        Properties properties = dbConfig.toProperties(null);
        if (properties.containsKey("connectProperties") && properties.get("connectProperties") instanceof CharSequence) {
            String connectProperties = properties.getProperty("connectProperties");
            Properties props = new Properties();
            String[] entries = connectProperties.split(";");
            for (String entry : entries) {
                if (entry.length() > 0) {
                    int index = entry.indexOf('=');
                    if (index > 0) {
                        String n = entry.substring(0, index);
                        String value = entry.substring(index + 1);
                        props.setProperty(n, value);
                    } else {
                        properties.setProperty(entry, "");
                    }
                }
            }
            properties.put("connectProperties", props);
        }

        bdb.addPropertyValue("xaProperties", properties);
        bdb.setInitMethodName("init");
        bdb.setDestroyMethodName("close");

        acf.registerBeanDefinition(beanName, bdb.getBeanDefinition());
        com.atomikos.jdbc.AtomikosDataSourceBean ds = (com.atomikos.jdbc.AtomikosDataSourceBean) acf.getBean(beanName);
        DataSourceContext.registDataSource(dbConfig.getName(), ds);
        DruidDataSource druidDataSource = BeanUtils.getFieldValue(ds, "xaDataSource");
        DruidDataSourceProperties.FilterProperties filter = dbConfig.getFilter();
        addFilter(druidDataSource, filter);
        return ds;
    }

    @Override
    public DruidDataSource buildSingletonDatasource(DefaultListableBeanFactory acf, DruidDataSourceProperties dbConfig) {
        String beanName = dbConfig.getName() + "DataSource";
        System.getProperties().putAll(dbConfig.toProperties("druid."));
        DruidDataSource dataSource = new DruidDataSource();
        DruidDataSourceProperties.FilterProperties filter = dbConfig.getFilter();
        addFilter(dataSource, filter);
        BeanUtils.copyTo(dbConfig, dataSource, "testWhileIdle",
            "testOnBorrow",
            "validationQuery",
            "useGlobalDataSourceStat",
            "asyncInit",
            "timeBetweenLogStatsMillis",
            "clearFiltersEnable",
            "resetStatEnable",
            "notFullTimeoutRetryCount",
            "timeBetweenEvictionRunsMillis",
            "maxWaitThreadCount",
            "failFast",
            "phyTimeoutMillis",
            "phyMaxUseCount",
            "minEvictableIdleTimeMillis",
            "maxEvictableIdleTimeMillis",
            "keepAlive",
            "poolPreparedStatements",
            "initVariants",
            "initGlobalVariants",
            "useUnfairLock",
            "initialSize",
            "minIdle",
            "maxActive",
            "killWhenSocketReadTimeout",
            "maxPoolPreparedStatementPerConnectionSize",
            "maxWait",
            "removeAbandoned",
            "removeAbandonedTimeoutMillis");

        DataSourceContext.registDataSource(dbConfig.getName(), dataSource);
        acf.registerSingleton(beanName, dataSource);
        return acf.getBean(DruidDataSource.class);
    }

    private void addFilter(DruidDataSource dataSource, DruidDataSourceProperties.FilterProperties filter) {
        if (null != filter) {
            List<Filter> filterList = new LinkedList<>();
            if (filter.isStat()) {
                filterList.add(new StatFilter());
            }
            if (filter.isConfig()) {
                filterList.add(new ConfigFilter());
            }
            if (filter.isEncoding()) {
                filterList.add(new EncodingConvertFilter());
            }
            if (filter.isSlf4j()) {
                filterList.add(new Slf4jLogFilter());
            }
            if (filter.isLog4j()) {
                filterList.add(new Log4jFilter());
            }
            if (filter.isLog4j2()) {
                filterList.add(new Log4j2Filter());
            }
            if (filter.isCommonsLog()) {
                filterList.add(new CommonsLogFilter());
            }
            if (filter.isWall()) {
                WallFilter wallFilter = new WallFilter();
                wallFilter.setConfig(new WallConfig());
                filterList.add(wallFilter);
            }
            List<Filter> dsFilters = BeanUtils.getFieldValue(dataSource, "filters");
            dsFilters.addAll(filterList);
        }
    }
}
