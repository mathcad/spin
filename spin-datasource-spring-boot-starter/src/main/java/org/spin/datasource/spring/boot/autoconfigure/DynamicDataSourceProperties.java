package org.spin.datasource.spring.boot.autoconfigure;

import org.spin.datasource.enums.SeataMode;
import org.spin.datasource.spring.boot.autoconfigure.beecp.BeeCpConfig;
import org.spin.datasource.spring.boot.autoconfigure.dbcp2.Dbcp2Config;
import org.spin.datasource.spring.boot.autoconfigure.druid.DruidConfig;
import org.spin.datasource.spring.boot.autoconfigure.hikari.HikariCpConfig;
import org.spin.datasource.strategy.DynamicDataSourceStrategy;
import org.spin.datasource.strategy.LoadBalanceDynamicDataSourceStrategy;
import org.spin.datasource.toolkit.CryptoUtils;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.core.Ordered;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * DynamicDataSourceProperties
 *
 * @author TaoYu Kanyuxia
 * @see DataSourceProperties
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = DynamicDataSourceProperties.PREFIX)
public class DynamicDataSourceProperties {
    public static final String PREFIX = "spring.datasource.dynamic";
    public static final String HEALTH = PREFIX + ".health";
    public static final String DEFAULT_VALID_QUERY = "SELECT 1";
    /**
     * 主数据源,默认primary
     */
    private String primary = "primary";
    /**
     * 是否启用严格模式,默认不启用. 严格模式下未匹配到数据源直接报错, 非严格模式下则使用默认数据源primary所设置的数据源
     */
    private Boolean strict = false;
    /**
     * 是否使用p6spy输出，默认不输出
     */
    private Boolean p6spy = false;
    /**
     * 是否使用开启seata，默认不开启
     */
    private Boolean seata = false;
    /**
     * 是否懒加载数据源
     */
    private Boolean lazy = false;
    /**
     * seata使用模式，默认AT
     */
    private SeataMode seataMode = SeataMode.AT;
    /**
     * 是否使用 spring actuator 监控检查，默认不检查
     */
    private boolean health = false;
    /**
     * 监控检查SQL
     */
    private String healthValidQuery = DEFAULT_VALID_QUERY;
    /**
     * 数据源列表
     */
    private Map<String, DataSourceProperty> datasource = new LinkedHashMap<>();
    /**
     * 多数据源选择算法clazz，默认负载均衡算法
     */
    private Class<? extends DynamicDataSourceStrategy> strategy = LoadBalanceDynamicDataSourceStrategy.class;
    /**
     * aop切面顺序，默认优先级最高
     */
    private Integer order = Ordered.HIGHEST_PRECEDENCE;
    /**
     * Druid全局参数配置
     */
    @NestedConfigurationProperty
    private DruidConfig druid = new DruidConfig();
    /**
     * HikariCp全局参数配置
     */
    @NestedConfigurationProperty
    private HikariCpConfig hikari = new HikariCpConfig();
    /**
     * BeeCp全局参数配置
     */
    @NestedConfigurationProperty
    private BeeCpConfig beecp = new BeeCpConfig();
    /**
     * DBCP2全局参数配置
     */
    @NestedConfigurationProperty
    private Dbcp2Config dbcp2 = new Dbcp2Config();
    /**
     * 全局默认publicKey
     */
    private String publicKey = CryptoUtils.DEFAULT_PUBLIC_KEY_STRING;
    /**
     * aop 切面是否只允许切 public 方法
     */
    private boolean allowedPublicOnly = true;

    public String getPrimary() {
        return primary;
    }

    public void setPrimary(String primary) {
        this.primary = primary;
    }

    public Boolean getStrict() {
        return strict;
    }

    public void setStrict(Boolean strict) {
        this.strict = strict;
    }

    public Boolean getP6spy() {
        return p6spy;
    }

    public void setP6spy(Boolean p6spy) {
        this.p6spy = p6spy;
    }

    public Boolean getSeata() {
        return seata;
    }

    public void setSeata(Boolean seata) {
        this.seata = seata;
    }

    public Boolean getLazy() {
        return lazy;
    }

    public void setLazy(Boolean lazy) {
        this.lazy = lazy;
    }

    public SeataMode getSeataMode() {
        return seataMode;
    }

    public void setSeataMode(SeataMode seataMode) {
        this.seataMode = seataMode;
    }

    public boolean isHealth() {
        return health;
    }

    public void setHealth(boolean health) {
        this.health = health;
    }

    public String getHealthValidQuery() {
        return healthValidQuery;
    }

    public void setHealthValidQuery(String healthValidQuery) {
        this.healthValidQuery = healthValidQuery;
    }

    public Map<String, DataSourceProperty> getDatasource() {
        return datasource;
    }

    public void setDatasource(Map<String, DataSourceProperty> datasource) {
        this.datasource = datasource;
    }

    public Class<? extends DynamicDataSourceStrategy> getStrategy() {
        return strategy;
    }

    public void setStrategy(Class<? extends DynamicDataSourceStrategy> strategy) {
        this.strategy = strategy;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public DruidConfig getDruid() {
        return druid;
    }

    public void setDruid(DruidConfig druid) {
        this.druid = druid;
    }

    public HikariCpConfig getHikari() {
        return hikari;
    }

    public void setHikari(HikariCpConfig hikari) {
        this.hikari = hikari;
    }

    public BeeCpConfig getBeecp() {
        return beecp;
    }

    public void setBeecp(BeeCpConfig beecp) {
        this.beecp = beecp;
    }

    public Dbcp2Config getDbcp2() {
        return dbcp2;
    }

    public void setDbcp2(Dbcp2Config dbcp2) {
        this.dbcp2 = dbcp2;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public boolean isAllowedPublicOnly() {
        return allowedPublicOnly;
    }

    public void setAllowedPublicOnly(boolean allowedPublicOnly) {
        this.allowedPublicOnly = allowedPublicOnly;
    }
}
