package org.spin.boot.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * <p>Created by xuweinan on 2017/5/17.</p>
 *
 * @author xuweinan
 */
//@ConfigurationProperties(prefix = "spin")
public class SpinConfigProperties {

    private WebProperties web;

    private EncryptProperties encrypt;

    public static class WebProperties {

    }

    public static class EncryptProperties {

    }
}
