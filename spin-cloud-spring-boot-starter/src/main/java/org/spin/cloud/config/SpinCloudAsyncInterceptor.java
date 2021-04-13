package org.spin.cloud.config;

import org.spin.cloud.idempotent.IdempotentAspect;
import org.spin.cloud.util.CloudInfrasContext;
import org.spin.cloud.util.Linktrace;
import org.spin.cloud.vo.CurrentUser;
import org.spin.cloud.web.interceptor.CustomizeRouteInterceptor;
import org.spin.cloud.web.interceptor.GrayInterceptor;
import org.spin.cloud.web.interceptor.LinktraceInterceptor;
import org.spin.core.concurrent.AsyncContext;
import org.spin.core.concurrent.AsyncInterceptor;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2021/4/7</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class SpinCloudAsyncInterceptor implements AsyncInterceptor {

    @Override
    public void preAsync(AsyncContext context) {
        if (null != CurrentUser.getCurrent()) {
            context.put(CurrentUser.class.getName(), CurrentUser.getCurrent());
        }

        if (null != CloudInfrasContext.getGrayInfo()) {
            context.put(GrayInterceptor.X_GRAY_INFO, CloudInfrasContext.getGrayInfo());
        }

        if (null != CloudInfrasContext.getCustomizeRoute()) {
            context.put(CustomizeRouteInterceptor.CUSTOMIZE_ROUTE, CloudInfrasContext.getCustomizeRoute());
        }

        if (null != CloudInfrasContext.getIdempotentInfo()) {
            context.put(IdempotentAspect.IDEMPOTENT_ID, CloudInfrasContext.getIdempotentInfo());
        }

        if (null != Linktrace.getCurrentTraceInfo()) {
            context.put(LinktraceInterceptor.X_TRACE_ID, Linktrace.getCurrentTraceInfo());
        }
    }

    @Override
    public void onReady(AsyncContext context) {
        if (context.containsKey(CurrentUser.class.getName())) {
            CurrentUser.setCurrent(context.<CurrentUser>getObj(CurrentUser.class.getName()));
        }

        if (context.containsKey(GrayInterceptor.X_GRAY_INFO)) {
            CloudInfrasContext.setGrayInfo(context.getObj(GrayInterceptor.X_GRAY_INFO));
        }

        if (context.containsKey(CustomizeRouteInterceptor.CUSTOMIZE_ROUTE)) {
            CloudInfrasContext.setCustomizeRoute(context.getObj(CustomizeRouteInterceptor.CUSTOMIZE_ROUTE));
        }

        if (context.containsKey(IdempotentAspect.IDEMPOTENT_ID)) {
            CloudInfrasContext.setIdempotentInfo(context.getString(IdempotentAspect.IDEMPOTENT_ID));
        }

        if (context.containsKey(LinktraceInterceptor.X_TRACE_ID)) {
            Linktrace.setCurrentTraceInfo(context.getObj(LinktraceInterceptor.X_TRACE_ID));
        }
    }

    @Override
    public void onFinish(AsyncContext context) {
        CurrentUser.clearCurrent();
        CloudInfrasContext.removeGrayInfo();
        CloudInfrasContext.removeIdempotentInfo();
        CloudInfrasContext.removeCustomizeRoute();
        Linktrace.removeCurrentTraceInfo();
    }

    @Override
    public void afterAsync(AsyncContext context) {
        context.clear();
    }

    @Override
    public int getOrder() {
        return Integer.MIN_VALUE;
    }
}
