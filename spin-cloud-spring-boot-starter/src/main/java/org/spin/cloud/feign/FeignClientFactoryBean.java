package org.spin.cloud.feign;

import feign.Client;
import feign.Feign;
import feign.Target;
import org.spin.core.util.BeanUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.cloud.openfeign.FeignContext;
import org.springframework.cloud.openfeign.Targeter;
import org.springframework.cloud.openfeign.loadbalancer.FeignBlockingLoadBalancerClient;
import org.springframework.util.StringUtils;

/**
 * @author xuweinan
 */
public class FeignClientFactoryBean extends org.springframework.cloud.openfeign.FeignClientFactoryBean {
    protected Feign.Builder feign(FeignContext context) {
        return super.feign(context).errorDecoder(new RestfulErrorDecoder());
    }

    @Override
    public Object getObject() {
        return getTarget();
    }

    /**
     * @param <T> the target type of the Feign client
     * @return a {@link Feign} client created with the specified data and the context
     * information
     */
    <T> T getTarget() {
        BeanFactory beanFactory = BeanUtils.getFieldValue(this, "beanFactory");
        FeignContext context = beanFactory != null ? beanFactory.getBean(FeignContext.class)
            : getApplicationContext().getBean(FeignContext.class);
        Feign.Builder builder = feign(context);

        if (!StringUtils.hasText(getUrl())) {
            if (!getName().startsWith("http")) {
                setUrl("http://" + getName());
            } else {
                setUrl(getName());
            }
            setUrl(getUrl() + cleanPath());
            return (T) loadBalance(builder, context, new Target.HardCodedTarget<>(getType(), getName(), getUrl()));
        }
        if (StringUtils.hasText(getUrl()) && !getUrl().startsWith("http")) {
            setUrl("http://" + getUrl());
        }
        String url = getUrl() + cleanPath();
        Client client = getOptional(context, Client.class);
        if (client != null) {
            if (client instanceof FeignBlockingLoadBalancerClient) {
                // not load balancing because we have a url,
                // but Spring Cloud LoadBalancer is on the classpath, so unwrap
                client = ((FeignBlockingLoadBalancerClient) client).getDelegate();
            }
            builder.client(client);
        }
        Targeter targeter = get(context, Targeter.class);
        return (T) targeter.target(this, builder, context, new HardCodedTarget<>(getType(), getName(), url));
    }

    private String cleanPath() {
        String path = getPath().trim();
        if (StringUtils.hasLength(path)) {
            if (!path.startsWith("/")) {
                path = "/" + path;
            }
            if (path.endsWith("/")) {
                path = path.substring(0, path.length() - 1);
            }
        }
        return path;
    }
}
