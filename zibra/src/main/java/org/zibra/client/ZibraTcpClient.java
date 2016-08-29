package org.zibra.client;

import org.zibra.common.ZibraException;
import org.zibra.common.InvokeSettings;
import org.zibra.io.ZibraMode;
import org.zibra.net.Connection;
import org.zibra.net.ConnectionHandler;
import org.zibra.net.Connector;
import org.zibra.net.TimeoutType;
import org.zibra.util.concurrent.Promise;
import org.zibra.util.concurrent.Threads;
import org.zibra.util.concurrent.Timer;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;


final class Request {
    public final ByteBuffer buffer;
    public final Promise<ByteBuffer> result = new Promise<>();
    public final int timeout;

    public Request(ByteBuffer buffer, int timeout) {
        this.buffer = buffer;
        this.timeout = timeout;
    }
}

final class Response {
    public final Promise<ByteBuffer> result;
    public final long createTime;
    public final int timeout;

    public Response(Promise<ByteBuffer> result, int timeout) {
        this.result = result;
        this.createTime = System.currentTimeMillis();
        this.timeout = timeout;
    }
}

abstract class SocketTransporter extends Thread implements ConnectionHandler {
    protected final static class ConnectorHolder {
        final static Connector connector;

        static {
            Connector temp = null;
            try {
                temp = new Connector(ZibraTcpClient.getReactorThreads());
            } catch (IOException ignored) {
            } finally {
                connector = temp;
                if (connector != null)
                    connector.start();
            }
            Threads.registerShutdownHandler(new Runnable() {
                public void run() {
                    if (connector != null) {
                        connector.close();
                    }
                }
            });
        }
    }

    protected final ZibraTcpClient client;
    protected final BlockingQueue<Connection> idleConnections = new LinkedBlockingQueue<>();
    protected final BlockingQueue<Request> requests = new LinkedBlockingQueue<>();
    protected final AtomicInteger size = new AtomicInteger(0);

    public SocketTransporter(ZibraTcpClient client) {
        super();
        this.client = client;
    }

    public final int getReadTimeout() {
        return client.getReadTimeout();
    }

    public final int getWriteTimeout() {
        return client.getWriteTimeout();
    }

    public final int getConnectTimeout() {
        return client.getConnectTimeout();
    }

    @Override
    public void run() {
        try {
            while (!isInterrupted()) {
                Request request;
                if (requests.isEmpty()) {
                    request = requests.take();
                    requests.offer(request);
                }
                if (idleConnections.isEmpty()) {
                    if (geRealPoolSize() < client.getMaxPoolSize()) {
                        try {
                            ConnectorHolder.connector.create(client.uri, this, client.isKeepAlive(), client.isNoDelay());
                        } catch (IOException ex) {
                            while ((request = requests.poll()) != null) {
                                request.result.reject(ex);
                            }
                        }
                    }
                }
                Connection conn = idleConnections.poll(client.getConnectTimeout(), TimeUnit.MILLISECONDS);
                if (conn != null) {
                    request = requests.poll();
                    if (request == null) {
                        request = requests.take();
                    }
                    send(conn, request);
                }
            }
        } catch (InterruptedException ignored) {
        }
    }

    protected abstract int geRealPoolSize();

    protected abstract void send(Connection conn, Request request);

    public final synchronized Promise<ByteBuffer> send(ByteBuffer buffer, int timeout) {
        Request request = new Request(buffer, timeout);
        requests.offer(request);
        return request.result;
    }

    protected void close(Map<Connection, Object> responses) {
        interrupt();
        while (!responses.isEmpty()) {
            responses.keySet().forEach(Connection::close);
        }
        while (!requests.isEmpty()) {
            requests.poll().result.reject(new ClosedChannelException());
        }
    }

    public final void onClose(Connection conn) {
        idleConnections.remove(conn);
        onError(conn, new ClosedChannelException());
    }

    public abstract void close();

}

