package org.spin.datasource.spring.boot.autoconfigure.druid;


import java.util.Objects;

/**
 * Druid日志配置
 *
 * @author Lhx
 */
public class DruidSlf4jConfig {

    private Boolean enable = true;

    private Boolean statementExecutableSqlLogEnable = false;

    public Boolean getEnable() {
        return enable;
    }

    public void setEnable(Boolean enable) {
        this.enable = enable;
    }

    public Boolean getStatementExecutableSqlLogEnable() {
        return statementExecutableSqlLogEnable;
    }

    public void setStatementExecutableSqlLogEnable(Boolean statementExecutableSqlLogEnable) {
        this.statementExecutableSqlLogEnable = statementExecutableSqlLogEnable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DruidSlf4jConfig)) return false;
        DruidSlf4jConfig that = (DruidSlf4jConfig) o;
        return Objects.equals(enable, that.enable) && Objects.equals(statementExecutableSqlLogEnable, that.statementExecutableSqlLogEnable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(enable, statementExecutableSqlLogEnable);
    }

    @Override
    public String toString() {
        return "DruidSlf4jConfig{" +
            "enable=" + enable +
            ", statementExecutableSqlLogEnable=" + statementExecutableSqlLogEnable +
            '}';
    }
}
