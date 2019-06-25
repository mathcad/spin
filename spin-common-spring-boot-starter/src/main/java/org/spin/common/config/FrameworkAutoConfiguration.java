package org.spin.common.config;

import org.spin.common.internal.NetworkUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Configuration;

/**
 * 框架基础自动配置
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/6/24</p>
 *
 * @author xuweinan
 * @version 1.0
 */
@Configuration
public class FrameworkAutoConfiguration {

    @ConditionalOnBean(DiscoveryClient.class)
    public InitializingBean networkInit(DiscoveryClient client) {
        return () -> NetworkUtils.setDiscoveryClient(client);
    }
}
