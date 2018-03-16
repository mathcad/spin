package org.spin.wx.base;

public final class MessageType {

    private MessageType() {
    }

    /**
     * 文本消息
     */
    public static final String TEXT = "text";

    /**
     * 图片消息
     */
    public static final String IMAGE = "image";

    /**
     * 语音消息
     */
    public static final String VOICE = "voice";

    /**
     * 视频消息
     */
    public static final String VEDIO = "vedio";

    /**
     * 地理位置消息
     */
    public static final String LACATION = "location";

    /**
     * 事件消息
     */
    public static final String EVENT = "event";

    /**
     * 图文消息
     */
    public static final String NEWS = "news";

    /**
     * 验证URL消息
     */
    public static final String VERIFY_URL = "verifyUrl";
}
