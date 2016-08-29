package org.zibra.spring;

import org.zibra.common.FilterHandler;
import org.zibra.common.HproseFilter;
import org.zibra.common.InvokeHandler;
import org.zibra.io.ZibraMode;
import org.zibra.server.ZibraHttpService;
import org.zibra.server.ZibraServiceEvent;
import org.zibra.server.HttpContext;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.remoting.support.RemoteExporter;
import org.springframework.util.StringUtils;
import org.springframework.web.HttpRequestHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ZibraHttpServiceExporter extends RemoteExporter implements InitializingBean, HttpRequestHandler {
    private static final String ACCESS_TOKEN_HEADER_NAME = "ACCESS_TOKEN";
    private ZibraHttpService httpService;
    private boolean crossDomain = true;
    private boolean get = true;
    private boolean p3p = true;
    private boolean debug = true;
    private ZibraServiceEvent event = null;
    private ZibraMode mode = ZibraMode.MemberMode;
    private HproseFilter filter = null;
    private InvokeHandler invokeHandler = null;
    private FilterHandler beforeFilterHandler = null;
    private FilterHandler afterFilterHandler = null;
    private String accessToken;

    @Override
    public void afterPropertiesSet() {
        checkService();
        checkServiceInterface();
        Object service = getService();
        Class cls = getServiceInterface();
        httpService = new ZibraHttpService();
        httpService.add(service, cls);
        httpService.setCrossDomainEnabled(crossDomain);
        httpService.setGetEnabled(get);
        httpService.setP3pEnabled(p3p);
        httpService.setDebugEnabled(debug);
        httpService.setEvent(event);
        httpService.setMode(mode);
        httpService.setFilter(filter);
        httpService.use(invokeHandler);
        httpService.beforeFilter.use(beforeFilterHandler);
        httpService.afterFilter.use(afterFilterHandler);
    }

    public void setCrossDomainEnabled(boolean value) {
        crossDomain = value;
    }

    public void setGetEnabled(boolean value) {
        get = value;
    }

    public void setP3pEnabled(boolean value) {
        p3p = value;
    }

    public void setDebugEnabled(boolean value) {
        debug = value;
    }

    public void setEvent(ZibraServiceEvent value) {
        event = value;
    }

    public void setMode(ZibraMode value) {
        mode = value;
    }

    public void setFilter(HproseFilter value) {
        filter = value;
    }

    public void setInvokeHandler(InvokeHandler value) {
        invokeHandler = value;
    }

    public void setBeforeFilterHandler(FilterHandler value) {
        beforeFilterHandler = value;
    }

    public void setAfterFilterHandler(FilterHandler value) {
        afterFilterHandler = value;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    @Override
    public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String reqToken = request.getHeader(ACCESS_TOKEN_HEADER_NAME);
        if (!StringUtils.isEmpty(this.accessToken) && !this.accessToken.equals(reqToken)) {
            throw new RuntimeException("Invalid Access Token, service vender reject your request");
        }
        httpService.handle(new HttpContext(httpService, request, response, null, null));
    }
}