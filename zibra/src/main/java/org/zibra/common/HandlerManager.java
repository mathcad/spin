package org.zibra.common;

import org.zibra.util.concurrent.Func;
import org.zibra.util.concurrent.Promise;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public abstract class HandlerManager {
    private final ArrayList<InvokeHandler> invokeHandlers = new ArrayList<>();
    private final ArrayList<FilterHandler> beforeFilterHandlers = new ArrayList<>();
    private final ArrayList<FilterHandler> afterFilterHandlers = new ArrayList<>();
    private final NextInvokeHandler defaultInvokeHandler = this::invokeHandler;
    private final NextFilterHandler defaultBeforeFilterHandler = (request, context) -> {
        if (request.position() != 0) {
            request.flip();
        }
        return beforeFilterHandler(request, context).then(new Func<ByteBuffer, ByteBuffer>() {
            public ByteBuffer call(ByteBuffer response) throws Throwable {
                if (response.position() != 0) {
                    response.flip();
                }
                return response;
            }
        });
    };
    private final NextFilterHandler defaultAfterFilterHandler = (request, context) -> {
        if (request.position() != 0) {
            request.flip();
        }
        return afterFilterHandler(request, context).then(new Func<ByteBuffer, ByteBuffer>() {
            public ByteBuffer call(ByteBuffer response) throws Throwable {
                if (response.position() != 0) {
                    response.flip();
                }
                return response;
            }
        });
    };
    protected NextInvokeHandler invokeHandler = defaultInvokeHandler;
    protected NextFilterHandler beforeFilterHandler = defaultBeforeFilterHandler;
    protected NextFilterHandler afterFilterHandler = defaultAfterFilterHandler;

    protected abstract Promise<Object> invokeHandler(String name, Object[] args, ZibraContext context);
    protected abstract Promise<ByteBuffer> beforeFilterHandler(ByteBuffer request, ZibraContext context);
    protected abstract Promise<ByteBuffer> afterFilterHandler(ByteBuffer request, ZibraContext context);

    private NextInvokeHandler getNextInvokeHandler(final NextInvokeHandler next, final InvokeHandler handler) {
        return (name, args, context) -> {
            try {
                return handler.handle(name, args, context, next);
            }
            catch (Throwable e) {
                return Promise.error(e);
            }
        };
    }

    private NextFilterHandler getNextFilterHandler(final NextFilterHandler next, final FilterHandler handler) {
        return (request, context) -> {
            try {
                if (request.position() != 0) {
                    request.flip();
                }
                return handler.handle(request, context, next).then(new Func<ByteBuffer, ByteBuffer>() {
                    public ByteBuffer call(ByteBuffer response) throws Throwable {
                        if (response.position() != 0) {
                            response.flip();
                        }
                        return response;
                    }
                });
            }
            catch (Throwable e) {
                return Promise.error(e);
            }
        };
    }

    public final void addInvokeHandler(InvokeHandler handler) {
        if (handler == null) return;
        invokeHandlers.add(handler);
        NextInvokeHandler next = defaultInvokeHandler;
        for (int i = invokeHandlers.size() - 1; i >= 0; --i) {
            next = getNextInvokeHandler(next, invokeHandlers.get(i));
        }
        invokeHandler = next;
    }
    public final void addBeforeFilterHandler(FilterHandler handler) {
        if (handler == null) return;
        beforeFilterHandlers.add(handler);
        NextFilterHandler next = defaultBeforeFilterHandler;
        for (int i = beforeFilterHandlers.size() - 1; i >= 0; --i) {
            next = getNextFilterHandler(next, beforeFilterHandlers.get(i));
        }
        beforeFilterHandler = next;
    }
    public final void addAfterFilterHandler(FilterHandler handler) {
        if (handler == null) return;
        afterFilterHandlers.add(handler);
        NextFilterHandler next = defaultAfterFilterHandler;
        for (int i = afterFilterHandlers.size() - 1; i >= 0; --i) {
            next = getNextFilterHandler(next, afterFilterHandlers.get(i));
        }
        afterFilterHandler = next;
    }
    public final HandlerManager use(InvokeHandler handler) {
        addInvokeHandler(handler);
        return this;
    }
    public final FilterHandlerManager beforeFilter = new FilterHandlerManager() {
        public final FilterHandlerManager use(FilterHandler handler) {
            addBeforeFilterHandler(handler);
            return this;
        }
    };
    public final FilterHandlerManager afterFilter = new FilterHandlerManager() {
        public final FilterHandlerManager use(FilterHandler handler) {
            addAfterFilterHandler(handler);
            return this;
        }
    };
}
