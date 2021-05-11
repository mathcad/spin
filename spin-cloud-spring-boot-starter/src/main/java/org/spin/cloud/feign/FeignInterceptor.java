package org.spin.cloud.feign;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.spin.cloud.idempotent.IdempotentAspect;
import org.spin.cloud.util.CloudInfrasContext;
import org.spin.cloud.util.Env;
import org.spin.cloud.util.LinkTrace;
import org.spin.cloud.vo.CurrentUser;
import org.spin.cloud.vo.LinkTraceInfo;
import org.spin.cloud.web.interceptor.CustomizeRouteInterceptor;
import org.spin.cloud.web.interceptor.GrayInterceptor;
import org.spin.cloud.web.interceptor.LinkTraceInterceptor;
import org.spin.core.util.StringUtils;
import org.springframework.http.HttpHeaders;

import java.util.UUID;

/**
 * Feign拦截器
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/3/25</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class FeignInterceptor implements RequestInterceptor {
    public static final String X_APP_NAME = "X-App-Name";
    public static final String X_APP_PROFILE = "X-App-Profile";

    @Override
    public void apply(RequestTemplate template) {
        if (StringUtils.isNotEmpty(Env.getAppName())) {
            template.header(X_APP_NAME, Env.getAppName());
        }
        if (StringUtils.isNotEmpty(Env.getActiveProfiles())) {
            template.header(X_APP_PROFILE, Env.getActiveProfiles());
        }

        if (null != CurrentUser.getCurrent()) {
            template.header(HttpHeaders.FROM, CurrentUser.getCurrent().toString());
        }

        if (null != CloudInfrasContext.getGrayInfo()) {
            template.header(GrayInterceptor.X_GRAY_INFO, CloudInfrasContext.getGrayInfo().c1);
        }

        if (null != CloudInfrasContext.getCustomizeRoute()) {
            template.header(CustomizeRouteInterceptor.CUSTOMIZE_ROUTE, CloudInfrasContext.getCustomizeRoute().c1);
        }

        StringBuilder idempotent = new StringBuilder();
        idempotent.append(StringUtils.toStringEmpty(CloudInfrasContext.getIdempotentInfo()));
        if (idempotent.length() > 0) {
            idempotent.append(";");
        }
        template.header(IdempotentAspect.IDEMPOTENT_ID, idempotent.append(UUID.randomUUID().toString()).toString());

        LinkTraceInfo linktraceInfo = LinkTrace.getCurrentTraceInfo();
        if (null != linktraceInfo) {
            template.header(LinkTraceInterceptor.X_TRACE_ID, linktraceInfo.getTraceId());
            template.header(LinkTraceInterceptor.X_PARENT_SPAN_ID, linktraceInfo.getParentSpanId());
            template.header(LinkTraceInterceptor.X_SPAN_ID, linktraceInfo.getSpanId());
        }
    }
}
