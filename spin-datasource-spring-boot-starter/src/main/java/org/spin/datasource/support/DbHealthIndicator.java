package org.spin.datasource.support;

import org.spin.datasource.DynamicRoutingDataSource;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.IncorrectResultSetColumnCountException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.JdbcUtils;

import javax.sql.DataSource;
import java.sql.ResultSetMetaData;
import java.util.List;
import java.util.Map;

/**
 * 数据库健康状况指标
 *
 * @author hubin
 */
public class DbHealthIndicator extends AbstractHealthIndicator {

    private final String validQuery;

    private final HealthCheckAdapter healthCheckAdapter;
    /**
     * 当前执行数据源
     */
    private final DataSource dataSource;

    public DbHealthIndicator(DataSource dataSource, String validQuery, HealthCheckAdapter healthCheckAdapter) {
        this.dataSource = dataSource;
        this.validQuery = validQuery;
        this.healthCheckAdapter = healthCheckAdapter;
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) {
        if (dataSource instanceof DynamicRoutingDataSource) {
            Map<String, DataSource> dataSourceMap = ((DynamicRoutingDataSource) dataSource).getCurrentDataSources();
            // 循环检查当前数据源是否可用
            Boolean available = null;
            Boolean disable = null;
            for (Map.Entry<String, DataSource> dataSource : dataSourceMap.entrySet()) {
                Boolean resultAvailable = false;
                try {
                    resultAvailable = queryAvailable(dataSource.getValue());
                } catch (Throwable ignore) {
                } finally {
                    healthCheckAdapter.putHealth(dataSource.getKey(), resultAvailable);
                    builder.withDetail(dataSource.getKey(), resultAvailable);

                    if (resultAvailable) {
                        available = true;
                    } else {
                        disable = true;
                    }
                }
            }
            if (available != null) {
                if (disable != null) {
                    builder.status(Status.OUT_OF_SERVICE);
                } else {
                    builder.status(Status.UP);
                }
            } else {
                builder.status(Status.DOWN);
            }

        }
    }

    private Boolean queryAvailable(DataSource dataSource) {
        List<Integer> results = new JdbcTemplate(dataSource).query(this.validQuery, (resultSet, i) -> {
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columns = metaData.getColumnCount();
            if (columns != 1) {
                throw new IncorrectResultSetColumnCountException(1, columns);
            }
            return (Integer) JdbcUtils.getResultSetValue(resultSet, 1, Integer.class);
        });
        return DataAccessUtils.requiredSingleResult(results) == 1;
    }
}
