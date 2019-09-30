package com.netflix.discovery;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * 服务发现客户端更新消息监听器
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/9/18</p>
 *
 * @author xuweinan
 * @version 1.0
 */
//@Component
public class DiscoveryClientUpdateListener {
    private static final Logger logger = LoggerFactory.getLogger(DiscoveryClientUpdateListener.class);

    private final com.netflix.discovery.DiscoveryClient discoveryClient;

    public DiscoveryClientUpdateListener(com.netflix.discovery.DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    @KafkaListener(topics = "framework.discoveryClientUpdate", id = "discoveryClientListener-${random.int}")
    public void onDiscoveryClientMessage(ConsumerRecord<String, String> record) {
        logger.info("收到注册中心更新消息, 更新本地服务缓存");
        discoveryClient.refreshRegistry();
        discoveryClient.refreshInstanceInfo();
    }
}
