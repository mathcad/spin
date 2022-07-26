package org.spin.cloud.web.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.cloud.util.LinkTrace;
import org.spin.cloud.vo.LinkTraceInfo;
import org.spin.core.util.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.support.HttpRequestWrapper;
import org.springframework.lang.NonNull;
import org.springframework.ui.ModelMap;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.WebRequestInterceptor;

import javax.servlet.http.HttpServletRequest;
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
public class LinkTraceInterceptor implements WebRequestInterceptor, ClientHttpRequestInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(LinkTraceInterceptor.class);

    public static final String X_TRACE_ID = "X-Trace-Id";
    public static final String X_PARENT_SPAN_ID = "X-Parent-Span-Id";
    public static final String X_SPAN_ID = "X-Span-Id";

    @Override
    public void preHandle(@NonNull WebRequest request) {
        String requestInfo;
        if (request instanceof ServletRequestAttributes) {
            HttpServletRequest nRequest = ((ServletRequestAttributes) request).getRequest();
            String user = request.getHeader(HttpHeaders.FROM);
            if (user == null) {
                user = "unknown";
            }
            requestInfo = "method=" + nRequest.getMethod() + ";uri=" +
                nRequest.getRequestURI() + ";client=" + nRequest.getRemoteAddr() + ":" +
                nRequest.getRemotePort() + ";from=" + user;
        } else {
            requestInfo = request.getDescription(true);
        }
        String traceId = request.getHeader(X_TRACE_ID);
        if (StringUtils.isEmpty(traceId)) {
            traceId = fastUUID();
        }

        String parentSpanId = request.getHeader(X_SPAN_ID);
        if (StringUtils.isEmpty(parentSpanId)) {
            parentSpanId = "NONE";
        }

        String spanId = fastUUID();

        LinkTraceInfo linktraceInfo = new LinkTraceInfo(traceId, parentSpanId, spanId);
        LinkTrace.setCurrentTraceInfo(linktraceInfo);
        logger.info(linktraceInfo.entryInfo(requestInfo));
    }

    @Override
    public void postHandle(@NonNull WebRequest request, ModelMap model) {
        // do nothing
    }

    @Override
    public void afterCompletion(@NonNull WebRequest request, Exception ex) {
        LinkTraceInfo linktraceInfo = LinkTrace.removeCurrentTraceInfo();
        if (null != linktraceInfo) {
            linktraceInfo.setExitTime(System.currentTimeMillis());
            logger.info(linktraceInfo.exitInfo());
        }
    }

    @Override
    @NonNull
    public ClientHttpResponse intercept(@NonNull HttpRequest request, @NonNull byte[] body, @NonNull ClientHttpRequestExecution execution)
        throws IOException {
        HttpRequestWrapper requestWrapper = new HttpRequestWrapper(request);

        LinkTraceInfo linktraceInfo = LinkTrace.getCurrentTraceInfo();

        if (null != linktraceInfo) {
            requestWrapper.getHeaders().add(LinkTraceInterceptor.X_TRACE_ID, linktraceInfo.getTraceId());
            requestWrapper.getHeaders().add(LinkTraceInterceptor.X_PARENT_SPAN_ID, linktraceInfo.getParentSpanId());
            requestWrapper.getHeaders().add(LinkTraceInterceptor.X_SPAN_ID, linktraceInfo.getSpanId());
        }
        return execution.execute(requestWrapper, body);
    }

    private String fastUUID() {
        UUID uuid = UUID.randomUUID();
        byte[] buf = new byte[32];
        formatUnsignedLong0(uuid.getLeastSignificantBits(), buf, 20, 12);
        formatUnsignedLong0(uuid.getLeastSignificantBits() >>> 48, buf, 16, 4);
        formatUnsignedLong0(uuid.getMostSignificantBits(), buf, 12, 4);
        formatUnsignedLong0(uuid.getMostSignificantBits() >>> 16, buf, 8, 4);
        formatUnsignedLong0(uuid.getMostSignificantBits() >>> 32, buf, 0, 8);
        return new String(buf);
    }

    private void formatUnsignedLong0(long val, byte[] buf, int offset, int len) {
        int charPos = offset + len;
        int mask = 15;
        do {
            buf[--charPos] = (byte) digits[((int) val) & mask];
            val >>>= 4;
        } while (charPos > offset);
    }

    private static final char[] digits = {
        '0', '1', '2', '3', '4', '5',
        '6', '7', '8', '9', 'a', 'b',
        'c', 'd', 'e', 'f', 'g', 'h',
        'i', 'j', 'k', 'l', 'm', 'n',
        'o', 'p', 'q', 'r', 's', 't',
        'u', 'v', 'w', 'x', 'y', 'z'
    };
}
