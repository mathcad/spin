package org.spin.cloud.feign;

import feign.Request;
import feign.RequestTemplate;
import feign.Target;
import org.spin.cloud.util.CloudInfrasContext;

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
            if (null != CloudInfrasContext.getCustomizeRoute()) {
                Map<String, String> routes = CloudInfrasContext.getCustomizeRoute().c2;
                if (null != routes && routes.containsKey(name().toUpperCase())) {
                    targetUrl = routes.get(name());
                }
            }
            input.target(targetUrl);
        }
        return input.request();
    }
}
