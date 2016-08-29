package org.zibra.server;

public interface ZibraTcpServiceEvent extends ZibraServiceEvent {
    void onAccept(TcpContext tcpContext);
    void onClose(TcpContext tcpContext);
}
