package org.zibra.server;

import org.zibra.common.HproseMethods;
import org.zibra.common.ZibraContext;
import org.zibra.io.ByteBufferStream;
import org.zibra.net.Acceptor;
import org.zibra.net.Connection;
import org.zibra.net.ConnectionHandler;
import org.zibra.net.TimeoutType;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

public class ZibraTcpServer extends ZibraService {

    private final static ThreadLocal<TcpContext> currentContext = new ThreadLocal<>();
    private volatile ExecutorService threadPool = null;
    private volatile int readTimeout = 30000;
    private volatile int writeTimeout = 30000;
    private boolean threadPoolEnabled = false;
    private int reactorThreads = 2;
    private Acceptor acceptor = null;
    private String host = null;
    private int port = 0;

    private final class ServerHandler implements Runnable {
        private final Connection conn;
        private final ByteBuffer data;
        private final Integer id;

        public ServerHandler(Connection conn, ByteBuffer data, Integer id) {
            this.conn = conn;
            this.data = data;
            this.id = id;
        }

        @SuppressWarnings("unchecked")
        public final void run() {
            TcpContext context = new TcpContext(ZibraTcpServer.this, conn.socketChannel());
            currentContext.set(context);
            ZibraTcpServer.this.handle(data, context).then(value -> {
                conn.send(value, id);
            }).catchError(e -> {
                conn.close();
            }).whenComplete((Runnable) () -> {
                currentContext.remove();
                ByteBufferStream.free(data);
            });
        }
    }

    private final class ServerConnectionHandler implements ConnectionHandler {

        public void onConnect(Connection conn) {
        }

        public void onConnected(Connection conn) {
            fireAcceptEvent(conn.socketChannel());
        }

        public final void onReceived(Connection conn, ByteBuffer data, Integer id) {
            ServerHandler handler = new ServerHandler(conn, data, id);
            if (threadPool != null) {
                try {
                    threadPool.execute(handler);
                } catch (RejectedExecutionException e) {
                    conn.close();
                }
            } else {
                handler.run();
            }
        }

        public final void onSended(Connection conn, Integer id) {
        }

        public final void onClose(Connection conn) {
            fireCloseEvent(conn.socketChannel());
        }

        public void onError(Connection conn, Exception e) {
            if (conn == null) {
                fireErrorEvent(e, null);
            }
        }

        public void onTimeout(Connection conn, TimeoutType type) {
        }

        public int getReadTimeout() {
            return readTimeout;
        }

        public int getWriteTimeout() {
            return writeTimeout;
        }

        public int getConnectTimeout() {
            throw new UnsupportedOperationException();
        }
    }

    public ZibraTcpServer(String uri) throws URISyntaxException {
        URI u = new URI(uri);
        host = u.getHost();
        port = u.getPort();
    }

    public ZibraTcpServer(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String value) {
        host = value;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int value) {
        port = value;
    }

    public int getReactorThreads() {
        return reactorThreads;
    }

    public void setReactorThreads(int reactorThreads) {
        this.reactorThreads = reactorThreads;
    }

    public boolean isStarted() {
        return (acceptor != null);
    }

    public void start() throws IOException {
        if (!isStarted()) {
            acceptor = new Acceptor(host, port, new ServerConnectionHandler(), reactorThreads);
            acceptor.start();
        }
    }

    public void stop() {
        if (isStarted()) {
            acceptor.close();
            if (threadPool != null && !threadPool.isShutdown()) {
                try {
                    threadPool.shutdown();
                } catch (SecurityException e) {
                    fireErrorEvent(e, null);
                }
            }
            acceptor = null;
        }
    }

    @Override
    public HproseMethods getGlobalMethods() {
        if (globalMethods == null) {
            globalMethods = new ZibraTcpMethods();
        }
        return globalMethods;
    }

    @Override
    public void setGlobalMethods(HproseMethods methods) {
        if (methods instanceof ZibraTcpMethods) {
            this.globalMethods = methods;
        } else {
            throw new ClassCastException("methods must be a ZibraTcpMethods instance");
        }
    }

    @Override
    protected Object[] fixArguments(Type[] argumentTypes, Object[] arguments, ServiceContext context) {
        int count = arguments.length;
        TcpContext tcpContext = (TcpContext) context;
        if (argumentTypes.length != count) {
            Object[] args = new Object[argumentTypes.length];
            System.arraycopy(arguments, 0, args, 0, count);
            Class<?> argType = (Class<?>) argumentTypes[count];
            if (argType.equals(ZibraContext.class) || argType.equals(ServiceContext.class)) {
                args[count] = context;
            } else if (argType.equals(TcpContext.class)) {
                args[count] = tcpContext;
            } else if (argType.equals(SocketChannel.class)) {
                args[count] = tcpContext.getSocketChannel();
            } else if (argType.equals(Socket.class)) {
                args[count] = tcpContext.getSocket();
            }
            return args;
        }
        return arguments;
    }

    public static TcpContext getCurrentContext() {
        return currentContext.get();
    }

    /**
     * Is thread pool enabled.
     * This thread pool is not for the service threads, it is for the user service method.
     * The default value is false.
     *
     * @return is thread pool enabled
     */
    public boolean isThreadPoolEnabled() {
        return threadPoolEnabled;
    }

    /**
     * Set thread pool enabled.
     * This thread pool is not for the service threads, it is for the user service method.
     * If your service method takes a long time, or will be blocked, please set this property to be true.
     *
     * @param value is thread pool enabled
     */
    public void setThreadPoolEnabled(boolean value) {
        if (value && (threadPool == null)) {
            threadPool = Executors.newCachedThreadPool();
        }
        threadPoolEnabled = value;
    }

    /**
     * get the thread pool.
     * This thread pool is not for the service threads, it is for the user service method.
     * The default value is null.
     *
     * @return the thread pool
     */
    public ExecutorService getThreadPool() {
        return threadPool;
    }

    /**
     * set the thread pool.
     * This thread pool is not for the service threads, it is for the user service method.
     * Set it to null will disable thread pool.
     *
     * @param value is the thread pool
     */
    public void setThreadPool(ExecutorService value) {
        threadPool = value;
        threadPoolEnabled = (value != null);
    }

    protected void fireAcceptEvent(SocketChannel channel) {
        if (event != null && ZibraTcpServiceEvent.class.isInstance(event)) {
            ((ZibraTcpServiceEvent) event).onAccept(new TcpContext(this, channel));
        }
    }

    protected void fireCloseEvent(SocketChannel channel) {
        if (event != null && ZibraTcpServiceEvent.class.isInstance(event)) {
            ((ZibraTcpServiceEvent) event).onClose(new TcpContext(this, channel));
        }
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public int getWriteTimeout() {
        return writeTimeout;
    }

    public void setWriteTimeout(int writeTimeout) {
        this.writeTimeout = writeTimeout;
    }

}
