package org.spin.cloud.loadbalancer;

import org.spin.cloud.util.CloudInfrasContext;
import org.spin.core.util.CollectionUtils;
import org.spin.core.util.StringUtils;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
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

        if (null != CloudInfrasContext.getGrayInfo()) {
            Map<String, String> grayInfo = CloudInfrasContext.getGrayInfo().c2;
            if (null != grayInfo) {
                String version = grayInfo.get(StringUtils.trimToEmpty(StringUtils.toUpperCase(getServiceId())));
                return delegate.get().map(l -> CollectionUtils.select(l, i -> "true".equals(i.getMetadata().get("grayEnable"))
                    && version.equals(i.getMetadata().get("serviceVersion"))));
            }
        }
        return delegate.get();
    }
}