final class FullDuplexSocketTransporter extends SocketTransporter {
    private final static AtomicInteger nextId = new AtomicInteger(0);
    private final Map<Connection, Map<Integer, Response>> responses = new ConcurrentHashMap<>();
    private final Timer timer = new Timer(() -> {
        long currentTime = System.currentTimeMillis();
        for (Map.Entry<Connection, Map<Integer, Response>> entry : responses.entrySet()) {
            Connection conn = entry.getKey();
            Map<Integer, Response> res = entry.getValue();
            Iterator<Map.Entry<Integer, Response>> it = res.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Integer, Response> e = it.next();
                Response response = e.getValue();
                if ((currentTime - response.createTime) >= response.timeout) {
                    it.remove();
                    response.result.reject(new TimeoutException("timeout"));
                }
            }
            if (res.isEmpty() && conn.isConnected()) {
                recycle(conn);
            }
        }
    });

    public FullDuplexSocketTransporter(ZibraTcpClient client) {
        super(client);
        init();
    }

    private void init() {
        int timeout = Math.min(client.getTimeout(), client.getConnectTimeout());
        timeout = Math.min(timeout, client.getReadTimeout());
        timeout = Math.min(timeout, client.getWriteTimeout());
        timeout = Math.max(timeout, 1000);
        timer.setInterval((timeout + 1) >> 1);
        start();
    }

    private void recycle(Connection conn) {
        conn.setTimeout(client.getIdleTimeout(), TimeoutType.IDLE_TIMEOUT);
    }

    protected final void send(Connection conn, Request request) {
        Map<Integer, Response> res = responses.get(conn);
        if (res != null) {
            if (res.size() < 10) {
                int id = nextId.incrementAndGet() & 0x7fffffff;
                res.put(id, new Response(request.result, request.timeout));
                conn.send(request.buffer, id);
            } else {
                idleConnections.offer(conn);
                requests.offer(request);
            }
        }
    }

    protected final int geRealPoolSize() {
        return responses.size();
    }

    @Override
    @SuppressWarnings("unchecked")
    public final void close() {
        timer.clear();
        close((Map<Connection, Object>) (Object) responses);
    }

    public void onConnect(Connection conn) {
        responses.put(conn, new ConcurrentHashMap<>());
    }

    public void onConnected(Connection conn) {
        idleConnections.offer(conn);
        recycle(conn);
    }

    public final void onTimeout(Connection conn, TimeoutType type) {
        if (TimeoutType.CONNECT_TIMEOUT == type) {
            responses.remove(conn);
            Request request;
            while ((request = requests.poll()) != null) {
                request.result.reject(new TimeoutException("connect timeout"));
            }
        } else if (TimeoutType.IDLE_TIMEOUT != type) {
            Map<Integer, Response> res = responses.get(conn);
            if (res != null) {
                Iterator<Map.Entry<Integer, Response>> it = res.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<Integer, Response> entry = it.next();
                    it.remove();
                    Response response = entry.getValue();
                    response.result.reject(new TimeoutException(type.toString()));
                }
            }
        }
    }

    public final void onReceived(Connection conn, ByteBuffer data, Integer id) {
        Map<Integer, Response> res = responses.get(conn);
        if (res != null) {
            Response response = res.remove(id);
            if (response != null) {
                if (data.position() != 0) {
                    data.flip();
                }
                response.result.resolve(data);
            }
            if (res.isEmpty()) {
                recycle(conn);
            }
        }
    }

    public final void onSended(Connection conn, Integer id) {
        idleConnections.offer(conn);
    }

    public final void onError(Connection conn, Exception e) {
        Map<Integer, Response> res = responses.remove(conn);
        if (res != null) {
            Iterator<Map.Entry<Integer, Response>> it = res.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Integer, Response> entry = it.next();
                it.remove();
                Response response = entry.getValue();
                response.result.reject(e);
            }
        }
    }
}

final class HalfDuplexSocketTransporter extends SocketTransporter {
    private final static Response nullResponse = new Response(null, 0);
    private final Map<Connection, Response> responses = new ConcurrentHashMap<>();
    private final Timer timer = new Timer(() -> {
        long currentTime = System.currentTimeMillis();
        Iterator<Map.Entry<Connection, Response>> it = responses.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Connection, Response> entry = it.next();
            Connection conn = entry.getKey();
            Response response = entry.getValue();
            if ((currentTime - response.createTime) >= response.timeout) {
                it.remove();
                response.result.reject(new TimeoutException("timeout"));
                conn.close();
            }
        }
    });

    public HalfDuplexSocketTransporter(ZibraTcpClient client) {
        super(client);
        init();
    }

    private void init() {
        int timeout = Math.min(client.getTimeout(), client.getConnectTimeout());
        timeout = Math.min(timeout, client.getReadTimeout());
        timeout = Math.min(timeout, client.getWriteTimeout());
        timeout = Math.max(timeout, 1000);
        timer.setInterval((timeout + 1) >> 1);
        start();
    }

    private void recycle(Connection conn) {
        idleConnections.offer(conn);
        conn.setTimeout(client.getIdleTimeout(), TimeoutType.IDLE_TIMEOUT);
    }

    protected final void send(Connection conn, Request request) {
        responses.put(conn, new Response(request.result, request.timeout));
        conn.send(request.buffer, null);
    }

    protected final int geRealPoolSize() {
        return responses.size();
    }

    @Override
    @SuppressWarnings("unchecked")
    public final void close() {
        timer.clear();
        close((Map<Connection, Object>) (Object) responses);
    }

    public void onConnect(Connection conn) {
        responses.put(conn, nullResponse);
    }

    public void onConnected(Connection conn) {
        recycle(conn);
    }

    public final void onTimeout(Connection conn, TimeoutType type) {
        if (TimeoutType.CONNECT_TIMEOUT == type) {
            responses.remove(conn);
            Request request;
            while ((request = requests.poll()) != null) {
                request.result.reject(new TimeoutException("connect timeout"));
            }
        } else if (TimeoutType.IDLE_TIMEOUT != type) {
            Response response = responses.put(conn, nullResponse);
            if (response != null && response != nullResponse) {
                response.result.reject(new TimeoutException(type.toString()));
            }
        }
        conn.close();
    }

    public final void onReceived(Connection conn, ByteBuffer data, Integer id) {
        Response response = responses.put(conn, nullResponse);
        recycle(conn);
        if (response != null && response != nullResponse) {
            if (data.position() != 0) {
                data.flip();
            }
            response.result.resolve(data);
        }
    }

    public final void onSended(Connection conn, Integer id) {
    }

    public final void onError(Connection conn, Exception e) {
        Response response = responses.remove(conn);
        if (response != null && response != nullResponse) {
            response.result.reject(e);
        }
    }
}

