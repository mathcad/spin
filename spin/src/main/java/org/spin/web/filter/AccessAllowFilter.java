package org.spin.web.filter;

import org.spin.core.util.StringUtils;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 允许跨域访问
 * Created by xuweinan on 2016/10/11.
 *
 * @author xuweinan
 */
public class AccessAllowFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {
        // do nothing
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String originHeader = httpResponse.getHeader("Access-Control-Allow-Origin");
        if (StringUtils.isBlank(originHeader))
            httpResponse.addHeader("Access-Control-Allow-Origin", "*");
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // do nothing
    }
}
