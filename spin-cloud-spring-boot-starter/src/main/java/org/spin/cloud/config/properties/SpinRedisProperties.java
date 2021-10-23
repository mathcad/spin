package org.spin.cloud.config.properties;

import org.spin.data.redis.LettuceRedisProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2021/10/19</p>
 *
 * @author xuweinan
 * @version 1.0
 */
@ConfigurationProperties(prefix = "spin.redis")
public class SpinRedisProperties extends LettuceRedisProperties {
    private String delayQueueName;

    public String getDelayQueueName() {
        return delayQueueName;
    }

    public void setDelayQueueName(String delayQueueName) {
        this.delayQueueName = delayQueueName;
    }
}
