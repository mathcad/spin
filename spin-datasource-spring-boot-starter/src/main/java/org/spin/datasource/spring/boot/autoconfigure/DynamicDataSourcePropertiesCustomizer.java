package org.spin.datasource.spring.boot.autoconfigure;

/**
 * @author hzh727172424
 * @since 3.4.0
 */
public interface DynamicDataSourcePropertiesCustomizer {

    /**
     * Customize the given a {@link DynamicDataSourceProperties} object.
     *
     * @param properties the DynamicDataSourceProperties object to customize
     */
    void customize(DynamicDataSourceProperties properties);
}
