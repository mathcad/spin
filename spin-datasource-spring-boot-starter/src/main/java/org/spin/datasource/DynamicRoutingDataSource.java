package org.spin.datasource;

import com.p6spy.engine.spy.P6DataSource;
import io.seata.rm.datasource.DataSourceProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.util.StringUtils;
import org.spin.datasource.ds.AbstractRoutingDataSource;
import org.spin.datasource.ds.GroupDataSource;
import org.spin.datasource.ds.ItemDataSource;
import org.spin.datasource.provider.DynamicDataSourceProvider;
import org.spin.datasource.strategy.DynamicDataSourceStrategy;
import org.spin.datasource.strategy.LoadBalanceDynamicDataSourceStrategy;
import org.spin.datasource.toolkit.DynamicDataSourceContextHolder;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 核心动态数据源组件
 *
 * @author TaoYu Kanyuxia
 * @since 1.0.0
 */
public class DynamicRoutingDataSource extends AbstractRoutingDataSource implements InitializingBean, DisposableBean {

    private static final Logger logger = LoggerFactory.getLogger(DynamicRoutingDataSource.class);

    private static final String UNDERLINE = "_";
    /**
     * 所有数据库
     */
    private final Map<String, DataSource> dataSourceMap = new ConcurrentHashMap<>();
    /**
     * 分组数据库
     */
    private final Map<String, GroupDataSource> groupDataSources = new ConcurrentHashMap<>();
    private DynamicDataSourceProvider provider;
    private Class<? extends DynamicDataSourceStrategy> strategy = LoadBalanceDynamicDataSourceStrategy.class;
    private String primary = "primary";
    private Boolean strict = false;
    private Boolean p6spy = false;
    private Boolean seata = false;

    @Override
    public DataSource determineDataSource() {
        CurrentDatasourceInfo datasourceInfo = DynamicDataSourceContextHolder.peek();
        return getDataSource(null == datasourceInfo ? null : datasourceInfo.getDatasource());
    }

    private DataSource determinePrimaryDataSource() {
        logger.debug("dynamic-datasource switch to the primary datasource");
        return groupDataSources.containsKey(primary) ? groupDataSources.get(primary).determineDataSource() : dataSourceMap.get(primary);
    }

    /**
     * 获取当前所有的数据源
     *
     * @return 当前所有数据源
     */
    public Map<String, DataSource> getCurrentDataSources() {
        return dataSourceMap;
    }

    /**
     * 获取的当前所有的分组数据源
     *
     * @return 当前所有的分组数据源
     */
    public Map<String, GroupDataSource> getCurrentGroupDataSources() {
        return groupDataSources;
    }

    /**
     * 获取数据源
     *
     * @param ds 数据源名称
     * @return 数据源
     */
    public DataSource getDataSource(String ds) {
        if (StringUtils.isEmpty(ds)) {
            return determinePrimaryDataSource();
        } else if (!groupDataSources.isEmpty() && groupDataSources.containsKey(ds)) {
            logger.debug("dynamic-datasource switch to the datasource named [{}]", ds);
            return groupDataSources.get(ds).determineDataSource();
        } else if (dataSourceMap.containsKey(ds)) {
            logger.debug("dynamic-datasource switch to the datasource named [{}]", ds);
            return dataSourceMap.get(ds);
        }
        if (strict) {
            throw new RuntimeException("dynamic-datasource could not find a datasource named" + ds);
        }
        return determinePrimaryDataSource();
    }

    /**
     * 添加数据源
     *
     * @param ds         数据源名称
     * @param dataSource 数据源
     */
    public synchronized void addDataSource(String ds, DataSource dataSource) {
        DataSource oldDataSource = dataSourceMap.put(ds, dataSource);
        // 新数据源添加到分组
        this.addGroupDataSource(ds, dataSource);
        // 关闭老的数据源
        if (oldDataSource != null) {
            try {
                closeDataSource(oldDataSource);
            } catch (Exception e) {
                logger.error("dynamic-datasource - remove the database named [{}]  failed", ds, e);
            }
        }

        logger.info("dynamic-datasource - load a datasource named [{}] success", ds);
    }

    /**
     * 新数据源添加到分组
     *
     * @param ds         新数据源的名字
     * @param dataSource 新数据源
     */
    private void addGroupDataSource(String ds, DataSource dataSource) {
        if (ds.contains(UNDERLINE)) {
            String group = ds.split(UNDERLINE)[0];
            GroupDataSource groupDataSource = groupDataSources.get(group);
            if (groupDataSource == null) {
                try {
                    groupDataSource = new GroupDataSource(group, strategy.getDeclaredConstructor().newInstance());
                    groupDataSources.put(group, groupDataSource);
                } catch (Exception e) {
                    throw new RuntimeException("dynamic-datasource - add the datasource named " + ds + " error", e);
                }
            }
            groupDataSource.addDatasource(ds, dataSource);
        }
    }

