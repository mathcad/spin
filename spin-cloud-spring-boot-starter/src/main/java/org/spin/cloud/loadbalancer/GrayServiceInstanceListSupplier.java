package org.spin.cloud.loadbalancer;

import org.spin.cloud.web.interceptor.GrayInterceptor;
import org.spin.core.util.CollectionUtils;
import org.spin.core.util.StringUtils;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

/**
 * 支持灰度的服务实例提供者
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2020/12/24</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class GrayServiceInstanceListSupplier implements ServiceInstanceListSupplier {

    private final ServiceInstanceListSupplier delegate;

    public GrayServiceInstanceListSupplier(ServiceInstanceListSupplier delegate) {
        this.delegate = delegate;
    }

    @Override
    public String getServiceId() {
        return delegate.getServiceId();
    }

    @Override
    public Flux<List<ServiceInstance>> get() {

        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (null != requestAttributes) {
            @SuppressWarnings("unchecked")
            Map<String, String> grayInfo = (Map<String, String>) requestAttributes.getAttribute(GrayInterceptor.X_GRAY_INFO,
                RequestAttributes.SCOPE_REQUEST);
            if (null != grayInfo) {
                String version = grayInfo.get(StringUtils.trimToEmpty(StringUtils.toUpperCase(getServiceId())));
                return delegate.get().map(l -> CollectionUtils.select(l, i -> "true".equals(i.getMetadata().get("grayEnable"))
                    && version.equals(i.getMetadata().get("serviceVersion"))));
            }
        }
        return delegate.get();
    }
}
