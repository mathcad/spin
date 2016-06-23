package org.infrastructure.jpa.core.sqlmap;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.infrastructure.freemarker.EnumValueFunc;
import org.infrastructure.freemarker.ValidValueFunc;
import org.infrastructure.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * 利用FreeMarker的sql装载器
 */
public class SqlLoader {
	static Logger logger = LoggerFactory.getLogger(SqlLoader.class);

	XPathFactory xfactory = XPathFactory.newInstance();

	/** 绝对路径，优先配置 */
	String realDir = null;

	/** 未配置路径，可用相对路径 */
	String subDir = null;

	/**
	 * 得到配置目录
	 * 
	 * @return
	 */
	public String getMapDir() {
		if (StringUtils.isEmpty(realDir)) {
			try {
				String startupPath = this.getClass().getResource("/").getPath();
				// SqlLoader.class.getResource("/").getPath();
				logger.info(startupPath);
				startupPath = java.net.URLDecoder.decode(startupPath, "utf-8");
				realDir = startupPath + subDir;
			} catch (Exception e) {
				logger.info("", e);
			}
		}

		return realDir;
	}

	/**
	 * 得到参数化的语句
	 * 
	 * @param cmdName
	 * @param model
	 * @return
	 */
	public String getSql(String cmdName, Map<String, ?> model) {
		try {
			logger.info(cmdName);
			Configuration cfg = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
			cfg.setEncoding(Locale.CHINESE, "UTF-8");
			cfg.setNumberFormat("#");
			cfg.setSharedVariable("V", new ValidValueFunc());
			cfg.setSharedVariable("E", new EnumValueFunc());
			StringTemplateLoader sTmpLoader = new StringTemplateLoader();
			cfg.setTemplateLoader(sTmpLoader);
			sTmpLoader.putTemplate(cmdName, getSqlTemplate(cmdName));
			Template template = cfg.getTemplate(cmdName);
			StringWriter writer = new StringWriter();
			template.process(model, writer);
			String sql = writer.toString();
			logger.info(sql);
			return sql;
		} catch (TemplateException e) {
			throw new RuntimeException("Parse sql failed:" + cmdName, e);
		} catch (IOException e) {
			throw new RuntimeException("Parse sql failed:" + cmdName, e);
		}
	}

	/**
	 * 取得配置文件中的SQL文模板
	 * 
	 * @param cmdName
	 *            {file}.{commandName}
	 * @return
	 * @throws Exception
	 */
	public String getSqlTemplate(String cmdName) throws FileNotFoundException {

		String[] cmds = cmdName.split("\\.");
		String xmlStr = getCmdXml(cmds[0]);
		Document cmdDoc = stringToDoc(xmlStr);
		XPath xpath = xfactory.newXPath();
		XPathExpression cmdPath;

		try {
			cmdPath = xpath.compile("//commands//command[@id=\"" + cmds[1] + "\"]");
			String cmdText = (String) cmdPath.evaluate(cmdDoc, XPathConstants.STRING);
			return cmdText;
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
		// logger.info(this.getClass().getResource("/" +subDir+"/" + filePath));
		return this.getClass().getResourceAsStream("/" + subDir + "/" + filePath);
	}

	/**
	 * String 转 XML org.w3c.dom.Document
	 */
	public static Document stringToDoc(String xmlStr) {
		// 字符串转XML
		Document doc = null;
		try {
			xmlStr = new String(xmlStr.getBytes(), "UTF-8");
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

	/**
	 * @param realDir
	 *            the realDir to set
	 */
	public void setRealDir(String realDir) {
		this.realDir = realDir;
	}

	/**
	 * @param subDir
	 *            the subDir to set
	 */
	public void setSubDir(String subDir) {
		this.subDir = subDir;
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
