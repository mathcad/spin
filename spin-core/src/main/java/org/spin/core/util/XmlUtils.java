package org.spin.core.util;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
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
    private static SAXReader reader = new SAXReader();
    private Document document;

    public XmlUtils(File xmlFile) {
        try {
            document = reader.read(xmlFile);
        } catch (DocumentException e) {
            logger.error("XmlUtils parse ERROR 无法解析指定文档({}) -----------", xmlFile.getName());
            logger.debug("Exception Message:{}", e.getMessage());
        }
    }

    public XmlUtils(InputStream xmlStream) {
        try {
            document = reader.read(xmlStream);
        } catch (DocumentException e) {
            logger.error("XmlUtils parse ERROR 无法解析指定文档({}) -----------");
            logger.debug("Exception Message:{}", e.getMessage());
        }
    }

    public XmlUtils(String xmlContent) {
        try {
            StringReader strReader = new StringReader(xmlContent);
            document = reader.read(strReader);
            strReader.close();
        } catch (DocumentException e) {
            logger.error("XmlUtils parse ERROR 无法解析指定文档({}) -----------", xmlContent);
            logger.debug("Exception Message:{}", e.getMessage());
        }
    }

    public List<Node> getNodesbyXPath(String xPath) {
        //noinspection unchecked
        return document.selectNodes(xPath);
    }

    /**
     * 获取指定xpath的最后一个值（如果有多个的话）
     *
     * @param xPath xpath路径
     * @return 获取的最后一个值
     */
    public String getLastValuebyXPath(String xPath) {
        List<?> list = this.getNodesbyXPath(xPath);
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
        Map<String, String> result = new HashMap<>();
        this.travels("", null, result);
        return result;
    }

    private void travels(String prefix, Element root, Map<String, String> result) {
        if (null == root) {
            root = document.getRootElement();
        }
        @SuppressWarnings("unchecked")
        List<Element> subElems = root.elements();
        if (null == subElems || subElems.isEmpty()) {
            return;
        }
        prefix = prefix.length() == 0 ? prefix : prefix + ".";
        String tmp;
        for (Element elem : subElems) {
            tmp = prefix + elem.getName();
            if (elem.elements().isEmpty()) {
                result.put(tmp, elem.getText());
            }
            travels(tmp, elem, result);
        }
    }
}
