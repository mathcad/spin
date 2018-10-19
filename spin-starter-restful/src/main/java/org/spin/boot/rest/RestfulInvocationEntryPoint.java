package org.spin.boot.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.boot.annotation.DateFormat;
import org.spin.boot.converter.RestfulExceptionHandler;
import org.spin.boot.properties.SpinWebPorperties;
import org.spin.core.ErrorCode;
import org.spin.core.SpinContext;
import org.spin.core.auth.Authenticator;
import org.spin.core.auth.SecretManager;
import org.spin.core.inspection.ArgumentsDescriptor;
import org.spin.core.inspection.MethodDescriptor;
import org.spin.core.session.SessionManager;
import org.spin.core.session.SessionUser;
import org.spin.core.throwable.SimplifiedException;
import org.spin.core.util.CollectionUtils;
import org.spin.core.util.DateUtils;
import org.spin.core.util.JsonUtils;
import org.spin.core.util.ObjectUtils;
import org.spin.core.util.StringUtils;
import org.spin.core.util.excel.ExcelGrid;
import org.spin.core.util.excel.ExcelModel;
import org.spin.data.core.IEntity;
import org.spin.data.core.Page;
import org.spin.data.query.QueryParam;
import org.spin.data.util.EntityUtils;
import org.spin.web.RestfulResponse;
import org.spin.web.annotation.Needed;
import org.spin.web.annotation.Payload;
import org.spin.web.annotation.RestfulMethod;
import org.spin.web.view.ModelExcelView;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartRequest;
import org.springframework.web.servlet.ModelAndView;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import javax.servlet.http.HttpServletRequest;

/**
 * restful请求分发
 * <p>Created by xuweinan on 2017/9/13.</p>
 *
 * @author xuweinan
 */
@Controller
@RequestMapping("/")
@ConditionalOnProperty("spin.web.restfulPrefix")
public class RestfulInvocationEntryPoint implements ApplicationContextAware {
    private static final Logger logger = LoggerFactory.getLogger(RestfulInvocationEntryPoint.class);
    private static final String PATH_NOT_VALID = "请求的路径不正确";
    private static final String REQUEST_BODY_NAME = "request_body_param";
    private static final Function<Parameter, Boolean> checkNeeded = p -> Objects.nonNull(p.getAnnotation(Needed.class));

    @Autowired(required = false)
    private Authenticator authenticator;

    @Autowired(required = false)
    private SecretManager secretManager;

    @Autowired
    private SpinWebPorperties webPorperties;

    private ApplicationContext applicationContext;

    @ResponseBody
    @RequestMapping(value = "${spin.web.restfulPrefix}/**")
    public RestfulResponse exec(HttpServletRequest request, @RequestBody(required = false) String requestBody) {
        String[] resc = request.getRequestURI().substring(request.getRequestURI().indexOf(webPorperties.getRestfulPrefix()) + webPorperties.getRestfulPrefix().length() + 1).split("/");
        if (resc.length != 2) {
            return RestfulResponse.error(new SimplifiedException(PATH_NOT_VALID));
        }
        String module = resc[0];
        String service = resc[1];
        if (StringUtils.isNotEmpty(requestBody)) {
            request.setAttribute(REQUEST_BODY_NAME, requestBody);
        }
        return exec(module, service, request);
    }

    @ResponseBody
    @RequestMapping(value = "${spin.web.restfulPrefix}/plain/**")
    public String plainExec(HttpServletRequest request, @RequestBody(required = false) String requestBody) {
        String[] resc = request.getRequestURI().substring(request.getRequestURI().indexOf(webPorperties.getRestfulPrefix()) + webPorperties.getRestfulPrefix().length() + 1).split("/");
        if (resc.length != 3) {
            return PATH_NOT_VALID;
        }
        String module = resc[1];
        String service = resc[2];
        if (StringUtils.isNotEmpty(requestBody)) {
            request.setAttribute(REQUEST_BODY_NAME, requestBody);
        }
        RestfulResponse restfulResponse = exec(module, service, request);

        if (200 != restfulResponse.getCode()) {
            return restfulResponse.getMessage();
        } else {
            return JsonUtils.toJson(restfulResponse.getData());
        }

    }

