package org.spin.common.web.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.common.vo.CurrentUser;
import org.spin.common.web.annotation.Auth;
import org.spin.core.ErrorCode;
import org.spin.core.util.JsonUtils;
import org.spin.core.util.NetUtils;
import org.spin.core.util.StringUtils;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;

/**
 * 用户权限拦截器
 *
 * @author wangy QQ 837195190
 * <p>Created by wangy on 2019/3/14.</p>
 */
public class UserAuthInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(UserAuthInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        // 是否调用方法
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        // 认证信息判断
        Method method = ((HandlerMethod) handler).getMethod();

        Auth authAnno = AnnotatedElementUtils.getMergedAnnotation(method, Auth.class);
        if (null == authAnno) {
            CurrentUser.clearCurrent();
            responseWrite(response, ErrorCode.OTHER, "接口定义不正确");
            return false;
        }

        boolean internal = internalRequest(request);
        if (authAnno.scope() == ScopeType.INTERNAL && !internal) {
            CurrentUser.clearCurrent();
            logger.info("该接口[{}]仅允许内部调用, 实际来源[{}-{}]", request.getRequestURI(), request.getRemoteHost(), request.getRemoteAddr());
            responseWrite(response, ErrorCode.ACCESS_DENINED, "该接口仅允许内部调用: " + request.getRequestURI());
            return false;
        }
        // 用户信息
        String user = request.getHeader(HttpHeaders.FROM);
        CurrentUser currentUser = null;
        if (StringUtils.isNotBlank(user)) {
            currentUser = CurrentUser.setCurrent(user);
        }

        AuthLevel authLevel = authAnno.value();
        boolean auth = AuthLevel.NONE != authLevel;
        if (auth && authAnno.scope() == ScopeType.OPEN_UNAUTH) {
            auth = !internal;
        }

        if (null == currentUser) {
            CurrentUser.clearCurrent();
            if (auth) {
                responseWrite(response, ErrorCode.ACCESS_DENINED, "该接口不允许匿名访问: " + request.getRequestURI());
                return false;
            }
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        // do nothing
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        CurrentUser.clearCurrent();
    }

    /**
     * 判断请求是否来自内部
     * <pre>
     *     内部的定义:
     *     1. 不允许来源于网关
     *     2. 在白名单中，或者来源于局域网
     * </pre>
     *
     * @param request 请求
     * @return 是否来自内部
     */
    private boolean internalRequest(HttpServletRequest request) {
        return !StringUtils.toStringEmpty(request.getHeader(HttpHeaders.REFERER)).endsWith("GATEWAY")
            && (InternalWhiteList.containsOne(request.getRemoteAddr(), request.getRemoteHost())
            // || NetworkUtils.inSameVlan(request.getRemoteHost()) || NetworkUtils.inSameVlan(request.getRemoteAddr())
            || NetUtils.isInnerIP(request.getRemoteHost()) || NetUtils.isInnerIP(request.getRemoteAddr()));
    }

    private void responseWrite(HttpServletResponse response, ErrorCode errorCode, String... message) {
        try {
            response.setCharacterEncoding("UTF-8");
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setHeader("Encoded", "1");
            response.getWriter().write(JsonUtils.toJson(RestfulResponse
                .error(errorCode, ((null == message || message.length == 0 || StringUtils.isEmpty(message[0])) ? errorCode.getDesc() : message[0]))));
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }
}
