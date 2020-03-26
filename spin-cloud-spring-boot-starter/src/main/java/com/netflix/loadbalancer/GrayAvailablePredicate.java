package com.netflix.loadbalancer;

import com.netflix.client.config.IClientConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.cloud.util.BeanHolder;
import org.spin.cloud.web.interceptor.GrayInterceptor;
import org.spin.core.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.Map;

/**
 * 支持灰度的断言
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/9/17</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class GrayAvailablePredicate extends AbstractServerPredicate {
    private static final Logger logger = LoggerFactory.getLogger(GrayAvailablePredicate.class);

    public GrayAvailablePredicate(IRule rule) {
        super(rule);
        this.rule = rule;
    }

    public GrayAvailablePredicate(IRule rule, IClientConfig clientConfig) {
        super(rule, clientConfig);
    }

    @Override
    public boolean apply(PredicateKey input) {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        Server.MetaInfo metaInfo = input.getServer().getMetaInfo();
        if (null != requestAttributes) {
            @SuppressWarnings("unchecked")
            Map<String, String> grayInfo = (Map<String, String>) requestAttributes.getAttribute(GrayInterceptor.X_GRAY_INFO, RequestAttributes.SCOPE_REQUEST);
            if (null != grayInfo) {
                String version = grayInfo.get(metaInfo.getAppName());
                if (StringUtils.isNotEmpty(version)) {
                    boolean res = BeanHolder.getDiscoveryClient().getInstances(metaInfo.getAppName()).stream()
                        .anyMatch(it -> it.getInstanceId().equalsIgnoreCase(metaInfo.getInstanceId())
                            && "true".equals(it.getMetadata().get("grayEnable"))
                            && version.equals(it.getMetadata().get("serviceVersion")));
                    if (!res) {
                        logger.debug("服务[{}]的实例{}不满足当前灰度决策[{}]所要求的版本[{}]", metaInfo.getAppName(), metaInfo.getInstanceId(), grayInfo.get("GRAY-VERSION"), version);
                    }
                    return res;
                }
            }
        }

        boolean res = BeanHolder.getDiscoveryClient().getInstances(metaInfo.getAppName()).stream()
            .anyMatch(it -> it.getInstanceId().equalsIgnoreCase(metaInfo.getInstanceId())
                && (!it.getMetadata().containsKey("GRAY-ENABLE") || "false".equals(it.getMetadata().get("GRAY-ENABLE"))));
        if (!res) {
            logger.debug("服务[{}]的实例{}处于灰度决策中", metaInfo.getAppName(), metaInfo.getInstanceId());
        }
        return res;
    }
}
