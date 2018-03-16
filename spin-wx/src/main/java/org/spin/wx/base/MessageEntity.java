package org.spin.wx.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.util.StringUtils;
import org.spin.core.util.XmlUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 微信接口消息封装类
 *
 * @author xuweinan
 */
public class MessageEntity implements Serializable {
    private static final long serialVersionUID = -5822527770051354954L;
    private static final Logger logger = LoggerFactory.getLogger(MessageEntity.class);

    private Map<String, String> properties;

    public MessageEntity() {
        this.properties = new HashMap<>();
    }

    public MessageEntity(Map<String, String> properties) {
        this.properties = properties;
    }

    /**
     * 将消息实体序列化为xml串
     *
     * @return xml字符串
     */
    public String toXml() {
        StringBuilder result = new StringBuilder(512);
        result.append("<xml>");
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            if (StringUtils.isEmpty(entry.getValue())) {
                continue;
            }
            if (logger.isTraceEnabled()) {
                logger.trace("maps of msgEntity: propName={}&propValue={}", entry.getKey(), entry.getValue());
            }
            result.append("<").append(entry.getKey()).append(">").append("<![CDATA[").append(entry.getValue())
                .append("]]></").append(entry.getKey()).append(">");
        }
        result.append("</xml>");
        if (logger.isTraceEnabled()) {
            logger.trace("composited xml string: {}", result);
        }
        return result.toString();
    }

    public static MessageEntity fromXml(String xmlContent) {
        MessageEntity entity = new MessageEntity();
        entity.properties = XmlUtils.getInstance(xmlContent, XmlUtils.SourceType.XML_CONTENT).getSubElementsValue();
        return entity;
    }

    public String getMsgType() {
        return properties.get(PropertyType.MSG_TYPE);
    }

    public String[] getPropNames() {
        return properties.keySet().toArray(new String[0]);
    }

    public String getProperty(String name) {
        return properties.get(name);
    }

    public void setProperty(String propName, String propValue) {
        this.properties.put(propName, propValue);
    }

    public void setProperty(Map<String, String> properties) {
        this.properties.putAll(properties);
    }

    public String removeProperty(String propName) {
        return this.properties.remove(propName);
    }
}
