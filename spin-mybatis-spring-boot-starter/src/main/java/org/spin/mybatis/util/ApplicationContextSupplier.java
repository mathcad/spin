package org.spin.mybatis.util;

import org.spin.core.util.Util;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.ApplicationContext;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2022/3/8</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class ApplicationContextSupplier extends Util {

    private static ApplicationContext applicationContext;

    private ApplicationContextSupplier() {
    }

    static {
        Util.registerLatch(ApplicationContextSupplier.class);
    }

    public static void init(ApplicationContext applicationContext) {
        ApplicationContextSupplier.applicationContext = applicationContext;
        Util.ready(ApplicationContextSupplier.class);
    }

    public static ApplicationContext getApplicationContext() {
        Util.awaitUntilReady(ApplicationContextSupplier.class);
        return applicationContext;
    }
}
