package org.spin.cloud.seata.feign;

import feign.Client;
import feign.Request;
import feign.Response;
import org.springframework.cloud.loadbalancer.blocking.client.BlockingLoadBalancerClient;
import org.springframework.cloud.openfeign.loadbalancer.FeignBlockingLoadBalancerClient;

import java.io.IOException;

public class SeataFeignBlockingLoadBalancerClient
    extends FeignBlockingLoadBalancerClient {

    public SeataFeignBlockingLoadBalancerClient(Client delegate,
                                                BlockingLoadBalancerClient loadBalancerClient,
                                                SeataFeignObjectWrapper seataFeignObjectWrapper) {
        super((Client) seataFeignObjectWrapper.wrap(delegate), loadBalancerClient);
    }

    @Override
    public Response execute(Request request, Request.Options options) throws IOException {
        return super.execute(request, options);
    }

}
