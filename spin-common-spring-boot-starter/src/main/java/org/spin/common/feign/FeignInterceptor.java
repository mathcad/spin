package org.spin.common.feign;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.spin.common.vo.CurrentUser;
import org.spin.common.web.interceptor.GrayInterceptor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

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
            template.header(HttpHeaders.FROM, CurrentUser.getCurrent().toString());
        }

        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (null != requestAttributes) {
            String grayInfo = (String) requestAttributes.getAttribute(GrayInterceptor.X_GRAY_INFO_STR, RequestAttributes.SCOPE_REQUEST);
            if (null != grayInfo) {
                template.header(GrayInterceptor.X_GRAY_INFO, grayInfo);
            }
        }
    }
}
