package org.spin.common.feign;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.spin.common.vo.CurrentUser;
import org.spin.core.util.StringUtils;
import org.springframework.http.HttpHeaders;

/**
 * Feign拦截器
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/3/25</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class FeignInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate template) {
        if (null != CurrentUser.getCurrent()) {
            template.header(HttpHeaders.FROM, StringUtils.urlEncode(CurrentUser.getCurrent().toString()).replaceFirst("%3A", ":"));
        }
    }
}
