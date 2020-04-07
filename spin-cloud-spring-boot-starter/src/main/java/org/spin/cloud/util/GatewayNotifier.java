package org.spin.cloud.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.cloud.annotation.UtilClass;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.lang.NonNull;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

/**
 * 网关消息通知器
 * <p>实现对服务网关的多渠道通知</p>
 * <p>Created by xuweinan on 2019/11/18</p>
 *
 * @author xuweinan
 * @version 1.0
 */
@UtilClass
public abstract class GatewayNotifier {
    private static final Logger logger = LoggerFactory.getLogger(GatewayNotifier.class);

    private static final String ROLES_UPDATE_TIME_KEY = "GATEWAY.ROLES.UPDATETIME";
    private static final String ROLE_DEST = "gateway.roles";

    private static StringRedisTemplate redisTemplate;
    private static ApplicationContext applicationContext;

    public static void init(ApplicationContext applicationContext, StringRedisTemplate redisTemplate) {
        GatewayNotifier.applicationContext = applicationContext;
        GatewayNotifier.redisTemplate = redisTemplate;
    }

    /**
     * 更新了角色-权限绑定，用户组-角色绑定，角色继承关系后，通知网关
     */
    public static void notifyRoleAndGroupChanged() {
        redisTemplate.opsForValue().set(ROLES_UPDATE_TIME_KEY, String.valueOf(System.currentTimeMillis()));
        @SuppressWarnings("unchecked")
        KafkaTemplate<String, String> kafkaTemplate = applicationContext.getBean(KafkaTemplate.class);
        ListenableFuture<SendResult<String, String>> result = kafkaTemplate.send(ROLE_DEST, ROLES_UPDATE_TIME_KEY, String.valueOf(System.currentTimeMillis()));
        result.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
            @Override
            public void onSuccess(SendResult<String, String> result) {
                logger.info("网关角色变更通知消息发送成功");
            }

            @Override
            public void onFailure(@NonNull Throwable ex) {
                logger.warn("网关角色变更通知消息发送失败", ex);
            }
        });
    }
}
