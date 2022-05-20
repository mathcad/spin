package org.spin.web.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.ErrorCode;
import org.spin.core.util.CollectionUtils;
import org.spin.core.util.StringUtils;
import org.spin.web.util.RequestUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2020/6/3</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class RefererInterceptor implements HandlerInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(RefererInterceptor.class);

    private List<RefererRule> rules = Collections.emptyList();
    private final boolean allowWhenNoRule;

    /**
     * @param rules           匹配规则
     * @param allowWhenNoRule 当无匹配规则时是否直接放行
     */
    public RefererInterceptor(Map<String, String> rules, boolean allowWhenNoRule) {
        this.allowWhenNoRule = allowWhenNoRule;
    }

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        // 是否调用方法
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        if (rules.isEmpty()) {
            return true;
        }
        String referer = StringUtils.trimToEmpty(request.getHeader(HttpHeaders.REFERER));

        RefererRule detect = CollectionUtils.detect(rules, it -> it.getUrlPattern().matcher(request.getServletPath()).matches());
        if (null == detect) {
            if (allowWhenNoRule) {
                return true;
            } else {
                RequestUtils.error(response, ErrorCode.ACCESS_DENIED, "请求被拒绝: " + request.getRequestURI());
                return false;
            }
        }

        if (detect.getRefererPattern().matcher(referer).matches()) {
            return true;
        } else {
            RequestUtils.error(response, ErrorCode.ACCESS_DENIED, "请求被拒绝: " + request.getRequestURI());
            return false;
        }
    }

}
