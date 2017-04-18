package org.spin.util;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Copyright(c) Kengine 2009-2015. 玉柴联合动力股份有限公司
 * <p>XML文件解析通用类，提供简单的以xpath方式访问xml的方法</p>
 * <p>JDK version: 1.7 or later</p>
 * <p>Created by xuweinan on 2015/4/14.</p>
 * <p>
 * Modified By: xuweinan<br>
 * Modified Time: Apr 24, 2015 16:00:13
 * </p>
 * Modified History:<br>
 * 2015-04-24 实现文档递归遍历算法 xuweinan
 *
 * @author xuweinan
 * @version 1.0.3
 */
public final class XmlUtils {
    private static Logger logger = LoggerFactory.getLogger(XmlUtils.class);

    public enum SourceType {
        XmlFilePath, XmlContent
    }

    private static SAXReader reader = new SAXReader();
    private Document document = null;

    private XmlUtils(File xmlFile) throws DocumentException {
        this.document = XmlUtils.reader.read(xmlFile);
    }

    private XmlUtils(String xmlContent) throws DocumentException {
        StringReader strReader = new StringReader(xmlContent);
        this.document = XmlUtils.reader.read(strReader);
        strReader.close();
    }

    public static XmlUtils getXmlUtil(String xmlSource, SourceType type) {
        XmlUtils result = null;
        try {
            switch (type) {
                case XmlFilePath:
                    result = new XmlUtils(new File(xmlSource));
                    break;
                case XmlContent:
                    result = new XmlUtils(xmlSource);
                    break;
            }

        } catch (DocumentException e) {
            logger.error("XmlUtils parse ERROR 无法解析指定文档({}) -----------", xmlSource);
            logger.debug("Exception Message:{}", e.getMessage());
        }
        return result;
    }

    public List<?> getNodesbyXPath(String XPath) {
        return document.selectNodes(XPath);
    }

    /**
     * 获取指定xpath的最后一个值（如果有多个的话）
     *
     * @param XPath xpath路径
     * @return 获取的最后一个值
     */
    public String getLastValuebyXPath(String XPath) {
        List<?> list = this.getNodesbyXPath(XPath);
        if (list == null)
            return null;
        String result = null;
        for (Object aList : list) {
            Node n = (Node) aList;
            result = n.valueOf(".");
        }
        return result;
    }

    public Map<String, String> getSubElementsValue() {
        Element sub;
        Map<String, String> result = new HashMap<>();
        for (Iterator<?> iter = document.getRootElement().elementIterator(); iter.hasNext(); ) {
            sub = (Element) iter.next();
            result.put(sub.getName(), sub.getText());
        }
        return result;
    }

    /**
     * 递归遍历整个document
     * <p>
     * 得到document中所有叶节点的xpath与值的键值对
     * </p>
     * <p>
     * 形如: rootName.subName=value
     * </p>
     *
     * @return HashMap键值对
     */
    public Map<String, String> travelDocument() {
        return this.travels("", null);
    }

    private Map<String, String> travels(String prefix, Element root) {
        if (null == root) {
            root = document.getRootElement();
        }
        Map<String, String> result = new HashMap<>();
        List<?> sub = root.elements();
        if (null == sub)
            return result;
        for (Object aSub : sub) {
            Element elem = (Element) aSub;
            String tmp = elem.getName();
            if (!"".equals(prefix))
                tmp = prefix + "." + tmp;
            if (elem.elements().size() == 0) {

                result.put(tmp, elem.getText());
            }
            result.putAll(travels(tmp, elem));
        }
        return result;
    }
}