    @RequestMapping(value = "${spin.web.restfulPrefix}/expExcel/**")
    public ModelAndView exportExec(HttpServletRequest request, String grid) {
        ModelAndView mv = new ModelAndView();
        if (StringUtils.isEmpty(grid)) {
            return addCodeAndMsg(mv, -1, "未定义导出格式");
        }

        ExcelGrid g;
        try {
            g = JsonUtils.fromJson(grid, ExcelGrid.class);
        } catch (Exception e) {
            logger.error("导出格式定义不正确", e);
            return addCodeAndMsg(mv, -1, "导出格式定义不正确");
        }
        String[] resc = request.getRequestURI().substring(request.getRequestURI().indexOf(webPorperties.getRestfulPrefix()) + webPorperties.getRestfulPrefix().length() + 1).split("/");
        if (resc.length != 3) {
            return addCodeAndMsg(mv, -1, PATH_NOT_VALID);
        }
        String module = resc[1];
        String service = resc[2];
        RestfulResponse restfulResponse = exec(module, service, request);

        if (200 != restfulResponse.getCode()) {
            return addCodeAndMsg(mv, restfulResponse.getCode(), restfulResponse.getMessage());
        } else {
            Object data = restfulResponse.getData();
            Iterable<?> excelData;
            if (data instanceof ExcelModel) {
                g = ((ExcelModel) data).getGrid();
                excelData = ((ExcelModel) data).getData();
            } else if (data instanceof Iterable) {
                excelData = (Iterable) data;
            } else if (data instanceof Page) {
                excelData = ((Page) data).getRows();
            } else if (data instanceof IEntity) {
                excelData = CollectionUtils.ofArrayList(data);
            } else {
                return addCodeAndMsg(mv, -1, "请求的服务未返回数据列表，不能导出");
            }
            ModelExcelView mev = new ModelExcelView(new ExcelModel(g, excelData));
            mv.setView(mev);
            return mv;
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    private RestfulResponse exec(String module, String service, HttpServletRequest request) {
        if (StringUtils.isEmpty(module) || StringUtils.isEmpty(service)) {
            return RestfulResponse.error(new SimplifiedException("未完全指定请求的资源"));
        }

        if (StringUtils.toLowerCase(service).endsWith(".action")) {
            service = service.substring(0, service.length() - 7);
        } else if (StringUtils.toLowerCase(service).endsWith(".do")) {
            service = service.substring(0, service.length() - 3);
        }

        if (StringUtils.isEmpty(service)) {
            return RestfulResponse.error(new SimplifiedException("未指定请求的服务"));
        }

        logger.info("Invoke ModuleName: {}, ServiceName: {}", module, service);

        try {
            request.setCharacterEncoding("UTF-8");
        } catch (UnsupportedEncodingException e) {
            logger.error("Do not support UTF-8 encoding");
        }

        List<MethodDescriptor> services = SpinContext.getRestMethod(module, service);

        if (Objects.nonNull(services) && !services.isEmpty()) {
            List<ArgumentsDescriptor> argumentsDescriptors = new ArrayList<>(services.size());
            for (MethodDescriptor descriptor : services) {
                argumentsDescriptors.add(new ArgumentsDescriptor(descriptor, resolveArgs(descriptor, request), checkNeeded));
            }

            int selected = selectMethod(argumentsDescriptors);
            if (selected < 0) {
                logger.info("无法唯一定位请求的资源{}@{}", service, module);
                return RestfulResponse.error(new SimplifiedException("无法唯一定位请求的资源"));
            }

            // token验证
            RestfulMethod rMethod = argumentsDescriptors.get(selected).getMethodDescriptor().getMethod().getAnnotation(RestfulMethod.class);
            if (null != secretManager) {
                String token = request.getParameter("token");
                if (StringUtils.isEmpty(token)) {
                    token = request.getHeader("token");
                }
                try {
                    secretManager.bindCurrentSession(token);
                } catch (SimplifiedException e) {
                    if (rMethod.auth()) {
                        return RestfulResponse.error(e);
                    }
                }
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
        int paramLen = 0;
        for (int i = 1; i != argumentsDescriptors.size(); ++i) {
            if (argumentsDescriptors.get(i).getRank() < argumentsDescriptors.get(selected).getRank()
                || argumentsDescriptors.get(i).getRank() == argumentsDescriptors.get(selected).getRank()
                && argumentsDescriptors.get(i).getArgs().length > paramLen) {
                selected = i;
                paramLen = argumentsDescriptors.get(i).getArgs().length;
            }
        }
        final int i = selected;
        // 如果最低rank值的方法调用存在不止一个，无法确定调用分发
        if (argumentsDescriptors.stream().filter(a -> a.getRank() == argumentsDescriptors.get(i).getRank() && a.getArgs().length == argumentsDescriptors.get(i).getArgs().length).count() > 1) {
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
        Class<?> type = parameter.getType();
        Type pType = parameter.getParameterizedType();
        Type actualType = null;
        Class<?> cType = parameter.getType().getComponentType();
        if (pType instanceof ParameterizedType) {
            Type[] ts = ((ParameterizedType) pType).getActualTypeArguments();
            actualType = ts.length > 0 ? ts[0] : null;
        }

        // 方法参数接收单个文件
        if (MultipartFile.class.isAssignableFrom(type)) {
            if (!(request instanceof MultipartRequest)) {
                return null;
            }
            List<MultipartFile> files = ((MultipartRequest) request).getFiles(parameterName);
            if (files.isEmpty() || files.size() > 1) {
                return null;
            }
            return files.get(0);
        }

        // 方法参数为文件集合类型
        if (Collection.class.isAssignableFrom(type)
            && (null == actualType || actualType instanceof Class && MultipartFile.class.isAssignableFrom((Class) actualType))) {
            // 方法参数中含有泛型信息，且泛型类型为文件
            Collection<MultipartFile> value = JsonUtils.fromJson("[]", pType);
            if (request instanceof MultipartRequest) {
                value.addAll(((MultipartRequest) request).getFiles(parameterName));
            }
            if (actualType != null || !value.isEmpty()) {
                return value;
            }
        }

        // 方法参数为文件数组类型
        if (null != cType && MultipartFile.class.isAssignableFrom(cType)) {
            if (!(request instanceof MultipartRequest)) {
                return null;
            }
            List<MultipartFile> files = ((MultipartRequest) request).getFiles(parameterName);
            return files.toArray(new MultipartFile[0]);
        }

        //方法参数为非文件类型时的处理，从requestBody，attribute，parameter中获取所有指定名称参数，优先级从前到后，找到为止
        Object[] values = extractArg(parameter, parameterName, request);
        Object value;

        if (Objects.isNull(values) || values.length == 0) {
            // 请求中没有指定参数，直接返回
            return null;
        }

        if (!type.isArray() && !Iterable.class.isAssignableFrom(type) && values.length > 1) {
            // 参数不接收数组/集合类型，但请求中参数为多个，直接返回null
            return null;
        }

        if (QueryParam.class.isAssignableFrom(type)) {
            // 参数为QueryParam对象，特殊处理
            value = values[0];
            return QueryParam.parseFromJson(value.toString());
        }

        if (type.isArray() || Iterable.class.isAssignableFrom(type)) {
            // 参数为数组/集合类型，直接json转换
            boolean isStr = actualType instanceof Class && CharSequence.class.isAssignableFrom((Class<?>) actualType)
                || null != cType && CharSequence.class.isAssignableFrom(cType);
            String json = assembleJson(values, isStr);
            try {
                // 用json转换（使用泛型类型参数）
                return JsonUtils.fromJson(json, pType);
            } catch (Exception ignore) {
                // 使用json+泛型参数转换失败时，使用普通类型转换
                return JsonUtils.fromJson(json, type);
            }
        }

        // 方法参数为普通Java对象（非数组/集合类型）时，只取请求中找到的第一个参数来处理
        value = values[0];

        // 时间日期类型的处理
        DateFormat df = parameter.getAnnotation(DateFormat.class);
        if (null != df) {
            String fmt = df.value();
            switch (type.getName()) {
                case "java.util.Date":
                case "java.sql.Date":
                case "java.sql.Time":
                case "java.sql.Timestamp":
                    return DateUtils.toDate(value.toString(), fmt);
                case "java.util.Calendar":
                    Calendar c = Calendar.getInstance();
                    c.setTime(DateUtils.toDate(value.toString(), fmt));
                    return c;
                case "java.time.temporal.Temporal":
                case "java.time.LocalDateTime":
                    return DateUtils.toLocalDateTime(value.toString(), fmt);
                case "java.time.LocalTime":
                    return DateUtils.toLocalDateTime(value.toString(), fmt).toLocalTime();
                case "java.time.LocalDate":
                    return DateUtils.toLocalDateTime(value.toString(), fmt).toLocalDate();
                default:
                    return null;
            }
        }

        try {
            // 先尝试基本类型的转换
            return ObjectUtils.convert(type, value);
        } catch (ClassCastException e) {
            try {
                // 基本类型失败时，用json转换（使用泛型类型参数）
                return JsonUtils.fromJson(value.toString(), pType);
            } catch (Exception ignore) {
                // 使用json+泛型参数转换失败时，使用普通类型转换
                return JsonUtils.fromJson(value.toString(), type);
            }
        }
    }

    /**
     * 根据parameter信息，从对象中抽取实参
     *
     * @param parameter     方法形参定义
     * @param parameterName 参数名称
     * @param request       请求
     * @return 实参
     */
    private Object[] extractArg(Parameter parameter, String parameterName, HttpServletRequest request) {
        Object[] values;
        Object value = null;
        if (Objects.nonNull(parameter.getAnnotation(Payload.class))) {
            // RequestBody参数处理
            value = request.getAttribute(REQUEST_BODY_NAME);
        }
        if (Objects.isNull(value)) {
            value = request.getAttribute(parameterName);
        }
        if (Objects.isNull(value)) {
            values = request.getParameterValues(parameterName);
        } else {
            values = new Object[]{value};
        }
        return values;
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
                if (logger.isErrorEnabled()) {
                    logger.error("索引为{}的参数不能为空", JsonUtils.toJson(descriptor.getNeededNulls()));
                }
                return RestfulResponse.error(ErrorCode.INVALID_PARAM);
            }

            if (SpinContext.DEV_MODE && logger.isTraceEnabled()) {
                logger.trace("Invoke method: {}", descriptor.getMethodDescriptor().getMethodName());
                for (int idx = 0; idx != descriptor.getArgs().length; ++idx) {
                    logger.trace("Parameter info: index[{}] name[{}], value[{}]", idx, descriptor.getMethodDescriptor().getParamNames()[idx], descriptor.getArgs()[idx]);
                }
            }

            // 为方法描述器设置执行对象
            if (!descriptor.getMethodDescriptor().hasTarget()) {
                try {
                    Object bean = applicationContext.getBean(descriptor.getMethodDescriptor().getCls());
                    descriptor.getMethodDescriptor().setTarget(bean);
                } catch (BeansException e) {
                    logger.error("获取Service Bean失败", e);
                    return RestfulResponse.error(new SimplifiedException("无法获取服务提供者"));
                }
            }

            try {
                Object result = descriptor.invoke();
                if (result instanceof IEntity<?> && anno.fetchDepth() > -1) {
                    result = EntityUtils.getDTO(result, anno.fetchDepth());
                }
                return RestfulResponse.ok(result);
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
            } catch (Exception e) {
                logger.error("服务调用错误", e);
                return RestfulResponse.error(new SimplifiedException("服务调用错误"));
            }
        } else {
            return RestfulResponse.error(ErrorCode.ACCESS_DENINED);
        }
    }

    private ModelAndView addCodeAndMsg(ModelAndView mv, int code, String msg) {
        mv.addObject("code", code);
        mv.addObject("message", msg);
        return mv;
    }

    /**
     * 将对象数组组装成合法的json数组字符串
     *
     * @param values 对象数组
     * @param isStr  是否强制转成字符串集合
     * @return json数组字符串
     */
    private String assembleJson(Object[] values, boolean isStr) {
        String json;
        char s = values[0].toString().trim().charAt(0);
        if (values.length == 1) {
            json = values[0].toString().trim();
            if (s != '[') {
                if (s != '"' && s != '\'' && (s != '{' || isStr)) {
                    json = "\"" + json + "\"";
                }
                json = "[" + json + "]";
            }
        } else {
            StringBuilder b = new StringBuilder();
            for (int i = 0; i < values.length; i++) {
                if (null == values[i]) {
                    continue;
                }
                if (i > 0) {
                    b.append(",");
                }
                if (s != '"' && s != '\'' && (s != '{' && s != '[' || isStr)) {
                    b.append("\"").append(values[i]).append("\"");
                } else {
                    b.append(values[i]);
                }
            }
            json = "[" + b.toString() + "]";
        }
        return json;
    }
}
