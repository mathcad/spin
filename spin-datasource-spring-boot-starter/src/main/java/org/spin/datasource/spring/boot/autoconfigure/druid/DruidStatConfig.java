package org.spin.datasource.spring.boot.autoconfigure.druid;


import java.util.Objects;

/**
 * Druid监控配置
 *
 * @author TaoYu
 */
public class DruidStatConfig {

    private Long slowSqlMillis;

    private Boolean logSlowSql;

    private Boolean mergeSql;

    public Long getSlowSqlMillis() {
        return slowSqlMillis;
    }

    public void setSlowSqlMillis(Long slowSqlMillis) {
        this.slowSqlMillis = slowSqlMillis;
    }

    public Boolean getLogSlowSql() {
        return logSlowSql;
    }

    public void setLogSlowSql(Boolean logSlowSql) {
        this.logSlowSql = logSlowSql;
    }

    public Boolean getMergeSql() {
        return mergeSql;
    }

    public void setMergeSql(Boolean mergeSql) {
        this.mergeSql = mergeSql;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DruidStatConfig)) return false;
        DruidStatConfig that = (DruidStatConfig) o;
        return Objects.equals(slowSqlMillis, that.slowSqlMillis) && Objects.equals(logSlowSql, that.logSlowSql) && Objects.equals(mergeSql, that.mergeSql);
    }

    @Override
    public int hashCode() {
        return Objects.hash(slowSqlMillis, logSlowSql, mergeSql);
    }

    @Override
    public String toString() {
        return "DruidStatConfig{" +
            "slowSqlMillis=" + slowSqlMillis +
            ", logSlowSql=" + logSlowSql +
            ", mergeSql=" + mergeSql +
            '}';
    }
}
