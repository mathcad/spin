package org.spin.cloud.loadbalancer;

import feign.Client;
import feign.Request;
import feign.Response;
import feign.Target;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.util.BeanUtils;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.*;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.cloud.openfeign.loadbalancer.RetryableFeignBlockingLoadBalancerClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.retry.RetryListener;
import org.springframework.retry.backoff.BackOffPolicy;
import org.springframework.retry.backoff.NoBackOffPolicy;
import org.springframework.retry.policy.NeverRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.Assert;

import java.io.IOException;
import java.net.URI;
import java.util.*;


/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2020/12/28</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class SpinRetryableFeignBlockingLoadBalancerClient extends RetryableFeignBlockingLoadBalancerClient {
    private static final Logger logger = LoggerFactory.getLogger(SpinRetryableFeignBlockingLoadBalancerClient.class);

    private final Client delegate;
    private final LoadBalancerClient loadBalancerClient;
    private final LoadBalancedRetryFactory loadBalancedRetryFactory;
    private final LoadBalancerClientFactory loadBalancerClientFactory;

    public SpinRetryableFeignBlockingLoadBalancerClient(RetryableFeignBlockingLoadBalancerClient delegate) {
        this(delegate.getDelegate(), BeanUtils.getFieldValue(delegate, "loadBalancerClient"),
            BeanUtils.getFieldValue(delegate, "loadBalancedRetryFactory"),
            BeanUtils.getFieldValue(delegate, "loadBalancerClientFactory")
        );
    }

    public SpinRetryableFeignBlockingLoadBalancerClient(Client delegate, LoadBalancerClient loadBalancerClient,
                                                        LoadBalancedRetryFactory loadBalancedRetryFactory,
                                                        LoadBalancerClientFactory loadBalancerClientFactory) {
        super(delegate, loadBalancerClient, loadBalancedRetryFactory, loadBalancerClientFactory);

        this.delegate = delegate;
        this.loadBalancerClient = loadBalancerClient;
        this.loadBalancedRetryFactory = loadBalancedRetryFactory;
        this.loadBalancerClientFactory = loadBalancerClientFactory;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Response execute(Request request, Request.Options options) throws IOException {
        final URI originalUri = URI.create(request.url());
        String serviceId = originalUri.getHost();
        Assert.state(serviceId != null, "Request URI does not contain a valid hostname: " + originalUri);
        final LoadBalancedRetryPolicy retryPolicy = loadBalancedRetryFactory.createRetryPolicy(serviceId, loadBalancerClient);
        RetryTemplate retryTemplate = buildRetryTemplate(serviceId, request, retryPolicy);
        return retryTemplate.execute(context -> {
            Request feignRequest = null;
            Target<?> target = request.requestTemplate().feignTarget();
            ServiceInstance retrievedServiceInstance = null;
            Set<LoadBalancerLifecycle> supportedLifecycleProcessors = LoadBalancerLifecycleValidator.getSupportedLifecycleProcessors(
                loadBalancerClientFactory.getInstances(serviceId, LoadBalancerLifecycle.class),
                RetryableRequestContext.class, ResponseData.class, ServiceInstance.class);
            String hint = getHint(serviceId);
            DefaultRequest<RetryableRequestContext> lbRequest = new DefaultRequest<>(
                new RetryableRequestContext(null, buildRequestData(request), hint));
            if (target.name().equals(serviceId)) {
                // On retries the policy will choose the server and set it in the context
                // and extract the server and update the request being made
                if (context instanceof LoadBalancedRetryContext) {
                    LoadBalancedRetryContext lbContext = (LoadBalancedRetryContext) context;
                    ServiceInstance serviceInstance = lbContext.getServiceInstance();
                    if (serviceInstance == null) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Service instance retrieved from LoadBalancedRetryContext: was null. "
                                + "Reattempting service instance selection");
                        }
                        ServiceInstance previousServiceInstance = lbContext.getPreviousServiceInstance();
                        lbRequest.getContext().setPreviousServiceInstance(previousServiceInstance);
                        supportedLifecycleProcessors.forEach(lifecycle -> lifecycle.onStart(lbRequest));
                        retrievedServiceInstance = loadBalancerClient.choose(serviceId, lbRequest);
                        if (logger.isDebugEnabled()) {
                            logger.debug(String.format("Selected service instance: %s", retrievedServiceInstance));
                        }
                        lbContext.setServiceInstance(retrievedServiceInstance);
                    }

                    if (retrievedServiceInstance == null) {
                        if (logger.isWarnEnabled()) {
                            logger.warn("Service instance was not resolved, executing the original request");
                        }
                        org.springframework.cloud.client.loadbalancer.Response<ServiceInstance> lbResponse = new DefaultResponse(
                            retrievedServiceInstance);
                        supportedLifecycleProcessors.forEach(lifecycle -> lifecycle.onComplete(new CompletionContext<ResponseData, ServiceInstance, RetryableRequestContext>(
                            CompletionContext.Status.DISCARD, lbRequest, lbResponse)));
                        feignRequest = request;
                    } else {
                        if (logger.isDebugEnabled()) {
                            logger.debug(String.format("Using service instance from LoadBalancedRetryContext: %s",
                                retrievedServiceInstance));
                        }
                        String reconstructedUrl = loadBalancerClient.reconstructURI(retrievedServiceInstance, originalUri)
                            .toString();
                        feignRequest = buildRequest(request, reconstructedUrl);
                    }
                }
            } else {
                feignRequest = request;
            }


            org.springframework.cloud.client.loadbalancer.Response<ServiceInstance> lbResponse = new DefaultResponse(
                retrievedServiceInstance);
            Response response = executeWithLoadBalancerLifecycleProcessing(delegate, options,
                feignRequest, lbRequest, lbResponse, supportedLifecycleProcessors,
                retrievedServiceInstance != null);
            int responseStatus = response.status();
            if (retryPolicy != null && retryPolicy.retryableStatusCode(responseStatus)) {
                if (logger.isDebugEnabled()) {
                    logger.debug(String.format("Retrying on status code: %d", responseStatus));
                }
                response.close();
                throw new RetryableStatusCodeException(serviceId, responseStatus, response, URI.create(request.url()));
            }
            return response;
        }, new LoadBalancedRecoveryCallback<Response, Response>() {
            @Override
            protected Response createResponse(Response response, URI uri) {
                return response;
            }
        });
    }

    private RetryTemplate buildRetryTemplate(String serviceId, Request request, LoadBalancedRetryPolicy retryPolicy) {
        RetryTemplate retryTemplate = new RetryTemplate();
        BackOffPolicy backOffPolicy = this.loadBalancedRetryFactory.createBackOffPolicy(serviceId);
        retryTemplate.setBackOffPolicy(backOffPolicy == null ? new NoBackOffPolicy() : backOffPolicy);
        RetryListener[] retryListeners = this.loadBalancedRetryFactory.createRetryListeners(serviceId);
        if (retryListeners != null && retryListeners.length != 0) {
            retryTemplate.setListeners(retryListeners);
        }

        retryTemplate.setRetryPolicy(retryPolicy == null ? new NeverRetryPolicy()
            : new InterceptorRetryPolicy(toHttpRequest(request), retryPolicy, loadBalancerClient, serviceId));
        return retryTemplate;
    }

    private HttpRequest toHttpRequest(Request request) {
        return new HttpRequest() {
            @Override
            public HttpMethod getMethod() {
                return HttpMethod.resolve(request.httpMethod().name());
            }

            @Override
            public String getMethodValue() {
                return getMethod().name();
            }

            @Override
            public URI getURI() {
                return URI.create(request.url());
            }

            @Override
            public HttpHeaders getHeaders() {
                Map<String, List<String>> headers = new HashMap<>();
                Map<String, Collection<String>> feignHeaders = request.headers();
                for (String key : feignHeaders.keySet()) {
                    headers.put(key, new ArrayList<>(feignHeaders.get(key)));
                }
                HttpHeaders httpHeaders = new HttpHeaders();
                httpHeaders.putAll(headers);
                return httpHeaders;
            }
        };
    }

    private String getHint(String serviceId) {
        LoadBalancerProperties properties = loadBalancerClientFactory.getProperties(serviceId);
        String defaultHint = properties.getHint().getOrDefault("default", "default");
        String hintPropertyValue = properties.getHint().get(serviceId);
        return hintPropertyValue != null ? hintPropertyValue : defaultHint;
    }

    static RequestData buildRequestData(Request request) {
        HttpHeaders requestHeaders = new HttpHeaders();
        request.headers().forEach((key, value) -> requestHeaders.put(key, new ArrayList<>(value)));
        return new RequestData(HttpMethod.resolve(request.httpMethod().name()), URI.create(request.url()),
            requestHeaders, null, new HashMap<>());
    }

    static ResponseData buildResponseData(Response response) {
        HttpHeaders responseHeaders = new HttpHeaders();
        response.headers().forEach((key, value) -> responseHeaders.put(key, new ArrayList<>(value)));
        return new ResponseData(HttpStatus.resolve(response.status()), responseHeaders, null,
            buildRequestData(response.request()));
    }

    static Response executeWithLoadBalancerLifecycleProcessing(Client feignClient, Request.Options options,
                                                               Request feignRequest, org.springframework.cloud.client.loadbalancer.Request lbRequest,
                                                               org.springframework.cloud.client.loadbalancer.Response<ServiceInstance> lbResponse,
                                                               Set<LoadBalancerLifecycle> supportedLifecycleProcessors, boolean loadBalanced) throws IOException {
        supportedLifecycleProcessors.forEach(lifecycle -> lifecycle.onStartRequest(lbRequest, lbResponse));
        try {
            Response response = feignClient.execute(feignRequest, options);
            if (loadBalanced) {
                supportedLifecycleProcessors.forEach(
                    lifecycle -> lifecycle.onComplete(new CompletionContext<>(CompletionContext.Status.SUCCESS,
                        lbRequest, lbResponse, buildResponseData(response))));
            }
            return response;
        } catch (Exception exception) {
            if (loadBalanced) {
                supportedLifecycleProcessors.forEach(lifecycle -> lifecycle.onComplete(
                    new CompletionContext<>(CompletionContext.Status.FAILED, exception, lbRequest, lbResponse)));
            }
            throw exception;
        }
    }
}
