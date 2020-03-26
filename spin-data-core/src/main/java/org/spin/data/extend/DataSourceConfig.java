package org.spin.data.extend;

import org.spin.core.util.StringUtils;

import java.util.Properties;

/**
 * 数据源配置定义
 * <p>Created by xuweinan on 2017/11/29.</p>
 *
 * @author xuweinan
 */
public interface DataSourceConfig {

    /**
     * 获取数据源名称
     *
     * @return 数据源名称
     */
    String getName();

    /**
     * 设置数据源名称
     *
     * @param name 数据源名称
     */
    void setName(String name);

    /**
     * 获取数据库连接字符串
     *
     * @return 连接字符串
     */
    String getUrl();

    /**
     * 设置数据库连接字符串
     *
     * @param url 连接字符串
     */
    void setUrl(String url);

    /**
     * 获取数据库驱动类完整名称
     *
     * @return 驱动类名称
     */
    String getDriverClassName();

    /**
     * 设置数据库驱动类完整名称
     *
     * @param driverClassName 驱动类名称
     */
    void setDriverClassName(String driverClassName);


    /**
     * 获取数据库连接用户名
     *
     * @return 用户名
     */
    String getUsername();

    /**
     * 设置数据库连接用户名
     *
     * @param username 用户名
     */
    void setUsername(String username);

    /**
     * 获取数据库连接密码
     *
     * @return 密码
     */
    String getPassword();

    /**
     * 获取数据库连接密码
     *
     * @param password 密码
     */
    void setPassword(String password);

    /**
     * 获取连接池最大连接数
     *
     * @return 最大连接数
     */
    int getMaxPoolSize();

    /**
     * 获取连接池最小连接数
     *
     * @return 最小连接数
     */
    int getMinPoolSize();

    /**
     * 获取XA数据源完整类名
     *
     * @return XA数据源类名
     */
    String getXaDataSourceClassName();

    /**
     * 获取数据源完整类名
     *
     * @return 数据源类名
     */
    String getDataSourceClassName();

    /**
     * 将当前配置输出为Properties文件
     *
     * @param prefix 前缀
     * @return 配置对应的properties文件
     */
    Properties toProperties(String prefix);

    /**
     * 获取当前数据源对应的DBMS供应商
     *
     * @return 供应商名称
     */
    default String getVenderName() {
        String vender = getUrl().split(":")[1].toUpperCase();
        return StringUtils.isEmpty(vender) ? "UNKNOW-[" + getUrl() + "]" : vender;
    }

    /**
     * 将键/值存入指定的Properties对象
     *
     * @param properties 目的Properties对象
     * @param prefix     配置前缀(可以为空)
     * @param key        键
     * @param value      值
     */
    default void notNullAdd(Properties properties, String prefix, String key, Object value) {
        if (value != null) {
            properties.setProperty(StringUtils.trimToEmpty(prefix) + key, value.toString());
        }
    }

    /**
     * 是否支持XA数据源
     *
     * @return 默认为true
     */
    default boolean supportXa() {
        return true;
    }
}
