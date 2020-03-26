package org.spin.cloud.util;

import org.spin.cloud.annotation.UtilClass;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.ApplicationContext;

/**
 * 常用Bean
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/9/17</p>
 *
 * @author xuweinan
 * @version 1.0
 */
@UtilClass
public abstract class BeanHolder {

    private static ApplicationContext applicationContext;
    private static DiscoveryClient discoveryClient;

    private BeanHolder() {
    }

    public static void init(ApplicationContext applicationContext, DiscoveryClient discoveryClient) {
        BeanHolder.applicationContext = applicationContext;
        BeanHolder.discoveryClient = discoveryClient;
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public static DiscoveryClient getDiscoveryClient() {
        return discoveryClient;
    }
}
