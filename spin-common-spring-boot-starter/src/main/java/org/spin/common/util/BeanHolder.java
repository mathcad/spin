package org.spin.common.util;

import org.spin.common.annotation.UtilClass;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

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
    private static Environment environment;

    private BeanHolder() {
    }

    public static void init(ApplicationContext applicationContext, DiscoveryClient discoveryClient, Environment environment) {
        BeanHolder.applicationContext = applicationContext;
        BeanHolder.discoveryClient = discoveryClient;
        BeanHolder.environment = environment;
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public static DiscoveryClient getDiscoveryClient() {
        return discoveryClient;
    }

    public static Environment getEnvironment() {
        return environment;
    }
}
