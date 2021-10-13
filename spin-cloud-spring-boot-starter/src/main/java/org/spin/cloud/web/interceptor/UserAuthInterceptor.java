package org.spin.cloud.web.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.cloud.feign.FeignInterceptor;
import org.spin.cloud.util.Env;
import org.spin.cloud.vo.CurrentUser;
import org.spin.core.ErrorCode;
import org.spin.core.util.NetUtils;
import org.spin.core.util.StringUtils;
import org.spin.web.AuthLevel;
import org.spin.web.InternalWhiteList;
import org.spin.web.ScopeType;
import org.spin.web.annotation.Auth;
import org.spin.web.annotation.Author;
import org.spin.web.util.RequestUtils;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * 用户权限拦截器
 *
 * @author wangy QQ 837195190
 * <p>Created by wangy on 2019/3/14.</p>
 */
public class UserAuthInterceptor implements HandlerInterceptor, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(UserAuthInterceptor.class);
    private static final ThreadLocal<Long> EXECUTION_TIME = new ThreadLocal<>();

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {

        // 是否调用方法
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        EXECUTION_TIME.set(System.currentTimeMillis());

        // 环境检测
        String profile = request.getHeader(FeignInterceptor.X_APP_PROFILE);
        if (StringUtils.isNotEmpty(profile)) {
            if (!Objects.equals(profile, Env.getActiveProfiles())) {
                logger.error("禁止跨环境的服务调用 源环境: {}[{}], 目标环境: {}[{}]",
                    StringUtils.trimToSpec(request.getHeader(FeignInterceptor.X_APP_NAME), "GATEWAY"),
                    profile,
                    Env.getAppName(),
                    Env.getActiveProfiles());
                return false;
            }
        }

        // 认证信息判断
        Method method = ((HandlerMethod) handler).getMethod();

        Auth authAnno = AnnotatedElementUtils.getMergedAnnotation(method, Auth.class);
        if (null == authAnno) {
            CurrentUser.clearCurrent();
            RequestUtils.error(response, ErrorCode.OTHER, "接口定义不正确");
            return false;
        }

        // 用户信息
        String user = request.getHeader(HttpHeaders.FROM);
        CurrentUser currentUser = null;
        if (StringUtils.isNotBlank(user)) {
            currentUser = CurrentUser.setCurrent(user);
        }

        if (authAnno.openAuth()) {
            if (null == currentUser || !currentUser.isOpenAuth()) {
                RequestUtils.error(response, ErrorCode.ACCESS_DENIED, "接口仅允许开放授权用户访问: " + request.getRequestURI());
                return false;
            }
        } else {
            if (authAnno.value() != AuthLevel.NONE && null != currentUser && currentUser.isOpenAuth()) {
                RequestUtils.error(response, ErrorCode.ACCESS_DENIED, "该接口不允许开放授权用户调用: " + request.getRequestURI());
                return false;
            }

            boolean internal = internalRequest(request);
            if (authAnno.scope() == ScopeType.INTERNAL && !internal) {
                CurrentUser.clearCurrent();
                logger.info("接口[{}]仅允许内部调用, 实际来源[{}-{} <-- {}]", request.getRequestURI(),
                    request.getRemoteHost(), request.getRemoteAddr(), request.getHeader(HttpHeaders.USER_AGENT));
                RequestUtils.error(response, ErrorCode.ACCESS_DENIED, "接口仅允许内部调用: " + request.getRequestURI());
                return false;
            }

            AuthLevel authLevel = authAnno.value();
            boolean auth = AuthLevel.NONE != authLevel;
            if (auth && authAnno.scope() == ScopeType.OPEN_UNAUTH) {
                auth = !internal;
            }

            if (null == currentUser) {
                CurrentUser.clearCurrent();
                if (auth) {
                    RequestUtils.error(response, ErrorCode.ACCESS_DENIED, "该接口不允许匿名访问: " + request.getRequestURI());
                    return false;
                }
            }

            if (AuthLevel.AUTHORIZE == authAnno.value()) {
                String authName = authAnno.name();
                if (StringUtils.isEmpty(authName)) {
                    authName = method.getDeclaringClass().getName() + "-" + method.getName();
                }
                authName = "API:" + authName;

                Env.setCurrentApiCode(authName);
            }
        }
        return true;
    }

    @Override
    public void postHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, ModelAndView modelAndView) {
        Long startTime = EXECUTION_TIME.get();
        if (null != startTime) {
            long executionTime = System.currentTimeMillis() - startTime;
            EXECUTION_TIME.remove();
            String resource;
            String author = "匿名";
            if ((handler instanceof HandlerMethod)) {
                Method method = ((HandlerMethod) handler).getMethod();
                resource = method.toGenericString();
                if (executionTime > 1000L) {
                    Author authorAnno = AnnotatedElementUtils.getMergedAnnotation(method, Author.class);
                    if (null != authorAnno) {
                        author = StringUtils.join(authorAnno.value());
                    }
                }

            } else {
                resource = request.getRequestURI();
            }

            if (executionTime > 1000L) {
                logger.warn("请求{}的执行时间过长, 作者: {}, 总计耗时: {}ms", resource, author, executionTime);
            } else {
                logger.debug("请求{}总计耗时: {}ms", resource, executionTime);
            }
        }
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, Exception ex) {
        CurrentUser.clearCurrent();
    }

    /**
     * 判断请求是否来自内部
     * <pre>
     *     内部的定义:
     *     1. 不允许来源于网关
     *     2. 在白名单中，或者来源于局域网
     *     3. 带有服务来源标识头部
     * </pre>
     *
     * @param request 请求
     * @return 是否来自内部
     */
    private boolean internalRequest(HttpServletRequest request) {
        return !StringUtils.toStringEmpty(request.getHeader(HttpHeaders.REFERER)).endsWith("GATEWAY")
            && (InternalWhiteList.containsOne(request.getRemoteAddr(), request.getRemoteHost())
            // || NetworkUtils.inSameVlan(request.getRemoteHost()) || NetworkUtils.inSameVlan(request.getRemoteAddr())
            || NetUtils.isInnerIP(request.getRemoteHost()) || NetUtils.isInnerIP(request.getRemoteAddr()))
            && StringUtils.isNotEmpty(request.getHeader(FeignInterceptor.X_APP_NAME));
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
