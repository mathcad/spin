package org.spin.cloud.vo;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * WebSocket消息头
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2020/1/11</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class MessageHeader {
    /**
     * 接收人，多个用“,”隔开
     */
    private Set<String> recipient;

    /**
     * 接收人类型
     */
    private RecipientType recipientType;

    /**
     * 接收模块
     */
    private String module;

    /**
     * 发送人用户Session ID
     */
    private String senderUserSid;

    /**
     * 是否发送给自己，默认false
     */
    private boolean sendToSelf;

    /**
     * 消息类型
     */
    private MessageType type;

    /**
     * 时间戳
     */
    private LocalDateTime timestamp;

    /**
     * 创建一个命令消息
     *
     * @return 消息头
     */
    public static MessageHeader aCommand() {
        return new MessageHeader().withType(MessageType.COMMAND);
    }

    /**
     * 创建一个指令消息
     *
     * @return 消息头
     */
    public static MessageHeader aDirective() {
        return new MessageHeader().withType(MessageType.DIRECTIVE);
    }

    /**
     * 创建一个通知消息
     *
     * @return 消息头
     */
    public static MessageHeader aNotice() {
        return new MessageHeader().withType(MessageType.NOTICE);
    }

    /**
     * 创建一个提示消息
     *
     * @return 消息头
     */
    public static MessageHeader aTip() {
        return new MessageHeader().withType(MessageType.TIP);
    }

    public MessageHeader withRecipient(Set<String> recipient) {
        this.recipient = recipient;
        return this;
    }

    public MessageHeader withRecipientType(RecipientType recipientType) {
        this.recipientType = recipientType;
        return this;
    }

    public MessageHeader withModule(String module) {
        this.module = module;
        return this;
    }

    public MessageHeader withSenderUserSid(String senderUserSid) {
        this.senderUserSid = senderUserSid;
        return this;
    }

    public MessageHeader withSendToSelf(boolean sendToSelf) {
        this.sendToSelf = sendToSelf;
        return this;
    }

    public MessageHeader withTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
        return this;
    }


    public Set<String> getRecipient() {
        return recipient;
    }

    public void setRecipient(Set<String> recipient) {
        this.recipient = recipient;
    }

    public RecipientType getRecipientType() {
        return recipientType;
    }

    public void setRecipientType(RecipientType recipientType) {
        this.recipientType = recipientType;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getSenderUserSid() {
        return senderUserSid;
    }

    public void setSenderUserSid(String senderUserSid) {
        this.senderUserSid = senderUserSid;
    }

    public boolean isSendToSelf() {
        return sendToSelf;
    }

    public void setSendToSelf(boolean sendToSelf) {
        this.sendToSelf = sendToSelf;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    private MessageHeader withType(MessageType type) {
        this.type = type;
        return this;
    }
}
