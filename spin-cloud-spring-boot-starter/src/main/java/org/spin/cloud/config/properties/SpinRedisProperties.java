package org.spin.cloud.config.properties;

import org.spin.data.redis.LettuceRedisProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

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
    private DelayQueue delayQueue;

    public DelayQueue getDelayQueue() {
        return delayQueue;
    }

    public void setDelayQueue(DelayQueue delayQueue) {
        this.delayQueue = delayQueue;
    }

    public static class DelayQueue {
        private String name;
        private String scheduleGroupId;
        private Integer corePoolSize = 2;
        private Integer maxPoolSize = 10;
        private Duration keepAliveTime = Duration.ofMinutes(3);
        private Integer workQueueSize = 20;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getScheduleGroupId() {
            return scheduleGroupId;
        }

        public void setScheduleGroupId(String scheduleGroupId) {
            this.scheduleGroupId = scheduleGroupId;
        }

        public Integer getCorePoolSize() {
            return corePoolSize;
        }

        public void setCorePoolSize(Integer corePoolSize) {
            this.corePoolSize = corePoolSize;
        }

        public Integer getMaxPoolSize() {
            return maxPoolSize;
        }

        public void setMaxPoolSize(Integer maxPoolSize) {
            this.maxPoolSize = maxPoolSize;
        }

        public Duration getKeepAliveTime() {
            return keepAliveTime;
        }

        public void setKeepAliveTime(Duration keepAliveTime) {
            this.keepAliveTime = keepAliveTime;
        }

        public Integer getWorkQueueSize() {
            return workQueueSize;
        }

        public void setWorkQueueSize(Integer workQueueSize) {
            this.workQueueSize = workQueueSize;
        }
    }
}
