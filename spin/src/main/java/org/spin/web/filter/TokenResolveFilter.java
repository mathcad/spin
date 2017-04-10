package org.spin.web.filter;

import org.spin.sys.EnvCache;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * rest请求的token处理
 * Created by xuweinan on 2016/10/3.
 *
 * @author xuweinan
 */
public class TokenResolveFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        EnvCache.putLocalParam("token", request.getParameter("token"));
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}