final class Result {
    public volatile ByteBuffer buffer;
    public volatile Throwable e;
}

public class ZibraTcpClient extends ZibraClient {
    private static int reactorThreads = 2;

    public static int getReactorThreads() {
        return reactorThreads;
    }

    public static void setReactorThreads(int aReactorThreads) {
        reactorThreads = aReactorThreads;
    }

    private volatile boolean fullDuplex = false;
    private volatile boolean noDelay = false;
    private volatile int maxPoolSize = 2;
    private volatile int idleTimeout = 30000;
    private volatile int readTimeout = 30000;
    private volatile int writeTimeout = 30000;
    private volatile int connectTimeout = 30000;
    private volatile boolean keepAlive = true;
    private final SocketTransporter fdTrans = new FullDuplexSocketTransporter(this);
    private final SocketTransporter hdTrans = new HalfDuplexSocketTransporter(this);

    public ZibraTcpClient() {
        super();
    }

    public ZibraTcpClient(String uri) {
        super(uri);
    }

    public ZibraTcpClient(ZibraMode mode) {
        super(mode);
    }

    public ZibraTcpClient(String uri, ZibraMode mode) {
        super(uri, mode);
    }

    public ZibraTcpClient(String[] uris) {
        super(uris);
    }

    public ZibraTcpClient(String[] uris, ZibraMode mode) {
        super(uris, mode);
    }

    public static ZibraClient create(String uri, ZibraMode mode) throws IOException, URISyntaxException {
        String scheme = (new URI(uri)).getScheme().toLowerCase();
        if (!scheme.equals("tcp") &&
                !scheme.equals("tcp4") &&
                !scheme.equals("tcp6")) {
            throw new ZibraException("This client doesn't support " + scheme + " scheme.");
        }
        return new ZibraTcpClient(uri, mode);
    }

    public static ZibraClient create(String[] uris, ZibraMode mode) throws IOException, URISyntaxException {
        int i = 0, n = uris.length;
        while (i < n) {
            String scheme = (new URI(uris[i])).getScheme().toLowerCase();
            if (!scheme.equals("tcp") &&
                    !scheme.equals("tcp4") &&
                    !scheme.equals("tcp6")) {
                throw new ZibraException("This client doesn't support " + scheme + " scheme.");
            }
            ++i;
        }
        return new ZibraTcpClient(uris, mode);
    }

    @Override
    public final void close() {
        fdTrans.close();
        hdTrans.close();
        super.close();
    }

    public final boolean isFullDuplex() {
        return fullDuplex;
    }

    public final void setFullDuplex(boolean fullDuplex) {
        this.fullDuplex = fullDuplex;
    }

    public final boolean isNoDelay() {
        return noDelay;
    }

    public final void setNoDelay(boolean noDelay) {
        this.noDelay = noDelay;
    }

    public final int getMaxPoolSize() {
        return maxPoolSize;
    }

    public final void setMaxPoolSize(int maxPoolSize) {
        if (maxPoolSize < 1) throw new IllegalArgumentException("maxPoolSize must be great than 0");
        this.maxPoolSize = maxPoolSize;
    }

    public final int getIdleTimeout() {
        return idleTimeout;
    }

    public final void setIdleTimeout(int idleTimeout) {
        if (idleTimeout < 0) throw new IllegalArgumentException("idleTimeout must be great than -1");
        this.idleTimeout = idleTimeout;
    }

    public final int getReadTimeout() {
        return readTimeout;
    }

    public final void setReadTimeout(int readTimeout) {
        if (readTimeout < 1) throw new IllegalArgumentException("readTimeout must be great than 0");
        this.readTimeout = readTimeout;
    }

    public final int getWriteTimeout() {
        return writeTimeout;
    }

    public final void setWriteTimeout(int writeTimeout) {
        if (writeTimeout < 1) throw new IllegalArgumentException("writeTimeout must be great than 0");
        this.writeTimeout = writeTimeout;
    }

    public final int getConnectTimeout() {
        return connectTimeout;
    }

    public final void setConnectTimeout(int connectTimeout) {
        if (connectTimeout < 1) throw new IllegalArgumentException("connectTimeout must be great than 0");
        this.connectTimeout = connectTimeout;
    }

    public final boolean isKeepAlive() {
        return keepAlive;
    }

    public final void setKeepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
    }

    protected Promise<ByteBuffer> sendAndReceive(final ByteBuffer request, ClientContext context) {
        final InvokeSettings settings = context.getSettings();
        if (fullDuplex) {
            return fdTrans.send(request, settings.getTimeout());
        } else {
            return hdTrans.send(request, settings.getTimeout());
        }
    }

}