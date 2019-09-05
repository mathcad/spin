package org.spin.common.web.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.common.internal.NetworkUtils;
import org.spin.common.util.PermissionUtils;
import org.spin.common.vo.CurrentUser;
import org.spin.common.web.InternalWhiteList;
import org.spin.common.web.RestfulResponse;
import org.spin.common.web.ScopeType;
import org.spin.common.web.annotation.Auth;
import org.spin.core.ErrorCode;
import org.spin.core.util.JsonUtils;
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
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Set;

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
            responseWrite(response, ErrorCode.OTHER, "接口定义不正确");
            return false;
        }

        boolean internal = internalRequest(request);
        if (authAnno.scope() == ScopeType.INTERNAL && !internal) {
            responseWrite(response, ErrorCode.ACCESS_DENINED, "该接口仅允许内部调用: " + request.getRequestURI());
            return false;
        }
        // 用户信息
        Enumeration<String> enumeration = request.getHeaders(HttpHeaders.FROM);
        CurrentUser currentUser = null;
        if (enumeration.hasMoreElements()) {
            String user = enumeration.nextElement();
            if (StringUtils.isNotEmpty(user)) {
                currentUser = CurrentUser.setCurrent(StringUtils.urlDecode(user));
            }
        }

        boolean auth = authAnno.value();
        if (auth && authAnno.scope() == ScopeType.OPEN_UNAUTH) {
            auth = !internal;
        }

        ErrorCode errorCode = null;
        if (null == currentUser) {
            CurrentUser.clearCurrent();
            errorCode = ErrorCode.ACCESS_DENINED;
        } else if (ErrorCode.TOKEN_EXPIRED.getCode() == -currentUser.getId().intValue()) {
            CurrentUser.clearCurrent();
            errorCode = ErrorCode.TOKEN_EXPIRED;
        } else if (ErrorCode.TOKEN_INVALID.getCode() == -currentUser.getId().intValue()) {
            CurrentUser.clearCurrent();
            errorCode = ErrorCode.TOKEN_INVALID;
        }

        if (null != errorCode && errorCode != ErrorCode.ACCESS_DENINED) {
            logger.warn("非法的Token: {}", errorCode.toString());
        }

        if (!auth) {
            return true;
        } else if (null != errorCode) {
            responseWrite(response, errorCode);
            return false;
        }

        // 权限信息
        String[] permissions = authAnno.permissions();
        if (permissions.length == 0) {
            return true;
        }

        Set<String> allPermissions = PermissionUtils.getUserPermissions(currentUser.getId());

        if (allPermissions.containsAll(Arrays.asList(permissions))) {
            return true;
        }

        // 无效的授权
        responseWrite(response, ErrorCode.ACCESS_DENINED);

        return false;

    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        // do nothing
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // do nothing
    }

    /**
     * 判断请求是否来自内部
     * <pre>
     *     内部的定义:
     *     1. 不允许来源于网关
     *     2. 在白名单中，或者属于同一子网(不允许跨VLAN)
     * </pre>
     *
     * @param request 请求
     * @return 是否来自内部
     */
    private boolean internalRequest(HttpServletRequest request) {
        return !StringUtils.toStringEmpty(request.getHeader(HttpHeaders.REFERER)).endsWith("GATEWAY")
            && (InternalWhiteList.containsOne(request.getRemoteAddr(), request.getRemoteHost())
            || NetworkUtils.inSameVlan(request.getRemoteHost()) || NetworkUtils.inSameVlan(request.getRemoteAddr()));
    }

    private void responseWrite(HttpServletResponse response, ErrorCode errorCode, String... message) {
        try {
            response.setCharacterEncoding("UTF-8");
            response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
            response.setHeader("Encoded", "1");
            response.getWriter().write(JsonUtils.toJson(RestfulResponse
                .error(errorCode, ((null == message || message.length == 0 || StringUtils.isEmpty(message[0])) ? errorCode.getDesc() : message[0]))));
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }
}
