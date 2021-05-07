package org.spin.cloud.web.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.cloud.util.Linktrace;
import org.spin.cloud.vo.LinktraceInfo;
import org.spin.core.util.StringUtils;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.support.HttpRequestWrapper;
import org.springframework.lang.NonNull;
import org.springframework.ui.ModelMap;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.WebRequestInterceptor;

import java.io.IOException;
import java.util.UUID;

/**
 * 链路跟踪拦截器
 * <p>解析灰度策略并绑定到当前请求上下文</p>
 * <p>Created by xuweinan on 2019/9/17</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class LinktraceInterceptor implements WebRequestInterceptor, ClientHttpRequestInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(LinktraceInterceptor.class);

    public static final String X_TRACE_ID = "X-Trace-Id";
    public static final String X_PARENTSPAN_ID = "X-Parent-Span-Id";
    public static final String X_SPAN_ID = "X-Span-Id";

    @Override
    public void preHandle(@NonNull WebRequest request) {

        String traceId = request.getHeader(X_TRACE_ID);
        if (StringUtils.isEmpty(traceId)) {
            traceId = UUID.randomUUID().toString();
        }

        String parentSpanId = request.getHeader(X_SPAN_ID);
        if (StringUtils.isEmpty(parentSpanId)) {
            parentSpanId = "NONE";
        }

        String spanId = UUID.randomUUID().toString();

        logger.info("Linktrace Info Entry - TraceId: {} ParentSpanId: {} SpanId: {}\n  Request Info: {}", traceId, parentSpanId, spanId,
            request.getDescription(false));
        Linktrace.setCurrentTraceInfo(new LinktraceInfo(traceId, parentSpanId, spanId));
    }

    @Override
    public void postHandle(@NonNull WebRequest request, ModelMap model) {
        // do nothing
    }

    @Override
    public void afterCompletion(@NonNull WebRequest request, Exception ex) {
        LinktraceInfo linktraceInfo = Linktrace.removeCurrentTraceInfo();
        if (null != linktraceInfo) {
            logger.info("Linktrace Info Exit - TraceId: {} ParentSpanId: {} SpanId: {}",
                linktraceInfo.getTraceId(),
                linktraceInfo.getParentSpanId(),
                linktraceInfo.getSpanId()
            );
        }
    }

    @Override
    @NonNull
    public ClientHttpResponse intercept(@NonNull HttpRequest request, @NonNull byte[] body, @NonNull ClientHttpRequestExecution execution)
        throws IOException {
        HttpRequestWrapper requestWrapper = new HttpRequestWrapper(request);

        LinktraceInfo linktraceInfo = Linktrace.getCurrentTraceInfo();

        if (null != linktraceInfo) {
            requestWrapper.getHeaders().add(LinktraceInterceptor.X_TRACE_ID, linktraceInfo.getTraceId());
            requestWrapper.getHeaders().add(LinktraceInterceptor.X_PARENTSPAN_ID, linktraceInfo.getParentSpanId());
            requestWrapper.getHeaders().add(LinktraceInterceptor.X_SPAN_ID, linktraceInfo.getSpanId());
        }
        return execution.execute(requestWrapper, body);
    }
}
