package org.infrastructure.web.Filters;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * 适用于（静态压缩） 只是在顶部添加GZip的压缩标记
 * 
 * @author zhou
 * 
 */
@SuppressWarnings("unchecked")
public class GZipHeaderFilter implements javax.servlet.Filter {

	static Logger logger = LoggerFactory.getLogger(GZipHeaderFilter.class);
	static HashMap<String, String> MIME_TYPE = new HashMap<String, String>();
	static {
		MIME_TYPE.put(".css", "text/css");
		MIME_TYPE.put(".js", "text/javascript");
	}

	public Map headers = new HashMap();

	public void destroy() {
	}

	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
			throws IOException, ServletException {
		if (req instanceof HttpServletRequest) {
			filterHeader((HttpServletRequest) req, (HttpServletResponse) res, chain);
		} else {
			chain.doFilter(req, res);
		}
	}

	/**
	 * 头部拦截
	 * 
	 * @param request
	 * @param response
	 * @param chain
	 * @throws IOException
	 * @throws ServletException
	 */
	public void filterHeader(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		if (isGZipEncoding(request)) {
			for (Iterator it = headers.entrySet().iterator(); it.hasNext();) {
				Map.Entry entry = (Map.Entry) it.next();
				response.addHeader((String) entry.getKey(), (String) entry.getValue());
			}
			String srcLocation = request.getRequestURI().substring(0, request.getRequestURI().length() - 5);
			String gzipPostfix = srcLocation.substring(srcLocation.lastIndexOf("."));
			if (MIME_TYPE.containsKey(gzipPostfix)) {
				response.setContentType(MIME_TYPE.get(gzipPostfix));
			}
			chain.doFilter(request, response);
		} else {
			// 非Gzip支持浏览器，直接访问js文件
			String srcLocation = request.getRequestURI().substring(0, request.getRequestURI().length() - 5);
			response.sendRedirect(srcLocation);
			chain.doFilter(request, response);
		}
	}

	/**
	 * 初始化
	 */
	public void init(FilterConfig config) throws ServletException {
		String headersStr = config.getInitParameter("headers");
		String[] headersl = headersStr.split(",");
		for (int i = 0; i < headersl.length; i++) {
			String[] temp = headersl[i].split("=");
			this.headers.put(temp[0].trim(), temp[1].trim());
		}
	}

	/**
	 * 判断浏览器是否支持GZIP
	 * 
	 * @param request
	 * @return
	 */
	private static boolean isGZipEncoding(HttpServletRequest request) {
		boolean flag = false;
		String encoding = request.getHeader("Accept-Encoding");
		if (encoding.indexOf("gzip") != -1) {
			flag = true;
		}
		return flag;
	}
}
