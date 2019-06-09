package org.spin.common.service.remote;

import feign.hystrix.FallbackFactory;
import org.spin.common.feign.AbstractFallback;
import org.spin.common.throwable.BizException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;
import java.util.Set;

/**
 * description user permissions
 *
 * @author wangy QQ 837195190
 * <p>Created by wangy on 2019/3/20.</p>
 */
@FeignClient(name = "bnd-omall-openapi", fallbackFactory = PermissionServiceFallbackFactory.class)
public interface PermissionService {

    @GetMapping(value = "v1/demo/permission")
    Map<Long, Set<String>> getUserPermissions();
}


class PermissionServiceFallback extends AbstractFallback implements PermissionService {

    public PermissionServiceFallback(Throwable cause) {
        super(cause);
    }


    @Override
    public Map<Long, Set<String>> getUserPermissions() {
        handleKnownException();
        throw new BizException("用户服务调用失败");
    }
}

@Component
class PermissionServiceFallbackFactory implements FallbackFactory<PermissionService> {

    @Override
    public PermissionService create(Throwable cause) {
        return new PermissionServiceFallback(cause);
    }
}
