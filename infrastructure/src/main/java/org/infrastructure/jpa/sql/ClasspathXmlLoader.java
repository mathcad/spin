/*
 *  Copyright 2002-2016 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.infrastructure.jpa.sql;

import org.infrastructure.throwable.SQLException;
import org.infrastructure.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.Charset;

/**
 * 基于xml格式的sql装载器
 * Created by xuweinan on 2016/8/14.
 *
 * @author xuweinan
 */
public class ClasspathXmlLoader extends FileSystemSQLLoader {
    private static final Logger logger = LoggerFactory.getLogger(ClasspathXmlLoader.class);

    private XPathFactory xfactory = XPathFactory.newInstance();

    @Override
    public String getSqlTemplateSrc(String id) {
        String[] cmds = id.split("\\.");
        // 检查缓存
        if (this.use_cache && this.sqlSourceMap.containsKey(id) && (!this.autoCheck || !this.isModified(id)))
            return this.sqlSourceMap.get(id);
        // 物理读取
        String xmlStr = getCmdXml(id);
        Document cmdDoc = stringToDoc(xmlStr);
        XPath xpath = xfactory.newXPath();
        XPathExpression cmdPath;
        try {
            cmdPath = xpath.compile(String.format("//commands//command[@id=\"%s\"]", cmds[1]));
            String tempSrc = (String) cmdPath.evaluate(cmdDoc, XPathConstants.STRING);
            Long version = this.getFile(id).lastModified();
            this.sqlSourceMap.put(id, tempSrc);
            this.sqlSourceVersion.put(id, version);
            return (String) cmdPath.evaluate(cmdDoc, XPathConstants.STRING);
        } catch (XPathExpressionException e) {
            throw new SQLException(SQLException.CANNOT_GET_SQL, "解析模板文件异常", e);
        }
    }

    private String getCmdXml(String id) {
        String content = null;
        try {
            InputStream in = new FileInputStream(this.getFile(id));
            content = readString(in, Charset.forName(charset));
        } catch (IOException e) {
            logger.error("读取命令文件出错", e);
        }
        return content;
    }

    /**
     * String 转 XML org.w3c.dom.Document
     */
    private Document stringToDoc(String xmlStr) {
        // 字符串转XML
        Document doc = null;
        try {
            StringReader sr = new StringReader(xmlStr);
            InputSource is = new InputSource(sr);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder;
            builder = factory.newDocumentBuilder();
            doc = builder.parse(is);
        } catch (ParserConfigurationException e) {
            logger.error("pc", e);
        } catch (SAXException e) {
            logger.error("sax", e);
        } catch (IOException e) {
            logger.error("io", e);
        }
        return doc;
    }

    private String readString(InputStream in, Charset charset) throws IOException {
        Assert.notNull(in, "No InputStream specified");
        StringBuilder out = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(in, charset));
        String tmp;
        while ((tmp = br.readLine()) != null) {
            out.append(tmp).append("\n");
        }
        br.close();
        return out.toString();
    }

    @Override
    protected File getFile(String id) {
        String cmdFileName = id.substring(0, id.lastIndexOf("."));
        String path = "/" + (StringUtils.isEmpty(this.getRootUri()) ? "" : (this.getRootUri() + "/")) + cmdFileName + ".xml";
        String uri;
        try {
            uri = this.getClass().getResource(path).getPath();
        } catch (Exception e) {
            throw new RuntimeException("无法获取指定文件资源：" + path, e);
        }
        return new File(uri);
    }
}