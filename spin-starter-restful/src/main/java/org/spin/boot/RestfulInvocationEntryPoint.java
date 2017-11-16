package org.spin.boot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.boot.converter.RestfulExceptionHandler;
import org.spin.boot.properties.SpinWebPorperties;
import org.spin.core.ErrorCode;
import org.spin.core.SpinContext;
import org.spin.core.auth.Authenticator;
import org.spin.core.inspection.ArgumentsDescriptor;
import org.spin.core.inspection.MethodDescriptor;
import org.spin.core.session.SessionManager;
import org.spin.core.session.SessionUser;
import org.spin.core.throwable.SimplifiedException;
import org.spin.core.util.JsonUtils;
import org.spin.core.util.ObjectUtils;
import org.spin.core.util.StringUtils;
import org.spin.data.query.QueryParam;
import org.spin.web.RestfulResponse;
import org.spin.web.annotation.Needed;
import org.spin.web.annotation.RestfulMethod;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * restful请求分发
 * <p>Created by xuweinan on 2017/9/13.</p>
 *
 * @author xuweinan
 */
@RestController
@RequestMapping("/")
@ConditionalOnProperty("spin.web.restfulPrefix")
public class RestfulInvocationEntryPoint implements ApplicationContextAware {
    private static final Logger logger = LoggerFactory.getLogger(RestfulInvocationEntryPoint.class);

    @Autowired(required = false)
    private Authenticator authenticator;

    @Autowired
    private SpinWebPorperties webPorperties;

    private ApplicationContext applicationContext;

    @RequestMapping(value = "${spin.web.restfulPrefix}/**")
    public RestfulResponse exec(HttpServletRequest request) throws UnsupportedEncodingException {
        String[] resc = request.getRequestURI().substring(request.getRequestURI().indexOf(webPorperties.getRestfulPrefix()) + webPorperties.getRestfulPrefix().length() + 1).split("/");
        if (resc.length != 2) {
            return RestfulResponse.error(new SimplifiedException("请求的路径不正确"));
        }
        String module = resc[0];
        String service = resc[1];
        logger.info("Invoke ModuleName: {}, ServiceName: {}", module, service);

        if (StringUtils.isBlank(module) || StringUtils.isBlank(service)) {
            return RestfulResponse.error(new SimplifiedException("请求的资源不存在"));
        }

        request.setCharacterEncoding("UTF-8");

        List<MethodDescriptor> services = SpinContext.getRestMethod(module, service);
        List<ArgumentsDescriptor> argumentsDescriptors = new ArrayList<>();

        if (Objects.nonNull(services) && !services.isEmpty()) {
            for (MethodDescriptor descriptor : services) {
                argumentsDescriptors.add(new ArgumentsDescriptor(descriptor, resolveArgs(descriptor, request), p -> Objects.nonNull(p.getAnnotation(Needed.class))));
            }

            int selected = selectMethod(argumentsDescriptors);
            if (selected < 0) {
                logger.info("无法唯一定位请求的资源" + service + "@" + module);
                return RestfulResponse.error(new SimplifiedException("无法唯一定位请求的资源"));
            }

            return invoke(argumentsDescriptors.get(selected));
        } else {
            return RestfulResponse.error(new SimplifiedException("请求的资源不存在"));
        }
    }

    /**
     * 选举执行方法
     *
     * @param argumentsDescriptors 参数描述器
     * @return 选出的索引
     */
    private int selectMethod(final List<ArgumentsDescriptor> argumentsDescriptors) {
        int selected = 0;
        for (int i = 1; i != argumentsDescriptors.size(); ++i) {
            if (argumentsDescriptors.get(i).getRank() < argumentsDescriptors.get(selected).getRank()) {
                selected = i;
            }
        }
        final int i = selected;
        // 如果最低rank值的方法调用存在不止一个，无法确定调用分发
        if (argumentsDescriptors.stream().filter(a -> a.getRank() == argumentsDescriptors.get(i).getRank()).count() > 1) {
            selected = -1;
        }
        return selected;
    }

    /**
     * 解析调用方法的实参
     *
     * @param descriptor 方法描述器
     * @param request    请求
     * @return 实参数组
     */
    private Object[] resolveArgs(MethodDescriptor descriptor, HttpServletRequest request) {
        Parameter[] parameters = descriptor.getMethod().getParameters();
        String[] argNames = descriptor.getParamNames();
        Object[] args = new Object[argNames.length];

        for (int i = 0; i != argNames.length; ++i) {
            args[i] = resolveArg(parameters[i], argNames[i], request);
        }
        return args;
    }

