package org.spin.wx.base;

public interface MessageType {

    /**
     * 文本消息
     */
    String TEXT = "text";

    /**
     * 图片消息
     */
    String IMAGE = "image";

    /**
     * 语音消息
     */
    String VOICE = "voice";

    /**
     * 视频消息
     */
    String VEDIO = "vedio";

    /**
     * 地理位置消息
     */
    String LACATION = "location";

    /**
     * 事件消息
     */
    String EVENT = "event";

    /**
     * 图文消息
     */
    String NEWS = "news";

    /**
     * 验证URL消息
     */
    String VERIFY_URL = "verifyUrl";
}
