package org.spin.web.filter;

import org.spin.core.auth.SecretManager;
import org.spin.core.throwable.SimplifiedException;
import org.spin.core.util.JsonUtils;
import org.spin.core.util.StringUtils;
import org.spin.web.RestfulResponse;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

/**
 * rest请求的token处理
 * Created by xuweinan on 2016/10/3.
 *
 * @author xuweinan
 */
public class TokenResolveFilter implements Filter {

    private SecretManager secretManager;

    public TokenResolveFilter(SecretManager secretManager) {
        this.secretManager = secretManager;
    }

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        String token = request.getParameter("token");
        if (StringUtils.isNotBlank(token)) {
            try {
                secretManager.bindCurrentSession(token);
                chain.doFilter(request, response);
            } catch (SimplifiedException e) {
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write(JsonUtils.toJson(RestfulResponse.error(e)));
            }
        } else {
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {
    }
}
