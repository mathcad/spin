package org.spin.cloud.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.cloud.annotation.UtilClass;
import org.spin.cloud.vo.CurrentUser;
import org.spin.cloud.vo.LogInfoVo;
import org.spin.cloud.vo.LogLevel;
import org.spin.cloud.vo.SessionEmpInfo;
import org.spin.core.Assert;
import org.spin.core.util.JsonUtils;
import org.spin.core.util.Util;
import org.springframework.context.ApplicationContext;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.lang.NonNull;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.time.LocalDateTime;

/**
 * 系统日志发布者
 * <p>将用户日志推送到消息队列</p>
 * <p>Created by xuweinan on 2019/12/26</p>
 *
 * @author xuweinan
 * @version 1.0
 */
@UtilClass
@SuppressWarnings("unchecked")
public final class SysLogPublisher extends Util {
    public static final String LOG_DEST = "platform.log";
    private static final Logger logger = LoggerFactory.getLogger(SysLogPublisher.class);

    private static final ListenableFutureCallback<SendResult<String, String>> CALLBACK = new ListenableFutureCallback<SendResult<String, String>>() {
        @Override
        public void onSuccess(SendResult<String, String> result) {
            logger.info("日志消息发送成功");
        }

        @Override
        public void onFailure(@NonNull Throwable ex) {
            logger.warn("日志消息发送失败", ex);
        }
    };

    private static KafkaTemplate<String, String> kafkaTemplate;

    static {
        Util.registerLatch(SysLogPublisher.class);
    }

    private static void init(ApplicationContext applicationContext) {
        try {
            SysLogPublisher.kafkaTemplate = applicationContext.getBean(KafkaTemplate.class);
        } catch (Exception ignore) {
            logger.warn("系统上下文中没有启用Kafka, 日志功能将被禁用");
        }
        Util.ready(SysLogPublisher.class);
    }

    public static void publish(LogInfoVo infoVo) {
        Util.awaitUntilReady(SysLogPublisher.class);
        if (null == kafkaTemplate) {
            throw new UnsupportedOperationException("由于未启用Kafka, 平台日志发送功能无法使用");
        }

        Assert.notEmpty(Assert.notNull(infoVo, "日志内容不能为空").getModule(), "操作模块不能为空");
        Assert.notEmpty(infoVo.getOperation(), "操作内容不能为空");
        Assert.notNull(infoVo.getOperationTime(), "操作时间不能为空");
        CurrentUser currentUser = CurrentUser.getCurrent();
        infoVo.setAppName(Env.getAppName());
        if (null != currentUser) {
            SessionEmpInfo enterprise = currentUser.getSessionEmpInfo();
            infoVo.setUserId(currentUser.getId());
            infoVo.setRealName(currentUser.getName());
            if (null != enterprise) {
                infoVo.setEnterpriseId(enterprise.getEnterpriseId());
            } else {
                infoVo.setEnterpriseId(0L);
            }
            infoVo.setAccessIp(currentUser.getLoginIp());
            infoVo.setLoginTime(currentUser.getLoginTime());
        } else {
            infoVo.setUserId(null);
            infoVo.setRealName("匿名");
            infoVo.setEnterpriseId(null);
            infoVo.setAccessIp(null);
            infoVo.setLoginTime(null);
        }

        ListenableFuture<SendResult<String, String>> result = kafkaTemplate.send(LOG_DEST, "log", JsonUtils.toJson(infoVo));
        result.addCallback(CALLBACK);
    }

    public static void publish(String module, String operation) {
        publish(module, operation, LocalDateTime.now());
    }

    public static void publish(String module, String operation, LocalDateTime operationTime) {
        publish(module, LogLevel.INFO, operation, operationTime);
    }

    public static void publish(String module, LogLevel level, String operation, LocalDateTime operationTime) {
        LogInfoVo infoVo = LogInfoVo.aLog(module, operation)
            .withLevel(level)
            .withOperationTime(operationTime);
        publish(infoVo);
    }
}
