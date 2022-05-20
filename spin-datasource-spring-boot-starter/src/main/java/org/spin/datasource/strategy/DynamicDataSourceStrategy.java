package org.spin.datasource.strategy;

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
     * @param dsNames given dataSources
     * @return final dataSource
     */
    String determineDSKey(List<String> dsNames);
}
