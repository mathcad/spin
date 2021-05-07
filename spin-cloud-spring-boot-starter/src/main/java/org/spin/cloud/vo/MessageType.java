package org.spin.cloud.vo;

/**
 * 消息类型
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2020/1/11</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public enum MessageType {

    /**
     * 命令(仅用于框架内部，消息不会实际发出)
     */
    COMMAND,

    /**
     * 指令(该类型消息不可见，用于指示接收者执行特定操作)
     */
    DIRECTIVE,

    /**
     * 通知
     */
    NOTICE,

    /**
     * 提示
     */
    TIP,

    /**
     * 消息
     */
    MSG,
}
