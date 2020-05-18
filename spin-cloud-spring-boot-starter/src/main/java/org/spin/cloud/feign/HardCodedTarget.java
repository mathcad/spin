package org.spin.cloud.feign;

import feign.Request;
import feign.RequestTemplate;
import feign.Target;
import org.spin.cloud.web.interceptor.CustomizeRouteInterceptor;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.Map;

public class HardCodedTarget<T> extends Target.HardCodedTarget<T> {

    public HardCodedTarget(Class<T> type, String url) {
        this(type, url, url);
    }

    public HardCodedTarget(Class<T> type, String name, String url) {
        super(type, name, url);
    }

    @Override
    public Request apply(RequestTemplate input) {
        if (input.url().indexOf("http") != 0) {

            String targetUrl = url();
            RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
            if (null != requestAttributes) {
                @SuppressWarnings("unchecked")
                Map<String, String> routes = (Map<String, String>) requestAttributes.getAttribute(CustomizeRouteInterceptor.CUSTOMIZE_ROUTE,
                    RequestAttributes.SCOPE_REQUEST);
                if (null != routes && routes.containsKey(name().toUpperCase())) {
                    targetUrl = routes.get(name());
                }
            }
            input.target(targetUrl);
        }
        return input.request();
    }
}
