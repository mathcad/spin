package org.spin.datasource.strategy;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Random strategy to switch a database
 *
 * @author TaoYu Kanyuxia
 * @since 1.0.0
 */
public class RandomDynamicDataSourceStrategy implements DynamicDataSourceStrategy {

    @Override
    public String determineDSKey(List<String> dsNames) {
        return dsNames.get(ThreadLocalRandom.current().nextInt(dsNames.size()));
    }
}
