package org.spin.cloud.config.properties;

import org.spin.data.delayqueue.RedisDelayQueueProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2021/10/19</p>
 *
 * @author xuweinan
 * @version 1.0
 */
@ConfigurationProperties(prefix = "spin.delay-queue")
public class DelayQueueProperties extends RedisDelayQueueProperties {
}
