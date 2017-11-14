package org.spin.web.filter;

import org.spin.core.auth.SecretManager;
import org.spin.core.session.SessionManager;
import org.spin.core.util.StringUtils;
import org.spin.data.core.AbstractUser;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
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
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        String token = request.getParameter("token");
        if (StringUtils.isBlank(token)) {
            HttpSession session = ((HttpServletRequest) request).getSession();
            if (SessionManager.containsSession(session.getId())) {
                SessionManager.setCurrentSessionId(session.getId());
                AbstractUser user = AbstractUser.ref(Long.parseLong(session.getAttribute("userId").toString()));
                user.setSessionId(session.getId());
                SessionManager.setCurrentUser(AbstractUser.ref(Long.parseLong(session.getAttribute("userId").toString())));
            }
        } else {
            try {
                secretManager.bindCurrentSession(token);
            } catch (Exception ignore) {
            }
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}
