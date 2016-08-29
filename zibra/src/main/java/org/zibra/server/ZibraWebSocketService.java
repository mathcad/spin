package org.zibra.server;

import org.zibra.common.HproseMethods;
import org.zibra.common.ZibraContext;
import org.zibra.io.ByteBufferStream;

import javax.websocket.EndpointConfig;
import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;

public class ZibraWebSocketService extends ZibraService {
    private final static ThreadLocal<WebSocketContext> currentContext = new ThreadLocal<>();
    private EndpointConfig config = null;

    public static WebSocketContext getCurrentContext() {
        return currentContext.get();
    }

    @Override
    public HproseMethods getGlobalMethods() {
        if (globalMethods == null) {
            globalMethods = new ZibraWebSocketMethods();
        }
        return globalMethods;
    }

    @Override
    public void setGlobalMethods(HproseMethods methods) {
        if (methods instanceof ZibraWebSocketMethods) {
            this.globalMethods = methods;
        } else {
            throw new ClassCastException("methods must be a ZibraWebSocketMethods instance");
        }
    }

    @Override
    protected Object[] fixArguments(Type[] argumentTypes, Object[] arguments, ServiceContext context) {
        int count = arguments.length;
        WebSocketContext wsContext = (WebSocketContext) context;
        if (argumentTypes.length != count) {
            Object[] args = new Object[argumentTypes.length];
            System.arraycopy(arguments, 0, args, 0, count);
            Class<?> argType = (Class<?>) argumentTypes[count];
            if (argType.equals(ZibraContext.class) || argType.equals(ServiceContext.class)) {
                args[count] = context;
            } else if (argType.equals(WebSocketContext.class)) {
                args[count] = wsContext;
            } else if (argType.equals(EndpointConfig.class)) {
                args[count] = wsContext.getConfig();
            } else if (argType.equals(Session.class)) {
                args[count] = wsContext.getSession();
            }
            return args;
        }
        return arguments;
    }

    public void setConfig(EndpointConfig config) {
        this.config = config;
    }

    @SuppressWarnings("unchecked")
    public void handle(final ByteBuffer buf, final Session session) throws IOException {
        WebSocketContext context = new WebSocketContext(this, session, config);
        final int id = buf.getInt();
        currentContext.set(context);
        handle(buf.slice(), context).then(value -> {
            try {
                ByteBuffer buffer = ByteBuffer.allocate(4 + value.remaining());
                buffer.putInt(id);
                buffer.put(value);
                buffer.flip();
                final RemoteEndpoint.Async remote = session.getAsyncRemote();
                remote.sendBinary(buffer);
            } finally {
                ByteBufferStream.free(value);
            }
        }).whenComplete((Runnable) () -> {
            currentContext.remove();
            ByteBufferStream.free(buf);
        });
    }

    public void handleError(Session session, Throwable error) {
        WebSocketContext context = new WebSocketContext(this, session, config);
        fireErrorEvent(error, context);
    }
}
