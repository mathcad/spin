package org.zibra.server;

import javax.websocket.EndpointConfig;
import javax.websocket.Session;

public class WebSocketContext extends ServiceContext {
    private final Session session;
    private final EndpointConfig config;

    public WebSocketContext(ZibraClients clients, Session session, EndpointConfig config) {
        super(clients);
        this.session = session;
        this.config = config;
    }

    public Session getSession() {
        return session;
    }

    public EndpointConfig getConfig() {
        return config;
    }
}