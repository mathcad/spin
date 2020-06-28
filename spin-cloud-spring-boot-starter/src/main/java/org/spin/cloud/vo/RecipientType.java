package org.spin.cloud.vo;

/**
 * 接收人类型
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2020/1/11</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public enum RecipientType {

    /**
     * 用户(用户ID)
     */
    USER,

    /**
     * 用户会话(用户ID:用户会话ID)
     */
    USER_SESSION,

    /**
     * Websocket会话
     */
    SESSION
}
