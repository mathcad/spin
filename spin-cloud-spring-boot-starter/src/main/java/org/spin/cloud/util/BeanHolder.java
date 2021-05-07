package org.spin.cloud.util;

import org.spin.cloud.annotation.UtilClass;
import org.spin.core.util.Util;
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
public final class BeanHolder extends Util {

    private static ApplicationContext applicationContext;
    private static DiscoveryClient discoveryClient;

    private BeanHolder() {
    }

    static {
        Util.registerLatch(BeanHolder.class);
    }

    public static void init(ApplicationContext applicationContext, DiscoveryClient discoveryClient) {
        BeanHolder.applicationContext = applicationContext;
        BeanHolder.discoveryClient = discoveryClient;
        Util.ready(BeanHolder.class);
    }

    public static ApplicationContext getApplicationContext() {
        Util.awaitUntilReady(BeanHolder.class);
        return applicationContext;
    }

    public static DiscoveryClient getDiscoveryClient() {
        Util.awaitUntilReady(BeanHolder.class);
        return discoveryClient;
    }
}