    /**
     * 删除数据源
     *
     * @param ds 数据源名称
     */
    public synchronized void removeDataSource(String ds) {
        if (StringUtils.isBlank(ds)) {
            throw new RuntimeException("remove parameter could not be empty");
        }
        if (primary.equals(ds)) {
            throw new RuntimeException("could not remove primary datasource");
        }
        if (dataSourceMap.containsKey(ds)) {
            DataSource dataSource = dataSourceMap.remove(ds);
            try {
                closeDataSource(dataSource);
            } catch (Exception e) {
                logger.error("dynamic-datasource - remove the database named [{}]  failed", ds, e);
            }

            if (ds.contains(UNDERLINE)) {
                String group = ds.split(UNDERLINE)[0];
                if (groupDataSources.containsKey(group)) {
                    DataSource oldDataSource = groupDataSources.get(group).removeDatasource(ds);
                    if (oldDataSource == null) {
                        if (logger.isWarnEnabled()) {
                            logger.warn("fail for remove datasource from group. dataSource: {} ,group: {}", ds, group);
                        }
                    }
                }
            }
            logger.info("dynamic-datasource - remove the database named [{}] success", ds);
        } else {
            logger.warn("dynamic-datasource - could not find a database named [{}]", ds);
        }
    }

    /**
     * 关闭数据源。
     * <pre>
     *    从3.2.0开启，如果是原生或使用 DataSourceCreator 创建的数据源会包装成ItemDataSource。
     *    ItemDataSource保留了最原始的数据源，其可直接关闭。
     *    如果不是DataSourceCreator创建的数据源则只有尝试解包装再关闭。
     * </pre>
     */
    private void closeDataSource(DataSource dataSource) throws Exception {
        if (dataSource instanceof ItemDataSource) {
            ((ItemDataSource) dataSource).close();
        } else {
            if (seata && dataSource instanceof DataSourceProxy) {
                DataSourceProxy dataSourceProxy = (DataSourceProxy) dataSource;
                dataSource = dataSourceProxy.getTargetDataSource();
            }
            if (p6spy && dataSource instanceof P6DataSource) {
                Field realDataSourceField = P6DataSource.class.getDeclaredField("realDataSource");
                realDataSourceField.setAccessible(true);
                dataSource = (DataSource) realDataSourceField.get(dataSource);
            }
            Class<? extends DataSource> clazz = dataSource.getClass();
            Method closeMethod = clazz.getDeclaredMethod("close");
            closeMethod.invoke(dataSource);
        }
    }

    @Override
    public void destroy() throws Exception {
        logger.info("dynamic-datasource start closing ....");
        for (Map.Entry<String, DataSource> item : dataSourceMap.entrySet()) {
            closeDataSource(item.getValue());
        }
        logger.info("dynamic-datasource all closed success,bye");
    }

    @Override
    public void afterPropertiesSet() {
        // 检查开启了配置但没有相关依赖
        checkEnv();
        // 添加并分组数据源
        Map<String, DataSource> dataSources = provider.loadDataSources();
        for (Map.Entry<String, DataSource> dsItem : dataSources.entrySet()) {
            addDataSource(dsItem.getKey(), dsItem.getValue());
        }
        // 检测默认数据源是否设置
        if (groupDataSources.containsKey(primary)) {
            logger.info("dynamic-datasource initial loaded [{}] datasource,primary group datasource named [{}]", dataSources.size(), primary);
        } else if (dataSourceMap.containsKey(primary)) {
            logger.info("dynamic-datasource initial loaded [{}] datasource,primary datasource named [{}]", dataSources.size(), primary);
        } else {
            throw new RuntimeException("dynamic-datasource Please check the setting of primary");
        }
    }

    public void setProvider(DynamicDataSourceProvider provider) {
        this.provider = provider;
    }

    public void setStrategy(Class<? extends DynamicDataSourceStrategy> strategy) {
        this.strategy = strategy;
    }

    public void setPrimary(String primary) {
        this.primary = primary;
    }

    public void setStrict(Boolean strict) {
        this.strict = strict;
    }

    public void setP6spy(Boolean p6spy) {
        this.p6spy = p6spy;
    }

    public void setSeata(Boolean seata) {
        this.seata = seata;
    }

    private void checkEnv() {
        if (p6spy) {
            try {
                Class.forName("com.p6spy.engine.spy.P6DataSource");
                logger.info("dynamic-datasource detect P6SPY plugin and enabled it");
            } catch (Exception e) {
                throw new RuntimeException("dynamic-datasource enabled P6SPY ,however without p6spy dependency", e);
            }
        }
        if (seata) {
            try {
                Class.forName("io.seata.rm.datasource.DataSourceProxy");
                logger.info("dynamic-datasource detect ALIBABA SEATA and enabled it");
            } catch (Exception e) {
                throw new RuntimeException("dynamic-datasource enabled ALIBABA SEATA,however without seata dependency", e);
            }
        }
    }
}
