package org.spin.cloud.seata.feign;

import feign.Client;
import feign.Request;
import feign.Response;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;
import org.springframework.cloud.openfeign.ribbon.CachingSpringLoadBalancerFactory;
import org.springframework.cloud.openfeign.ribbon.LoadBalancerFeignClient;

import java.io.IOException;

public class SeataLoadBalancerFeignClient extends LoadBalancerFeignClient {

    SeataLoadBalancerFeignClient(Client delegate,
                                 CachingSpringLoadBalancerFactory lbClientFactory,
                                 SpringClientFactory clientFactory,
                                 SeataFeignObjectWrapper seataFeignObjectWrapper) {
        super((Client) seataFeignObjectWrapper.wrap(delegate), lbClientFactory,
            clientFactory);
    }

    @Override
    public Response execute(Request request, Request.Options options) throws IOException {
        return super.execute(request, options);
    }

}
