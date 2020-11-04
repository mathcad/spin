package org.spin.cloud.seata.feign;

import feign.Client;
import feign.Request;
import feign.Response;
import io.seata.core.context.RootContext;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SeataFeignClient implements Client {

    private final Client delegate;

    private final BeanFactory beanFactory;

    private static final int MAP_SIZE = 16;

    public SeataFeignClient(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
        this.delegate = new Client.Default(null, null);
    }

    public SeataFeignClient(BeanFactory beanFactory, Client delegate) {
        this.delegate = delegate;
        this.beanFactory = beanFactory;
    }

    @Override
    public Response execute(Request request, Request.Options options) throws IOException {

        Request modifiedRequest = getModifyRequest(request);
        return this.delegate.execute(modifiedRequest, options);
    }

    private Request getModifyRequest(Request request) {

        String xid = RootContext.getXID();

        if (StringUtils.isEmpty(xid)) {
            return request;
        }

        Map<String, Collection<String>> headers = new HashMap<>(MAP_SIZE);
        headers.putAll(request.headers());

        List<String> seataXid = new ArrayList<>();
        seataXid.add(xid);
        headers.put(RootContext.KEY_XID, seataXid);

        return Request.create(request.httpMethod(), request.url(), headers, request.body(),
            request.charset(), null);
    }

}
