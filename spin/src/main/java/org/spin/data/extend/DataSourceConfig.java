package org.spin.data.extend;

import org.spin.core.util.StringUtils;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.security.InvalidKeyException;
import java.util.Properties;

/**
 * <p>Created by xuweinan on 2017/11/29.</p>
 *
 * @author xuweinan
 */
public interface DataSourceConfig {

    String getName();

    void setName(String name);

    String getUrl();

    void setUrl(String url);

    String getDriverClassName();

    void setDriverClassName(String driverClassName);

    String getUsername();

    void setUsername(String username);

    String getPassword();

    void setPassword(String password) throws BadPaddingException, InvalidKeyException, IllegalBlockSizeException;

    int getMaxPoolSize();

    int getMinPoolSize();

    String getXaDataSourceClassName();

    String getDataSourceClassName();

    Properties toProperties();

    default String getVenderName() {
        String vender = getUrl().split(":")[1].toUpperCase();
        return StringUtils.isEmpty(vender) ? "UNKNOW-[" + getUrl() + "]" : vender;
    }

    default void notNullAdd(Properties properties, String prefix, String key, Object value) {
        if (value != null) {
            properties.setProperty(StringUtils.trimToEmpty(prefix) + key, value.toString());
        }
    }
}
