package org.spin.web.filter;

import org.spin.core.util.StreamUtils;
import org.spin.core.util.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * 跨域过滤器
 * Created by xuweinan on 2016/10/11.
 *
 * @author xuweinan
 */
public class CorsFilter implements Filter {
    private static final String ALL = "*";
    private static final String MAX_AGE = "18000L";

    private final Set<String> excludeService = new HashSet<>();

    @Override
    public void init(FilterConfig filterConfig) {
        // do nothing
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpServletResponse httpResponse = (HttpServletResponse) response;

            if (HttpMethod.OPTIONS.matches(httpRequest.getMethod())) {
                addCorsHeaders(httpRequest, httpResponse);
                httpResponse.setStatus(HttpStatus.OK.value());
                return;
            }

            String path = StringUtils.trimToEmpty(httpRequest.getRequestURI());
            String appId = "";
            if (StringUtils.isNotEmpty(path) && path.length() > 1 && path.lastIndexOf('/') != 0) {
                try {
                    appId = path.toLowerCase().substring(1, path.indexOf("/", 1));
                } catch (Exception ignore) {
                    // do nothing
                }
            }

            if (!excludeService.contains(appId)) {
                addCorsHeaders(httpRequest, httpResponse);
            }
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        excludeService.clear();
    }

    @Value("${gateway.cors.exclude}")
    public void refreshExcludeService(String excludeService) {
        synchronized (this.excludeService) {
            this.excludeService.clear();
            this.excludeService.addAll(Arrays.asList(StringUtils.trimToEmpty(excludeService).toLowerCase().split(",")));
        }
    }

    private void addCorsHeaders(HttpServletRequest request, HttpServletResponse response) {

        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, request.getHeader(HttpHeaders.ORIGIN));
        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
        response.setHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, ALL);
        response.setHeader(HttpHeaders.ACCESS_CONTROL_MAX_AGE, MAX_AGE);
        List<String> headers = new LinkedList<>();
        headers.add(HttpHeaders.CONTENT_TYPE);
        headers.add(HttpHeaders.AUTHORIZATION);
        StreamUtils.enumerationAsStream(request.getHeaders(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS)).forEach(h -> {
            switch (h.toLowerCase().trim()) {
                case "content-type":
                case "authorization":
                case "accept":
                case "accept-language":
                case "content-language":
                    break;
                default:
                    headers.add(h);
            }
        });

        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, StringUtils.join(headers, ","));
        String requestMethod = request.getHeader(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD);
        if (requestMethod != null) {
            response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, requestMethod);
        }
    }
}
