package org.spin.cloud.web.interceptor;

import org.spin.cloud.util.CloudInfrasContext;
import org.spin.core.collection.Pair;
import org.spin.core.util.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.ui.ModelMap;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.WebRequestInterceptor;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 灰度发布拦截器
 * <p>解析灰度策略并绑定到当前请求上下文</p>
 * <p>Created by xuweinan on 2019/9/17</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class GrayInterceptor implements WebRequestInterceptor {

    public static final String X_GRAY_INFO = "X-Gray-Info";

    @Override
    public void preHandle(@NonNull WebRequest request) {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (null == requestAttributes) {
            RequestContextHolder.setRequestAttributes(request);
        }

        String grayInfo = request.getHeader(X_GRAY_INFO);

        if (StringUtils.isNotEmpty(grayInfo)) {
            Map<String, String> grayInfoMap = Arrays.stream(StringUtils.urlDecode(grayInfo).split(";"))
                .collect(Collectors.toMap(it -> StringUtils.trimToEmpty(it.substring(0, it.indexOf('='))),
                    it -> StringUtils.trimToEmpty(it.substring(it.indexOf('=')))));
            CloudInfrasContext.setGrayInfo(Pair.of(grayInfo, grayInfoMap));
        }
    }

    @Override
    public void postHandle(@NonNull WebRequest request, ModelMap model) {
        CloudInfrasContext.removeGrayInfo();
    }

    @Override
    public void afterCompletion(@NonNull WebRequest request, Exception ex) {
        // do nothing
    }
}
