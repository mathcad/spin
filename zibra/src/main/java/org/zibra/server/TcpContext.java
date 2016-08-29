package org.zibra.server;

import java.net.Socket;
import java.nio.channels.SocketChannel;

public class TcpContext extends ServiceContext {
    private final SocketChannel socketChannel;

    public TcpContext(ZibraClients clients,
                      SocketChannel socketChannel) {
        super(clients);
        this.socketChannel = socketChannel;
    }

    public SocketChannel getSocketChannel() {
        return socketChannel;
    }

    public Socket getSocket() {
        return socketChannel.socket();
    }
}