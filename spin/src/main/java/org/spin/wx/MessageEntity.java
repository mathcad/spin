package org.spin.wx;

import org.spin.core.util.XmlUtils;
import org.spin.wx.wx.base.PropertyType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * 微信接口消息封装类
 *
 * @author xuweinan
 */
public class MessageEntity {
    private static Logger logger = LoggerFactory.getLogger(MessageEntity.class);
    private Map<String, String> properties;

    public String getPropValue(String name) {
        return properties.get(name);
    }

    public String[] getPropNames() {
        return (String[]) properties.keySet().toArray();
    }

    public String getMsgType() {
        return properties.get(PropertyType.MSG_TYPE);
    }

    /**
     * 将消息实体序列化为xml串
     *
     * @return xml字符串
     */
    public String getXmlProp() {
        StringBuffer result = new StringBuffer(512);
        result.append("<xml>");
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            logger.trace("maps of msgEntity: propName={}&propValue={}", entry.getKey(), entry.getValue());
            result.append("<").append(entry.getKey()).append(">").append("<![CDATA[").append(entry.getValue())
                .append("]]></").append(entry.getKey()).append(">");
        }
        result.append("</xml>");
        logger.trace("composited xml string: {}", result);
        return result.toString();
    }

    public static MessageEntity configEntity(String xmlContent) {
        MessageEntity entity = new MessageEntity();
        entity.properties = XmlUtils.getXmlUtil(xmlContent, XmlUtils.SourceType.XmlContent).getSubElementsValue();
        return entity;
    }

    public MessageEntity() {
        this.properties = new HashMap<>();
    }

    public MessageEntity(Map<String, String> properties) {
        this.properties = properties;
    }

    public void addProperty(String propName, String propValue) {
        this.properties.put(propName, propValue);
    }

    public void addProperty(Map<String, String> properties) {
        this.properties.putAll(properties);
    }

    public String removeProperty(String propName) {
        return this.properties.remove(propName);
    }

    public void setProperty(String propName, String propValue) {
        this.properties.put(propName, propValue);
    }
}
