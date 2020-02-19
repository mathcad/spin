package org.spin.common.vo;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.spin.core.util.StringUtils;
import org.spin.web.AuthLevel;
import org.spin.web.ScopeType;
import org.spin.web.annotation.Auth;
import org.spin.web.annotation.Author;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

/**
 * RequestMapping信息
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/8/8</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class RequestMappingInfoWrapper implements Serializable {
    private LinkedHashSet<String> urlPatterns;
    private LinkedHashSet<String> requestMethods;

    private String beanType;
    private String groupDesc;
    private String methodName;

    private String author;
    private String department;
    private String contact;

    private ScopeType scopeType = ScopeType.OPEN;
    private AuthLevel auth = AuthLevel.NONE;
    private String authName;
    private LinkedHashSet<String> roles;
    private String remark;

    public RequestMappingInfoWrapper(RequestMappingInfo info, HandlerMethod handlerMethod) {
        urlPatterns = new LinkedHashSet<>(info.getPatternsCondition().getPatterns());
        requestMethods = info.getMethodsCondition().getMethods().stream().map(Enum::name).collect(Collectors.toCollection(LinkedHashSet::new));

        beanType = handlerMethod.getBeanType().getName();
        methodName = handlerMethod.getMethod().getName();
        Auth authAnno = AnnotatedElementUtils.getMergedAnnotation(handlerMethod.getMethod(), Auth.class);
        if (null != authAnno) {
            scopeType = authAnno.scope();
            auth = authAnno.value();
            authName = authAnno.name();
            if (StringUtils.isEmpty(authName)) {
                authName = beanType + "-" + methodName;
            }
            authName  = "API:" + authName;
            roles = new LinkedHashSet<>(authAnno.roles().length);
            roles.addAll(Arrays.asList(authAnno.roles()));
        }

        Author authorAnno = AnnotatedElementUtils.getMergedAnnotation(handlerMethod.getMethod(), Author.class);
        if (null != authorAnno) {
            Object[] a = authorAnno.value();
            author = StringUtils.join(a, ",");
            department = authorAnno.department();
            contact = authorAnno.contact();
        }

        ApiOperation operationAnno = AnnotatedElementUtils.getMergedAnnotation(handlerMethod.getMethod(), ApiOperation.class);
        if (null != operationAnno) {
            remark = operationAnno.value();
        }

        Api apiAnno = AnnotatedElementUtils.getMergedAnnotation(handlerMethod.getMethod().getDeclaringClass(), Api.class);
        if (null != apiAnno) {
            groupDesc = (apiAnno.tags().length > 0 && StringUtils.isNotEmpty(apiAnno.tags()[0])) ? apiAnno.tags()[0] : apiAnno.value();
        }
    }

    public LinkedHashSet<String> getUrlPatterns() {
        return urlPatterns;
    }

    public LinkedHashSet<String> getRequestMethods() {
        return requestMethods;
    }

    public String getBeanType() {
        return beanType;
    }

    public String getGroupDesc() {
        return groupDesc;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getAuthor() {
        return author;
    }

    public String getDepartment() {
        return department;
    }

    public String getContact() {
        return contact;
    }

    public ScopeType getScopeType() {
        return scopeType;
    }

    public AuthLevel getAuth() {
        return auth;
    }

    public String getAuthName() {
        return authName;
    }

    public LinkedHashSet<String> getRoles() {
        return roles;
    }

    public String getRemark() {
        return remark;
    }
}
