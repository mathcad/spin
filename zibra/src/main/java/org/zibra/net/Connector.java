package org.zibra.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public final class Connector extends Thread {
    protected final AtomicInteger size = new AtomicInteger(0);
    private final Selector selector;
    private final ReactorGroup reactor;
    private final Queue<Connection> queue = new ConcurrentLinkedQueue<>();

    public Connector(int reactorThreads) throws IOException {
        this.selector = Selector.open();
        this.reactor = new ReactorGroup(reactorThreads);
    }

    @Override
    public final void run() {
        reactor.start();
        try {
            while (!isInterrupted()) {
                try {
                    process();
                    dispatch();
                } catch (IOException ignored) {
                }
            }
        } catch (ClosedSelectorException ignored) {
        }
        reactor.close();
    }

    public final void close() {
        try {
            selector.close();
        } catch (IOException ignored) {
        }
    }

    private void process() {
        for (; ; ) {
            final Connection conn = queue.poll();
            if (conn == null) {
                break;
            }
            try {
                conn.connect(selector);
            } catch (ClosedChannelException ignored) {
            }
        }
    }

    private void dispatch() throws IOException {
        int n = selector.select();
        if (n == 0) return;
        Iterator<SelectionKey> it = selector.selectedKeys().iterator();
        while (it.hasNext()) {
            SelectionKey key = it.next();
            it.remove();
            if (key.isConnectable()) {
                connect(key);
            }
        }
    }

    private void connect(SelectionKey key) throws IOException {
        final SocketChannel channel = (SocketChannel) key.channel();
        if (channel.isConnectionPending()) {
            channel.finishConnect();
        }
        reactor.register((Connection) key.attachment());
        key.cancel();
    }

    private void register(Connection conn) {
        queue.offer(conn);
        selector.wakeup();
    }

    public final void create(String uri, ConnectionHandler handler, boolean keepAlive, boolean noDelay) throws IOException {
        try {
            URI u = new URI(uri);
            SocketChannel channel = SocketChannel.open();
            Connection conn = new Connection(channel, handler);
            handler.onConnect(conn);
            channel.configureBlocking(false);
            channel.socket().setReuseAddress(true);
            channel.socket().setKeepAlive(keepAlive);
            channel.socket().setTcpNoDelay(noDelay);
            channel.connect(new InetSocketAddress(u.getHost(), u.getPort()));
            register(conn);
        } catch (URISyntaxException e) {
            throw new IOException(e.getMessage());
        }
    }
}
