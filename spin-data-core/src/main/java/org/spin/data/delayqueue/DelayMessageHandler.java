package org.spin.data.delayqueue;

/**
 * 延迟队列消息处理器
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2021/10/19</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public interface DelayMessageHandler {

    /**
     * @return 队列主题
     */
    String getTopic();

    /**
     * 消息处理逻辑
     *
     * @param message 消息
     * @throws Exception 异常
     */
    void handle(String message) throws Exception;

    /**
     * 异常处理逻辑
     *
     * @param message 消息
     * @param e       异常
     */
    void handleException(String message, Exception e);

}
