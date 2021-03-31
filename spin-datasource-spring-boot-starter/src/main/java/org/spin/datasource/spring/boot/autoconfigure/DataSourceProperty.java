package org.spin.datasource.spring.boot.autoconfigure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.util.StringUtils;
import org.spin.datasource.spring.boot.autoconfigure.druid.DruidConfig;
import org.spin.datasource.spring.boot.autoconfigure.hikari.HikariCpConfig;
import org.spin.datasource.toolkit.CryptoUtils;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import javax.sql.DataSource;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author TaoYu
 * @since 1.2.0
 */
public class DataSourceProperty {
    private static final Logger logger = LoggerFactory.getLogger(DataSourceProperty.class);

    /**
     * 加密正则
     */
    private static final Pattern ENC_PATTERN = Pattern.compile("^ENC\\((.*)\\)$");

    /**
     * 连接池名称(只是一个名称标识)</br> 默认是配置文件上的名称
     */
    private String poolName;
    /**
     * 连接池类型，如果不设置自动查找 Druid > HikariCp
     */
    private Class<? extends DataSource> type;
    /**
     * JDBC driver
     */
    private String driverClassName;
    /**
     * JDBC url 地址
     */
    private String url;
    /**
     * JDBC 用户名
     */
    private String username;
    /**
     * JDBC 密码
     */
    private String password;
    /**
     * 默认Catalog
     */
    private String defaultCatalog;
    /**
     * jndi数据源名称(设置即表示启用)
     */
    private String jndiName;
    /**
     * 自动运行的建表脚本
     */
    private String schema;
    /**
     * 自动运行的数据脚本
     */
    private String data;
    /**
     *
     */
    private Boolean seata = true;
    /**
     *
     */
    private Boolean p6spy = true;
    /**
     * 错误是否继续 默认 true
     */
    private boolean continueOnError = true;
    /**
     * 分隔符 默认 ;
     */
    private String separator = ";";
    /**
     * Druid参数配置
     */
    @NestedConfigurationProperty
    private DruidConfig druid = new DruidConfig();
    /**
     * HikariCp参数配置
     */
    @NestedConfigurationProperty
    private HikariCpConfig hikari = new HikariCpConfig();

    /**
     * 解密公匙(如果未设置默认使用全局的)
     */
    private String publicKey;

    public String getPoolName() {
        return poolName;
    }

    public void setPoolName(String poolName) {
        this.poolName = poolName;
    }

    public Class<? extends DataSource> getType() {
        return type;
    }

    public void setType(Class<? extends DataSource> type) {
        this.type = type;
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    public String getUrl() {
        return decrypt(url);
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return decrypt(username);
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return decrypt(password);
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDefaultCatalog() {
        if (StringUtils.isEmpty(defaultCatalog)) {
            String tmp = url.split("\\?")[0];
            tmp = tmp.substring(5);
            if (tmp.startsWith("p6spy")) {
                tmp = tmp.substring(6);
            }
            if (tmp.startsWith("mysql") || tmp.startsWith("postgresql") || tmp.startsWith("sybase") || tmp.startsWith("db2")) {
                tmp = tmp.substring(tmp.indexOf("//") + 2);
                int i = tmp.indexOf('/');
                if (i >= 0) {
                    defaultCatalog = tmp.substring(i + 1);
                }
            } else if (tmp.startsWith("oracle") || tmp.startsWith("sqlserver") || tmp.startsWith("microsoft:sqlserver")) {
                defaultCatalog = getUsername();
            }
        }
        return defaultCatalog;
    }

    public void setDefaultCatalog(String defaultCatalog) {
        this.defaultCatalog = defaultCatalog;
    }

    public String getJndiName() {
        return jndiName;
    }

    public void setJndiName(String jndiName) {
        this.jndiName = jndiName;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public Boolean getSeata() {
        return seata;
    }

    public void setSeata(Boolean seata) {
        this.seata = seata;
    }

    public Boolean getP6spy() {
        return p6spy;
    }

    public void setP6spy(Boolean p6spy) {
        this.p6spy = p6spy;
    }

    public boolean isContinueOnError() {
        return continueOnError;
    }

    public void setContinueOnError(boolean continueOnError) {
        this.continueOnError = continueOnError;
    }

    public String getSeparator() {
        return separator;
    }

    public void setSeparator(String separator) {
        this.separator = separator;
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

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    @Override
    public String toString() {
        return "DataSourceProperty{" +
            "poolName='" + poolName + '\'' +
            ", type=" + type +
            ", driverClassName='" + driverClassName + '\'' +
            ", url='" + url + '\'' +
            ", username='" + username + '\'' +
            ", password='" + password + '\'' +
            ", jndiName='" + jndiName + '\'' +
            ", schema='" + schema + '\'' +
            ", data='" + data + '\'' +
            ", seata=" + seata +
            ", p6spy=" + p6spy +
            ", continueOnError=" + continueOnError +
            ", separator='" + separator + '\'' +
            ", druid=" + druid +
            ", hikari=" + hikari +
            ", publicKey='" + publicKey + '\'' +
            '}';
    }

    /**
     * 字符串解密
     */
    private String decrypt(String cipherText) {
        if (StringUtils.isNotBlank(cipherText)) {
            Matcher matcher = ENC_PATTERN.matcher(cipherText);
            if (matcher.find()) {
                try {
                    return CryptoUtils.decrypt(publicKey, matcher.group(1));
                } catch (Exception e) {
                    logger.error("DynamicDataSourceProperties.decrypt error ", e);
                }
            }
        }
        return cipherText;
    }
}
