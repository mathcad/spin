package org.infrastructure.jpa.core.sqlmap;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.infrastructure.freemarker.ConcurrentStrTemplateLoader;
import org.infrastructure.freemarker.EnumValueFunc;
import org.infrastructure.freemarker.ValidValueFunc;
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
import javax.xml.xpath.XPathFactory;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.Map;

/**
 * 基于FreeMarker的sql加载器
 */
public class SqlLoader {
    private static final Logger logger = LoggerFactory.getLogger(SqlLoader.class);
    private volatile boolean use_cache = true;
    private final Object mutex = new Object();

    private XPathFactory xfactory = XPathFactory.newInstance();

    /**
     * 绝对路径，优先配置
     */
    private String realDir = null;

    /**
     * 未配置路径，可用相对路径
     */
    private String subDir = null;

    private Configuration configuration;

    private ConcurrentStrTemplateLoader strTemplateLoader;

    public SqlLoader() {
        this.configuration = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
        strTemplateLoader = new ConcurrentStrTemplateLoader();
        configuration.setEncoding(Locale.CHINESE, "UTF-8");
        configuration.setNumberFormat("#");
        configuration.setSharedVariable("V", new ValidValueFunc());
        configuration.setSharedVariable("E", new EnumValueFunc());
        configuration.setTemplateLoader(strTemplateLoader);
    }

    /**
     * 得到配置目录
     *
     * @return 配置目录
     */
    public String getMapDir() {
        if (StringUtils.isEmpty(realDir)) {
            String startupPath = this.getClass().getResource("/").getPath();
            logger.info(startupPath);
            try {
                startupPath = java.net.URLDecoder.decode(startupPath, "UTF-8");
                this.realDir = startupPath + subDir;
            } catch (Exception e) {
                logger.info("decode失败", e);
            }
        }
        return this.realDir;
    }

    /**
     * 得到参数化的语句
     *
     * @param cmdName sql的path
     * @param model   参数
     * @return 参数化的sql
     */
    public String getSql(String cmdName, Map<String, ?> model) {
        try {
            logger.info(cmdName);
            Template template = null;
            if (this.use_cache && this.strTemplateLoader.containsTemplate(cmdName))
                template = this.configuration.getTemplate(cmdName);
            else {
                this.strTemplateLoader.putTemplate(cmdName, getSqlTemplateString(cmdName));
                template = this.configuration.getTemplate(cmdName);
            }
            StringWriter writer = new StringWriter();
            template.process(model, writer);
            String sql = writer.toString();
            logger.info(sql);
            return sql;
        } catch (TemplateException | IOException e) {
            throw new RuntimeException("Parse sql failed:" + cmdName, e);
        }
    }

    /**
     * 取得配置文件中的SQL模板
     *
     * @param cmdName {file}.{commandName}
     * @return sql模板语句
     * @throws FileNotFoundException
     */
    public String getSqlTemplateString(String cmdName) throws FileNotFoundException {
        String[] cmds = cmdName.split("\\.");
        String xmlStr = getCmdXml(cmds[0]);
        Document cmdDoc = stringToDoc(xmlStr);
        XPath xpath = xfactory.newXPath();
        XPathExpression cmdPath;

        try {
            cmdPath = xpath.compile(String.format("//commands//command[@id=\"%s\"]", cmds[1]));
            return (String) cmdPath.evaluate(cmdDoc, XPathConstants.STRING);
        } catch (Exception e) {
            logger.error(xmlStr);
            logger.error("解析模板命令异常" + cmdName, e);
            throw new RuntimeException("解析模板命令异常" + cmdName);
        }

    }

    private String getCmdXml(String cmdFileName) throws FileNotFoundException {
        String content = null;
        try {
            InputStream in = this.getConfigFile(cmdFileName + ".ftl");
            content = copyToString(in, Charset.forName("UTF-8"));
            in.close();
        } catch (IOException e) {
            logger.error("读取命令文件出错", e);
        }
        return content;
    }

    InputStream getConfigFile(String filePath) {
        return this.getClass().getResourceAsStream("/" + (StringUtils.isEmpty(subDir) ? "" : (subDir + "/")) + filePath);
    }

    /**
     * String 转 XML org.w3c.dom.Document
     */
    public static Document stringToDoc(String xmlStr) {
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

    public void setRealDir(String realDir) {
        this.realDir = realDir;
    }

    public void setSubDir(String subDir) {
        this.subDir = subDir;
    }

    public SqlLoader abandonCache() {
        synchronized (this.mutex) {
            this.use_cache = false;
        }
        return this;
    }

    public SqlLoader enableCache() {
        synchronized (this.mutex) {
            this.use_cache = true;
        }
        return this;
    }

    public void setCachable(boolean cachable) {
        synchronized (this.mutex) {
            this.use_cache = cachable;
        }
    }

    public static String copyToString(InputStream in, Charset charset) throws IOException {
        Assert.notNull(in, "No InputStream specified");
        StringBuilder out = new StringBuilder();
        InputStreamReader reader = new InputStreamReader(in, charset);
        char[] buffer = new char[4096];
        int bytesRead = -1;
        while ((bytesRead = reader.read(buffer)) != -1) {
            out.append(buffer, 0, bytesRead);
        }
        return out.toString();
    }
}
