package org.spin.cloud.web.interceptor;

import org.spin.cloud.idempotent.IdempotentAspect;
import org.spin.cloud.util.CloudInfrasContext;
import org.spin.core.collection.Pair;
import org.spin.core.util.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.ui.ModelMap;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.WebRequestInterceptor;

import java.util.HashMap;
import java.util.Map;

/**
 * 自定义路由拦截器
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2020/5/15</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class CustomizeRouteInterceptor implements WebRequestInterceptor {
    public static final String CUSTOMIZE_ROUTE = "X-Customize-Route";

    @Override
    public void preHandle(@NonNull WebRequest request) {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (null == requestAttributes) {
            RequestContextHolder.setRequestAttributes(request);
        }

        String customizeRoutesStr = request.getHeader(CUSTOMIZE_ROUTE);
        if (StringUtils.isNotEmpty(customizeRoutesStr)) {
            String[] split = customizeRoutesStr.split(",");
            Map<String, String> customizeRoutes = new HashMap<>(split.length);
            for (String r : split) {
                r = StringUtils.trimToEmpty(r);
                int i = r.indexOf("=");
                if (i > 0 && i < r.length() - 1) {
                    customizeRoutes.put(StringUtils.trimToEmpty(r.substring(0, i)).toUpperCase(), StringUtils.trimToEmpty(r.substring(i + 1)));
                }
            }

            CloudInfrasContext.setCustomizeRoute(Pair.of(customizeRoutesStr, customizeRoutes));
        }

        String idempotentId = request.getHeader(IdempotentAspect.IDEMPOTENT_ID);
        if (StringUtils.isNotEmpty(idempotentId)) {
            CloudInfrasContext.setIdempotentInfo(idempotentId);
        }
    }

    @Override
    public void postHandle(@NonNull WebRequest request, ModelMap model) {
        CloudInfrasContext.removeCustomizeRoute();
        CloudInfrasContext.removeIdempotentInfo();
    }

    @Override
    public void afterCompletion(@NonNull WebRequest request, Exception ex) {
        // do nothing
    }
}
