package org.spin.cloud.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.cloud.annotation.UtilClass;
import org.spin.cloud.vo.MessageContent;
import org.spin.cloud.vo.MessageHeader;
import org.spin.core.util.JsonUtils;
import org.spin.core.util.MapUtils;
import org.spin.core.util.Util;
import org.springframework.context.ApplicationContext;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.lang.NonNull;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.time.LocalDateTime;

/**
 * 消息推送工具类
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2020/1/11</p>
 *
 * @author xuweinan
 * @version 1.0
 */
@UtilClass
public final class MessageUtils extends Util {

    private static final Logger logger = LoggerFactory.getLogger(MessageUtils.class);

    private MessageUtils() {
    }

    private static KafkaTemplate<String, String> kafkaTemplate;

    private static final String WEBSOCKET_TOPIC = "message.websocket";
    private static final String WX_SUBSCRIBE_TOPIC = "message.wxsubscribe";

    static {
        Util.registerLatch(MessageUtils.class);
    }

    @SuppressWarnings("unchecked")
    public static void init(ApplicationContext applicationContext) {
        try {
            MessageUtils.kafkaTemplate = applicationContext.getBean(KafkaTemplate.class);
        } catch (Exception ignore) {
            logger.warn("系统上下文中没有启用Kafka, websocket功能将被禁用");
        }

        Util.ready(MessageUtils.class);
    }

    private static final ListenableFutureCallback<SendResult<String, String>> CALLBACK = new ListenableFutureCallback<SendResult<String, String>>() {
        @Override
        public void onSuccess(SendResult<String, String> result) {
            logger.info("消息发送成功");
        }

        @Override
        public void onFailure(@NonNull Throwable ex) {
            logger.warn("消息发送失败", ex);
        }
    };

    public static void sendWsMessage(MessageHeader header, MessageContent content) {
        Util.awaitUntilReady(MessageUtils.class);
        if (null == kafkaTemplate) {
            throw new UnsupportedOperationException("由于未启用Kafka, websocket发送功能无法使用");
        }

        ListenableFuture<SendResult<String, String>> result = kafkaTemplate
            .send(WEBSOCKET_TOPIC, JsonUtils.toJson(header), JsonUtils.toJson(content));
        result.addCallback(CALLBACK);
    }

    public static void sendWsCommand(String command, Object payload) {
        sendWsMessage(MessageHeader.aCommand(), MessageContent.aCommand().withTitle(command).withPayload(payload));
    }

    /**
     * 发送微信订阅消息
     *
     * @param openId 接收人
     * @param tmplId 所需下发的订阅模板id
     * @param page   跳转页面
     * @param data   数据
     * @param state  跳转小程序类型：developer为开发版；trial为体验版；formal为正式版；默认为正式版
     */
    public static void sendWxSubscribeMsg(String openId, String tmplId, String page, Object data, String state) {
        sendWxSubscribeMsg("default", openId, tmplId, page, data, state, "zh_CN");
    }

    /**
     * 发送微信订阅消息
     *
     * @param openId 接收人
     * @param tmplId 所需下发的订阅模板id
     * @param page   跳转页面
     * @param data   数据
     * @param state  跳转小程序类型：developer为开发版；trial为体验版；formal为正式版；默认为正式版
     * @param lang   进入小程序查看”的语言类型，支持zh_CN(简体中文)、en_US(英文)、zh_HK(繁体中文)、zh_TW(繁体中文)，默认为zh_CN
     */
    public static void sendWxSubscribeMsg(String openId, String tmplId, String page, Object data, String state, String lang) {
        sendWxSubscribeMsg("default", openId, tmplId, page, data, state, lang);
    }

    /**
     * 发送微信订阅消息
     *
     * @param configName 配置名称
     * @param openId     接收人
     * @param tmplId     所需下发的订阅模板id
     * @param page       跳转页面
     * @param data       数据
     * @param state      跳转小程序类型：developer为开发版；trial为体验版；formal为正式版；默认为正式版
     */
    public static void sendWxSubscribeMsg(String configName, String openId, String tmplId, String page, Object data, String state) {
        sendWxSubscribeMsg(configName, openId, tmplId, page, data, state, "zh_CN");
    }

    /**
     * 发送微信订阅消息
     *
     * @param configName 配置名称
     * @param openId     接收人
     * @param tmplId     所需下发的订阅模板id
     * @param page       跳转页面
     * @param data       数据
     * @param state      跳转小程序类型：developer为开发版；trial为体验版；formal为正式版；默认为正式版
     * @param lang       进入小程序查看”的语言类型，支持zh_CN(简体中文)、en_US(英文)、zh_HK(繁体中文)、zh_TW(繁体中文)，默认为zh_CN
     */
    public static void sendWxSubscribeMsg(String configName, String openId, String tmplId, String page, Object data, String state, String lang) {
        Util.awaitUntilReady(MessageUtils.class);
        if (null == kafkaTemplate) {
            throw new UnsupportedOperationException("由于未启用Kafka, 微信消息推送功能无法使用");
        }

        String msg = JsonUtils.toJson(MapUtils.ofMap("touser", openId, "templateId", tmplId,
            "page", page,
            "data", data,
            "miniprogramState", state,
            "lang", lang));
        logger.info("消息[{}]投递时间: {}", msg, LocalDateTime.now());
        ListenableFuture<SendResult<String, String>> result = kafkaTemplate
            .send(WX_SUBSCRIBE_TOPIC, configName, msg);
        result.addCallback(CALLBACK);
    }
}

