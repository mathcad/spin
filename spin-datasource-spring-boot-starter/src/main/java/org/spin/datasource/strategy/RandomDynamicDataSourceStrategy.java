package org.spin.datasource.strategy;

import javax.sql.DataSource;
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
    public DataSource determineDataSource(List<DataSource> dataSources) {
        return dataSources.get(ThreadLocalRandom.current().nextInt(dataSources.size()));
    }
}
