package org.spin.web.filter;

import org.spin.jpa.core.BaseUser;
import org.spin.sys.EnvCache;
import org.spin.sys.SessionUser;
import org.spin.sys.auth.SecretManager;
import org.spin.util.SessionUtils;
import org.spin.util.StringUtils;

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

    private SecretManager secretManager;

    public TokenResolveFilter(SecretManager secretManager) {
        this.secretManager = secretManager;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        String token = request.getParameter("token");
        try {
            String userId = secretManager.validateToken(token);
            EnvCache.putLocalParam("token", token);
            SessionUtils.setCurrentUser(BaseUser.ref(Long.parseLong(userId)));
        } catch (Exception ignore) {
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}
