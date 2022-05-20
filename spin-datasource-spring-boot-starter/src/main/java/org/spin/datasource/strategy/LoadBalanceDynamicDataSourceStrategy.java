package org.spin.datasource.strategy;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * LoadBalance strategy to switch a database
 *
 * @author TaoYu Kanyuxia
 * @since 1.0.0
 */
public class LoadBalanceDynamicDataSourceStrategy implements DynamicDataSourceStrategy {

    /**
     * 负载均衡计数器
     */
    private final AtomicInteger index = new AtomicInteger(0);

    @Override
    public String determineDSKey(List<String> dsNames) {
        return dsNames.get(Math.abs(index.getAndAdd(1) % dsNames.size()));
    }
}