    /**
     * 解析单个实参
     *
     * @param parameter     形参信息
     * @param parameterName 形参名称
     * @param request       请求
     * @return 解析后的实参
     */
    private Object resolveArg(Parameter parameter, String parameterName, HttpServletRequest request) {
        if (MultipartFile.class.equals(parameter.getType())) {
            if (!(request instanceof MultipartRequest)) {
                return null;
            }
            List<MultipartFile> files = ((MultipartRequest) request).getFiles(parameterName);
            return files.isEmpty() ? null : files.get(0);
        }

        if (Collection.class.isAssignableFrom(parameter.getType())) {
            if (parameter.getParameterizedType() instanceof ParameterizedType) {
                Type[] types = ((ParameterizedType) parameter.getParameterizedType()).getActualTypeArguments();
                if (request instanceof MultipartRequest && types.length > 0 && types[0] instanceof Class && MultipartFile.class.equals(types[0])) {
                    Collection<MultipartFile> value = JsonUtils.fromJson("[]", parameter.getParameterizedType());
                    if (Objects.nonNull(value)) {
                        value.addAll(((MultipartRequest) request).getFiles(parameterName));
                        return value;
                    } else {
                        return null;
                    }
                }
            } else if (request instanceof MultipartRequest && ((MultipartRequest) request).getFiles(parameterName).size() > 0) {
                Collection<MultipartFile> value = JsonUtils.fromJson("[]", parameter.getParameterizedType());
                if (Objects.nonNull(value)) {
                    value.addAll(((MultipartRequest) request).getFiles(parameterName));
                    return value;
                } else {
                    return null;
                }
            }
        }

        if (parameter.getType().isArray() && MultipartFile.class.equals(parameter.getType().getComponentType())) {
            if (!(request instanceof MultipartRequest)) {
                return null;
            }
            List<MultipartFile> files = ((MultipartRequest) request).getFiles(parameterName);
            return files.toArray(new MultipartFile[files.size()]);
        }

        Object[] values = {request.getAttribute(parameterName)};
        if (Objects.isNull(values)) {
            values = request.getParameterValues(parameterName);
        }


        if (Objects.isNull(values)) {
            return null;
        }

        if (!parameter.getType().isArray() && !Iterable.class.isAssignableFrom(parameter.getType()) && values.length > 1) {
            throw new SimplifiedException(ErrorCode.INVALID_PARAM, parameterName + "参数不匹配");
        }

        if (parameter.getType().equals(QueryParam.class)) {
            Object value = values[0];
            return QueryParam.parseFromJson(value.toString());
        }

        if (parameter.getType().isArray() || Iterable.class.isAssignableFrom(parameter.getType())) {
            return JsonUtils.fromJson(JsonUtils.toJson(values), parameter.getType());
        }

        Object value = values[0];
        try {
            return ObjectUtils.convert(parameter.getType(), value);
        } catch (ClassCastException e) {
            return JsonUtils.fromJson(value.toString(), parameter.getParameterizedType());
        }
    }

    /**
     * 调用方法并包装返回结果
     *
     * @param descriptor 参数描述器
     * @return Restful响应
     */
    private RestfulResponse invoke(ArgumentsDescriptor descriptor) {
        boolean isAllowed = false;
        RestfulMethod anno = descriptor.getMethodDescriptor().getMethod().getAnnotation(RestfulMethod.class);
        boolean needAuth = anno.auth();
        if (needAuth) {
            String authRouter = anno.authRouter();
            SessionUser user = SessionManager.getCurrentUser();
            if (user != null && authenticator.checkAuthorities(user.getId(), authRouter)) {
                isAllowed = true;
            }
        }
        if (isAllowed || !needAuth) {
            if (descriptor.getRank() > 100) {
                logger.error("索引为" + JsonUtils.toJson(descriptor.getNeededNulls()) + "的参数不能为空");
                return RestfulResponse.error(ErrorCode.INVALID_PARAM);
            }

            if (SpinContext.DEV_MODE && logger.isTraceEnabled()) {
                logger.trace("Invoke method: {}", descriptor.getMethodDescriptor().getMethodName());
                for (int idx = 0; idx != descriptor.getArgs().length; ++idx) {
                    logger.trace("Parameter info: index[{}] name[{}], value[{}]", idx, descriptor.getMethodDescriptor().getParamNames()[idx], descriptor.getArgs()[idx]);
                }
            }

            if (!descriptor.getMethodDescriptor().hasTarget()) {
                try {
                    Object bean = applicationContext.getBean(descriptor.getMethodDescriptor().getCls());
                    descriptor.getMethodDescriptor().setTarget(bean);
                } catch (BeansException ignore) {
                    logger.error("获取Service Bean失败", ignore);
                    throw new SimplifiedException(ErrorCode.INTERNAL_ERROR, "无法获取服务提供者");
                }
            }

            try {
                return RestfulResponse.ok(descriptor.getMethodDescriptor().invoke(descriptor.getArgs()));
            } catch (InvocationTargetException e) {
                Throwable cause = e.getCause();
                logger.error("服务执行异常", cause);

                if (cause instanceof SimplifiedException) {
                    return RestfulResponse.error((SimplifiedException) cause);
                }

                RestfulExceptionHandler handler = RestfulExceptionHandlerRegistry.getHandler(cause.getClass());
                if (Objects.nonNull(handler)) {
                    return RestfulResponse.error(new SimplifiedException(handler.handler(cause)));
                }

                return RestfulResponse.error(new SimplifiedException("服务执行异常"));
            } catch (Throwable e) {
                logger.error("服务调用错误", e);
                return RestfulResponse.error(new SimplifiedException("服务调用错误"));
            }
        } else {
            return RestfulResponse.error(ErrorCode.ACCESS_DENINED);
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
