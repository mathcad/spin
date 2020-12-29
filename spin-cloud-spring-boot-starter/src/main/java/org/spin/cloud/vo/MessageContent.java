package org.spin.cloud.vo;

/**
 * 消息体
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2020/1/11</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class MessageContent {

    /**
     * 消息类型
     */
    private MessageType type;

    /**
     * 标题
     */
    private String title;

    /**
     * 消息子类型，由业务自行确定
     */
    private Integer subType;

    /**
     * 图标
     */
    private String icon;

    /**
     * 消息内容
     */
    private Object payload;

    /**
     * 跳转链接
     */
    private String url;

    /**
     * 创建一个命令消息
     *
     * @return 消息内容
     */
    public static MessageContent aCommand() {
        return new MessageContent().withType(MessageType.COMMAND);
    }

    /**
     * 创建一个指令消息
     *
     * @return 消息内容
     */
    public static MessageContent aDirective() {
        return new MessageContent().withType(MessageType.DIRECTIVE);
    }

    /**
     * 创建一个通知消息
     *
     * @return 消息内容
     */
    public static MessageContent aNotice() {
        return new MessageContent().withType(MessageType.NOTICE);
    }

    /**
     * 创建一个提示消息
     *
     * @return 消息内容
     */
    public static MessageContent aTip() {
        return new MessageContent().withType(MessageType.TIP);
    }

    /**
     * 创建一个用户消息
     *
     * @return 消息内容
     */
    public static MessageContent aMsg() {
        return new MessageContent().withType(MessageType.MSG);
    }

    public MessageContent withTitle(String title) {
        this.title = title;
        return this;
    }

    public MessageContent withSubType(Integer subType) {
        this.subType = subType;
        return this;
    }

    public MessageContent withIcon(String icon) {
        this.icon = icon;
        return this;
    }

    public MessageContent withPayload(Object payload) {
        this.payload = payload;
        return this;
    }

    public MessageContent withUrl(String url) {
        this.url = url;
        return this;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getSubType() {
        return subType;
    }

    public void setSubType(Integer subType) {
        this.subType = subType;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }

    private MessageContent withType(MessageType type) {
        this.type = type;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
