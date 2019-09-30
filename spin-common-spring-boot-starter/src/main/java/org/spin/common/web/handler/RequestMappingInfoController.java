package org.spin.common.web.handler;

import org.spin.common.vo.ServiceRequestInfo;
import org.spin.common.web.AuthLevel;
import org.spin.common.web.ScopeType;
import org.spin.common.web.annotation.GetApi;
import org.spin.common.web.config.RequestMappingInfoHolder;
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

    @GetApi(value = "infos", auth = AuthLevel.NONE, scope = ScopeType.INTERNAL)
    public ServiceRequestInfo requestMappingInfos() {
        return RequestMappingInfoHolder.getRequestInfo();
    }
}
