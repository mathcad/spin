package org.spin.datasource.support;

/**
 * 动态数据源常量
 *
 * @author jobob
 * @since 2019-10-08
 */
public interface DdConstants {

    /**
     * 数据源：主库
     */
    String PRIMARY = "primary";
    /**
     * 数据源：从库
     */
    String REPLICA = "replica";

    /**
     * DRUID数据源类
     */
    String DRUID_DATASOURCE = "com.alibaba.druid.pool.DruidDataSource";
    /**
     * HikariCp数据源
     */
    String HIKARI_DATASOURCE = "com.zaxxer.hikari.HikariDataSource";
    /**
     * BeeCp数据源
     */
    String BEECP_DATASOURCE = "cn.beecp.BeeDataSource";
    /**
     * DBCP2数据源
     */
    String DBCP2_DATASOURCE = "org.apache.commons.dbcp2.BasicDataSource";
}
