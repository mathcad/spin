package org.spin.cloud.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.cloud.vo.CurrentUser;
import org.spin.cloud.vo.LogInfoVo;
import org.spin.cloud.vo.SessionEmpInfo;
import org.spin.core.Assert;
import org.spin.core.collection.Pair;
import org.spin.core.util.JsonUtils;
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
@SuppressWarnings("unchecked")
public class SysLogPublisher {
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
        try {
            SysLogPublisher.kafkaTemplate = BeanHolder.getApplicationContext().getBean(KafkaTemplate.class);
        } catch (Exception ignore) {
            logger.warn("系统上下文中没有启用Kafka, 日志功能将被禁用");
        }
    }

    public static void publish(LogInfoVo infoVo) {
        if (null == kafkaTemplate) {
            throw new UnsupportedOperationException("由于未启用Kafka, 平台日志发送功能无法使用");
        }

        Assert.notEmpty(Assert.notNull(infoVo, "日志内容不能为空").getModule(), "操作模块不能为空");
        Assert.notEmpty(infoVo.getOperation(), "操作内容不能为空");
        Assert.notNull(infoVo.getOperationTime(), "操作时间不能为空");
        CurrentUser currentUser = CurrentUser.getCurrent();
        infoVo.setAppName(Env.getAppName());
        if (null != currentUser) {
            SessionEmpInfo enterprise = currentUser.getSessionEnterprise();
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
        LogInfoVo infoVo = new LogInfoVo();
        infoVo.setModule(module);
        infoVo.setOperation(operation);
        infoVo.setOperationTime(operationTime);
        publish(infoVo);
    }
}
