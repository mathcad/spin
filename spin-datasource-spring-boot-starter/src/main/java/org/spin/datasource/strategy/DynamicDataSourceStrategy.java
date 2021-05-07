package org.spin.datasource.strategy;

import javax.sql.DataSource;
import java.util.List;

/**
 * The interface of dynamic datasource switch strategy
 *
 * @author TaoYu Kanyuxia
 * @see RandomDynamicDataSourceStrategy
 * @see LoadBalanceDynamicDataSourceStrategy
 * @since 1.0.0
 */
public interface DynamicDataSourceStrategy {

    /**
     * determine a database from the given dataSources
     *
     * @param dataSources given dataSources
     * @return final dataSource
     */
    DataSource determineDataSource(List<DataSource> dataSources);
}
