package org.spin.cloud.vo;

import org.spin.core.Assert;
import org.spin.core.trait.Evaluatable;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class MailVo implements Serializable {
    private static final long serialVersionUID = 572268660244872253L;
    /**
     * 消息类型
     */
    private String type;

    /**
     * 消息标题
     */
    private String title;

    /**
     * 摘要信息
     */
    private String summary;

    /**
     * 图标
     */
    private String icon;
    /**
     * 消息内容
     */
    private String content;

    /**
     * 内容类型
     */
    private ContentType contentType;
    /**
     * 所属模块
     */
    private String scope;
    /**
     * 接收人
     */
    private List<MailReceiverVo> receivers;


    public static MailVo aTextMail(String type) {
        return new MailVo().withType(type).withContentType(ContentType.TEXT);
    }

    public static MailVo aJsonMail(String type) {
        return new MailVo().withType(type).withContentType(ContentType.JSON);
    }

    public static MailVo aRtfMail(String type) {
        return new MailVo().withType(type).withContentType(ContentType.RTF);
    }

    public MailVo withType(String type) {
        this.type = type;
        return this;
    }

    public MailVo withTitle(String title) {
        this.title = title;
        return this;
    }

    public MailVo withSummary(String summary) {
        this.summary = summary;
        return this;
    }

    public MailVo withIcon(String icon) {
        this.icon = icon;
        return this;
    }

    public MailVo withContent(String content) {
        this.content = content;
        return this;
    }

    public MailVo withContentType(ContentType contentType) {
        this.contentType = contentType;
        return this;
    }

    public MailVo withScope(String scope) {
        this.scope = scope;
        return this;
    }

    public MailVo withReceivers(List<MailReceiverVo> receivers) {
        this.receivers = receivers;
        return this;
    }

    public MailVo withReceiver(MailReceiverVo receiver) {
        if (null == receivers) {
            receivers = new LinkedList<>();
        }
        this.receivers.add(Assert.notNull(receiver, "接收人不能为空"));
        return this;
    }

    public MailVo withReceiver(String receiver) {
        return withReceiver(MailReceiverVo.aReceiver(receiver));
    }

    public MailVo withReceiver(String receiver, Long enterpriseId) {
        return withReceiver(MailReceiverVo.aReceiver(receiver, enterpriseId));
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public ContentType getContentType() {
        return contentType;
    }

    public void setContentType(ContentType contentType) {
        this.contentType = contentType;
    }

    public List<MailReceiverVo> getReceivers() {
        return receivers;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public void setReceivers(List<MailReceiverVo> receivers) {
        this.receivers = receivers;
    }


    public enum ContentType implements Evaluatable<Integer> {

        /**
         * 文本
         */
        TEXT(1),

        /**
         * 富文本
         */
        RTF(2),

        /**
         * JSON
         */
        JSON(3);

        private final int value;

        ContentType(int value) {
            this.value = value;
        }

        @Override
        public Integer getValue() {
            return value;
        }
    }
}
