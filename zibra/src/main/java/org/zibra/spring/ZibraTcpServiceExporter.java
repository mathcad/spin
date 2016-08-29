package org.zibra.spring;

import org.zibra.common.FilterHandler;
import org.zibra.common.HproseFilter;
import org.zibra.common.InvokeHandler;
import org.zibra.io.ZibraMode;
import org.zibra.server.ZibraServiceEvent;
import org.zibra.server.ZibraTcpServer;
import java.io.IOException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.remoting.support.RemoteExporter;

public class ZibraTcpServiceExporter extends RemoteExporter implements InitializingBean {
    private ZibraTcpServer tcpServer;
    private String host;
    private int port = 0;
    private boolean debug = true;
    private ZibraServiceEvent event = null;
    private ZibraMode mode = ZibraMode.MemberMode;
    private HproseFilter filter = null;
    private InvokeHandler invokeHandler = null;
    private FilterHandler beforeFilterHandler = null;
    private FilterHandler afterFilterHandler = null;

    @Override
    public void afterPropertiesSet() throws Exception {
        checkService();
        checkServiceInterface();
        Object service = getService();
        Class cls = getServiceInterface();
        tcpServer = new ZibraTcpServer(host, port);
        tcpServer.add(service, cls);
        tcpServer.setDebugEnabled(debug);
        tcpServer.setEvent(event);
        tcpServer.setMode(mode);
        tcpServer.setFilter(filter);
        tcpServer.use(invokeHandler);
        tcpServer.beforeFilter.use(beforeFilterHandler);
        tcpServer.afterFilter.use(afterFilterHandler);
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

    public void setHost(String value) {
        host = value;
    }

    public void setPort(int value) {
        port = value;
    }

    public void start() throws IOException {
        tcpServer.start();
    }

    public void stop() {
        tcpServer.stop();
    }
}
