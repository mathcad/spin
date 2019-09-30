package org.spin.common.web.config;

import org.spin.common.vo.ServiceRequestInfo;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/8/9</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class RequestMappingInfoHolder {
    private static ServiceRequestInfo requestInfo;

    public static ServiceRequestInfo getRequestInfo() {
        return requestInfo;
    }

    static void setRequestInfo(ServiceRequestInfo requestInfo) {
        RequestMappingInfoHolder.requestInfo = requestInfo;
    }
}
