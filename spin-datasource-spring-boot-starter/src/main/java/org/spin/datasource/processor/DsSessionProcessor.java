package org.spin.datasource.processor;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * @author TaoYu
 * @since 2.5.0
 */
public class DsSessionProcessor extends DsProcessor {

    /**
     * session开头
     */
    private static final String SESSION_PREFIX = "#session";

    @Override
    public boolean matches(String key) {
        return key.startsWith(SESSION_PREFIX);
    }

    @Override
    public String doDetermineDatasource(MethodInvocation invocation, String key) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        return request.getSession().getAttribute(key.substring(9)).toString();
    }
}
