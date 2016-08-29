package org.zibra.server;

public interface ZibraHttpServiceEvent extends ZibraServiceEvent {
    void onSendHeader(HttpContext httpContext);
}
