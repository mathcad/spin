package org.spin.common.vo;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 微服务接口信息
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/8/8</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class ServiceRequestInfo implements Serializable {

    /**
     * 应用名称
     */
    private final String appName;

    /**
     * 应用版本
     */
    private final String appVersion;

    /**
     * context-path
     */
    private final String contextPath;

    /**
     * 映射信息 BeanType-&gt;{@link List}&lt;{@link RequestMappingInfoWrapper}&gt;
     */
    private final Map<String, List<RequestMappingInfoWrapper>> mappingInfos;

    public ServiceRequestInfo(String appName, String appVersion, String contextPath, List<RequestMappingInfoWrapper> infoWrappers) {
        this.appName = appName;
        this.appVersion = appVersion;
        this.contextPath = contextPath;
        mappingInfos = infoWrappers.stream().collect(Collectors.groupingBy(RequestMappingInfoWrapper::getBeanType));
    }

    public String getAppName() {
        return appName;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public String getContextPath() {
        return contextPath;
    }

    public Map<String, List<RequestMappingInfoWrapper>> getMappingInfos() {
        return mappingInfos;
    }
}
