package org.spin.cloud.web.handler;

import org.spin.cloud.vo.ServiceRequestInfo;
import org.spin.cloud.web.config.RequestMappingInfoHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * RequestMapping信息Controller
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/8/9</p>
 *
 * @author xuweinan
 * @version 1.0
 */
@RestController
@RequestMapping("/v1/mapping")
public class RequestMappingInfoController {

    public ServiceRequestInfo requestMappingInfos() {
        return RequestMappingInfoHolder.getRequestInfo();
    }
}
