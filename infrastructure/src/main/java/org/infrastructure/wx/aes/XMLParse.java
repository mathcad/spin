package org.infrastructure.wx.aes;

import org.dom4j.DocumentHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * XMLParse class
 * <p>
 * 提供提取消息格式中的密文及生成回复消息格式的接口.
 */
public class XMLParse {

    /**
     * 提取出xml数据包中的加密消息
     *
     * @param xmltext 待提取的xml字符串
     * @return 提取出的加密消息字符串
     */
    public static Object[] extract(String xmltext) throws AesException {
        Object[] result = new Object[3];
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            StringReader sr = new StringReader(xmltext);
            InputSource is = new InputSource(sr);
            Document document = db.parse(is);

            Element root = document.getDocumentElement();
            NodeList nodelist1 = root.getElementsByTagName("Encrypt");
            NodeList nodelist2 = root.getElementsByTagName("ToUserName");
            result[0] = 0;
            result[1] = nodelist1.item(0).getTextContent();
            result[2] = nodelist2.item(0).getTextContent();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            throw new AesException(AesException.ParseXmlError);
        }
    }

    /**
     * 生成xml消息
     *
     * @param encrypt   加密后的消息密文
     * @param signature 安全签名
     * @param timestamp 时间戳
     * @param nonce     随机字符串
     * @return 生成的xml字符串
     */
    public static String generate(String encrypt, String signature, String timestamp, String nonce) {

        String format = "<xml>\n" + "<Encrypt><![CDATA[%1$s]]></Encrypt>\n"
                + "<MsgSignature><![CDATA[%2$s]]></MsgSignature>\n"
                + "<TimeStamp>%3$s</TimeStamp>\n" + "<Nonce><![CDATA[%4$s]]></Nonce>\n" + "</xml>";
        return String.format(format, encrypt, signature, timestamp, nonce);

    }

    /**
     * 拼装xml，专为微信设计
     * @param map 请求参数
     * @return
     */
    public static String parseXml(SortedMap<String, Object> map) {
        StringBuffer sb = new StringBuffer();
        sb.append("<xml>");
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            sb.append("<").append(entry.getKey()).append(">");
            sb.append("<![CDATA[").append(entry.getValue()).append("]]>");
            sb.append("</").append(entry.getKey()).append(">");
        }
        sb.append("</xml>");
        return sb.toString();
    }

    /**
     * 微信返回xml转换成map
     * @param xml 返回xml
     * @return
     * @throws Exception
     */
    public static Map<String, Object> revertFromXml(String xml) throws Exception {
        SortedMap<String, Object> map = new TreeMap<>();
        org.dom4j.Document document = DocumentHelper.parseText(xml);
        org.dom4j.Element root = document.getRootElement();
        List<org.dom4j.Element> elementList = root.elements();
        for (org.dom4j.Element e : elementList) {
            map.put(e.getName(), e.getText());
        }

        return map;
    }
}