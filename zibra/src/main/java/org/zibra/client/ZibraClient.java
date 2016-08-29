package org.zibra.client;

import org.zibra.common.*;
import org.zibra.io.ByteBufferStream;
import org.zibra.io.ZibraMode;
import org.zibra.io.serialize.Writer;
import org.zibra.io.unserialize.Reader;
import org.zibra.util.ClassUtil;
import org.zibra.util.StrUtil;
import org.zibra.util.concurrent.*;

import static org.zibra.io.Tags.TagEnd;
import static org.zibra.io.Tags.TagArgument;
import static org.zibra.io.Tags.TagResult;
import static org.zibra.io.Tags.TagCall;
import static org.zibra.io.Tags.TagError;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


public abstract class ZibraClient extends HandlerManager {
    private final static Object[] nullArgs = new Object[0];
    private final ArrayList<HproseFilter> filters = new ArrayList<>();
    private final ArrayList<String> uris = new ArrayList<>();
    private final AtomicInteger index = new AtomicInteger(-1);
    private ZibraMode mode;
    private int timeout = 30000;
    private int retry = 10;
    private boolean idempotent = false;
    private boolean failswitch = false;
    private boolean byref = false;
    private boolean simple = false;
    protected String uri;
    public HproseErrorEvent onError = null;

    protected ZibraClient() {
        this((String[]) null, ZibraMode.MemberMode);
    }

    protected ZibraClient(ZibraMode mode) {
        this((String[]) null, mode);
    }

    protected ZibraClient(String uri) {
        this(uri, ZibraMode.MemberMode);
    }

    protected ZibraClient(String uri, ZibraMode mode) {
        this(uri == null ? null : new String[]{uri}, mode);
    }

    protected ZibraClient(String[] uris) {
        this(uris, ZibraMode.MemberMode);
    }

    protected ZibraClient(String[] uris, ZibraMode mode) {
        this.mode = mode;
        if (uris != null) {
            useService(uris);
        }
    }

    public void close() {
    }

    private final static HashMap<String, Class<? extends ZibraClient>> clientFactories = new HashMap<>();

    public static void registerClientFactory(String scheme, Class<? extends ZibraClient> clientClass) {
        synchronized (clientFactories) {
            clientFactories.put(scheme, clientClass);
        }
    }

    static {
        registerClientFactory("tcp", ZibraTcpClient.class);
        registerClientFactory("tcp4", ZibraTcpClient.class);
        registerClientFactory("tcp6", ZibraTcpClient.class);
        registerClientFactory("http", ZibraHttpClient.class);
        registerClientFactory("https", ZibraHttpClient.class);
    }

    public static ZibraClient create(String uri) throws IOException, URISyntaxException {
        return create(new String[]{uri}, ZibraMode.MemberMode);
    }

    public static ZibraClient create(String uri, ZibraMode mode) throws IOException, URISyntaxException {
        return create(new String[]{uri}, mode);
    }

    public static ZibraClient create(String[] uris, ZibraMode mode) throws IOException, URISyntaxException {
        String scheme = (new URI(uris[0])).getScheme().toLowerCase();
        for (int i = 1, n = uris.length; i < n; ++i) {
            if (!(new URI(uris[i])).getScheme().toLowerCase().equalsIgnoreCase(scheme)) {
                throw new ZibraException("Not support multiple protocol.");
            }
        }
        Class<? extends ZibraClient> clientClass = clientFactories.get(scheme);
        if (clientClass != null) {
            try {
                ZibraClient client = clientClass.newInstance();
                client.mode = mode;
                client.useService(uris);
                return client;
            } catch (Exception ex) {
                throw new ZibraException("This client doesn't support " + scheme + " scheme.");
            }
        }
        throw new ZibraException("This client doesn't support " + scheme + " scheme.");
    }

    public final int getTimeout() {
        return timeout;
    }

    public final void setTimeout(int timeout) {
        if (timeout < 1) throw new IllegalArgumentException("timeout must be great than 0");
        this.timeout = timeout;
    }

    public final int getRetry() {
        return retry;
    }

    public final void setRetry(int retry) {
        this.retry = retry;
    }

    public final boolean isIdempotent() {
        return idempotent;
    }

    public final void setIdempotent(boolean idempotent) {
        this.idempotent = idempotent;
    }

    public final boolean isFailswitch() {
        return failswitch;
    }

    public final void setFailswitch(boolean failswitch) {
        this.failswitch = failswitch;
    }

    public final boolean isByref() {
        return byref;
    }

    public final void setByref(boolean byref) {
        this.byref = byref;
    }

    public final boolean isSimple() {
        return simple;
    }

    public final void setSimple(boolean simple) {
        this.simple = simple;
    }

    public final HproseFilter getFilter() {
        if (filters.isEmpty()) {
            return null;
        }
        return filters.get(0);
    }

    public final void setFilter(HproseFilter filter) {
        if (!filters.isEmpty()) {
            filters.clear();
        }
        if (filter != null) {
            filters.add(filter);
        }
    }

    public final void addFilter(HproseFilter filter) {
        if (filter != null) {
            filters.add(filter);
        }
    }

    public final boolean removeFilter(HproseFilter filter) {
        return filters.remove(filter);
    }

    public final void useService(String uri) {
        useService(new String[]{uri});
    }

    public final void useService(String[] uris) {
        this.uris.clear();
        int n = uris.length;
        this.uris.addAll(Arrays.asList(uris).subList(0, n));
        if (n > 0) {
            index.set((int) Math.floor(Math.random() * n));
            this.uri = uris[index.get()];
        }
    }

    public final <T> T useService(Class<T> type) {
        return useService(type, null);
    }

    public final <T> T useService(String uri, Class<T> type) {
        return useService(uri, type, null);
    }

    public final <T> T useService(String[] uris, Class<T> type) {
        return useService(uris, type, null);
    }

    @SuppressWarnings("unchecked")
    public final <T> T useService(Class<T> type, String ns) {
        ZibraInvocationHandler handler = new ZibraInvocationHandler(this, ns);
        if (type.isInterface()) {
            return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler);
        } else {
            return (T) Proxy.newProxyInstance(type.getClassLoader(), type.getInterfaces(), handler);
        }
    }

    public final <T> T useService(String uri, Class<T> type, String ns) {
        useService(uri);
        return useService(type, ns);
    }

    public final <T> T useService(String[] uris, Class<T> type, String ns) {
        useService(uris);
        return useService(type, ns);
    }

    private ByteBuffer outputFilter(ByteBuffer request, ClientContext context) {
        if (request.position() != 0) {
            request.flip();
        }
        for (HproseFilter filter : filters) {
            request = filter.outputFilter(request, context);
            if (request.position() != 0) {
                request.flip();
            }
        }
        return request;
    }

    private ByteBuffer inputFilter(ByteBuffer response, ClientContext context) {
        if (response.position() != 0) {
            response.flip();
        }
        for (int i = filters.size() - 1; i >= 0; --i) {
            response = filters.get(i).inputFilter(response, context);
            if (response.position() != 0) {
                response.flip();
            }
        }
        return response;
    }

    private Promise<ByteBuffer> beforeFilterHandler(ByteBuffer request, final ClientContext context) {
        request = outputFilter(request, context);
        return afterFilterHandler.handle(request, context).then(new Func<ByteBuffer, ByteBuffer>() {
            public ByteBuffer call(ByteBuffer response) throws Throwable {
                if (context.getSettings().isOneway()) return null;
                response = inputFilter(response, context);
                return response;
            }
        });
    }

    private Promise<ByteBuffer> afterFilterHandler(ByteBuffer request, ClientContext context) {
        return sendAndReceive(request, context);
    }

    private Promise<ByteBuffer> sendRequest(final ByteBuffer request, final ClientContext context) {
        return beforeFilterHandler.handle(request, context).catchError(
                new AsyncFunc<ByteBuffer, Throwable>() {
                    public Promise<ByteBuffer> call(Throwable e) throws Throwable {
                        Promise<ByteBuffer> response = retry(request, context);
                        if (response != null) {
                            return response;
                        }
                        throw e;
                    }
                }
        );
    }

    private Promise<ByteBuffer> retry(final ByteBuffer request, final ClientContext context) {
        InvokeSettings settings = context.getSettings();
        if (settings.isFailswitch()) {
            failswitch();
        }
        if (settings.isIdempotent()) {
            int n = settings.getRetry();
            if (n > 0) {
                settings.setRetry(n - 1);
                int interval = (n >= 10) ? 500 : (10 - n) * 500;
                return Promise.delayed(interval, () -> sendRequest(request, context));
            }
        }
        return null;
    }

    private void failswitch() {
        int n = uris.size();
        if (n > 1) {
            int i = index.get() + (int) Math.floor(Math.random() * (n - 1)) + 1;
            if (i >= n) {
                i %= n;
            }
            index.set(i);
            uri = uris.get(i);
        }
    }

    private ClientContext getContext(InvokeSettings settings) {
        ClientContext context = new ClientContext(this);
        context.getSettings().copyFrom(settings);
        if (settings.getUserData() != null) {
            context.getUserData().putAll(settings.getUserData());
        }
        return context;
    }

    private ByteBufferStream encode(String name, Object[] args, ClientContext context) throws Throwable {
        ByteBufferStream stream = new ByteBufferStream();
        InvokeSettings settings = context.getSettings();
        Writer writer = new Writer(stream.getOutputStream(), mode, settings.isSimple());
        stream.write(TagCall);
        writer.writeString(name);
        if ((args != null) && (args.length > 0 || settings.isByref())) {
            writer.reset();
            for (int i = 0, n = args.length; i < n; ++i) {
                if (args[i] instanceof Future) {
                    args[i] = ((Future) args[i]).get(settings.getTimeout(), TimeUnit.MILLISECONDS);
                }
            }
            writer.writeArray(args);
            if (settings.isByref()) {
                writer.writeBoolean(true);
            }
        }
        stream.write(TagEnd);
        return stream;
    }

    private Object getRaw(ByteBufferStream stream, Type returnType) throws ZibraException {
        stream.flip();
        if (returnType == null ||
                returnType == Object.class ||
                returnType == ByteBuffer.class ||
                returnType == Buffer.class) {
            return stream.buffer;
        } else if (returnType == ByteBufferStream.class) {
            return stream;
        } else if (returnType == byte[].class) {
            byte[] bytes = stream.toArray();
            stream.close();
            return bytes;
        }
        throw new ZibraException("Can't Convert ByteBuffer to Type: " + returnType.toString());
    }

    private Object decode(ByteBufferStream stream, Object[] args, ClientContext context) throws IOException {
        InvokeSettings settings = context.getSettings();
        if (settings.isOneway()) {
            return null;
        }
        if (stream.available() == 0) throw new ZibraException("EOF");
        int tag = stream.buffer.get(stream.buffer.limit() - 1);
        if (tag != TagEnd) {
            throw new ZibraException("Wrong Response: \r\n" + StrUtil.toString(stream));
        }
        HproseResultMode resultMode = settings.getMode();
        Type returnType = settings.getReturnType();
        if (resultMode == HproseResultMode.RawWithEndTag) {
            return getRaw(stream, returnType);
        } else if (resultMode == HproseResultMode.Raw) {
            stream.buffer.limit(stream.buffer.limit() - 1);
            return getRaw(stream, returnType);
        }
        Object result = null;
        Reader reader = new Reader(stream.getInputStream(), mode);
        tag = stream.read();
        if (tag == TagResult) {
            if (resultMode == HproseResultMode.Normal) {
                result = reader.unserialize(returnType);
            } else if (resultMode == HproseResultMode.Serialized) {
                result = getRaw(reader.readRaw(), returnType);
            }
            tag = stream.read();
            if (tag == TagArgument) {
                reader.reset();
                Object[] arguments = reader.readObjectArray();
                int length = args.length;
                if (length > arguments.length) {
                    length = arguments.length;
                }
                System.arraycopy(arguments, 0, args, 0, length);
                tag = stream.read();
            }
        } else if (tag == TagError) {
            throw new ZibraException(reader.readString());
        }
        if (tag != TagEnd) {
            stream.rewind();
            throw new ZibraException("Wrong Response: \r\n" + StrUtil.toString(stream));
        }
        return result;
    }

    @Override
    protected Promise<Object> invokeHandler(String name, Object[] args, ZibraContext context) {
        return invokeHandler(name, args, (ClientContext) context);
    }

    @Override
    protected Promise<ByteBuffer> beforeFilterHandler(ByteBuffer request, ZibraContext context) {
        return beforeFilterHandler(request, (ClientContext) context);
    }

    @Override
    protected Promise<ByteBuffer> afterFilterHandler(ByteBuffer request, ZibraContext context) {
        return afterFilterHandler(request, (ClientContext) context);
    }

    @SuppressWarnings("unchecked")
    private Promise<Object> invokeHandler(String name, final Object[] args, final ClientContext context) {
        final ByteBufferStream stream;
        try {
            stream = encode(name, args, context);
        } catch (Throwable e) {
            return Promise.error(e);
        }
        final InvokeSettings settings = context.getSettings();
        return sendRequest(stream.buffer, context).then(new Func<Object, ByteBuffer>() {
            public Object call(ByteBuffer value) throws Throwable {
                stream.buffer = value;
                try {
                    return decode(stream, args, context);
                } finally {
                    if (settings.getMode() == HproseResultMode.Normal ||
                            settings.getMode() == HproseResultMode.Serialized ||
                            settings.getReturnType() == byte[].class) {
                        stream.close();
                    }
                }
            }
        });
    }

    protected abstract Promise<ByteBuffer> sendAndReceive(ByteBuffer request, ClientContext context);

    public final void invoke(String name, HproseCallback1<?> callback) {
        invoke(name, nullArgs, callback, null, null, null);
    }

    public final void invoke(String name, HproseCallback1<?> callback, HproseErrorEvent errorEvent) {
        invoke(name, nullArgs, callback, errorEvent, null, null);
    }

    public final void invoke(String name, HproseCallback1<?> callback, InvokeSettings settings) {
        invoke(name, nullArgs, callback, null, null, settings);
    }

    public final void invoke(String name, HproseCallback1<?> callback, HproseErrorEvent errorEvent, InvokeSettings settings) {
        invoke(name, nullArgs, callback, errorEvent, null, settings);
    }

    public final void invoke(String name, Object[] args, HproseCallback1<?> callback) {
        invoke(name, args, callback, null, null, null);
    }

    public final void invoke(String name, Object[] args, HproseCallback1<?> callback, HproseErrorEvent errorEvent) {
        invoke(name, args, callback, errorEvent, null, null);
    }

    public final void invoke(String name, Object[] args, HproseCallback1<?> callback, InvokeSettings settings) {
        invoke(name, args, callback, null, null, settings);
    }

    public final void invoke(String name, Object[] args, HproseCallback1<?> callback, HproseErrorEvent errorEvent, InvokeSettings settings) {
        invoke(name, args, callback, errorEvent, null, settings);
    }

    public final <T> void invoke(String name, HproseCallback1<T> callback, Class<T> returnType) {
        invoke(name, nullArgs, callback, null, returnType, null);
    }

    public final <T> void invoke(String name, HproseCallback1<T> callback, HproseErrorEvent errorEvent, Class<T> returnType) {
        invoke(name, nullArgs, callback, errorEvent, returnType, null);
    }

    public final <T> void invoke(String name, HproseCallback1<T> callback, Class<T> returnType, InvokeSettings settings) {
        invoke(name, nullArgs, callback, null, returnType, settings);
    }

    public final <T> void invoke(String name, HproseCallback1<T> callback, HproseErrorEvent errorEvent, Class<T> returnType, InvokeSettings settings) {
        invoke(name, nullArgs, callback, errorEvent, returnType, settings);
    }

    public final <T> void invoke(String name, Object[] args, HproseCallback1<T> callback, Class<T> returnType) {
        invoke(name, args, callback, null, returnType, null);
    }

    public final <T> void invoke(String name, Object[] args, HproseCallback1<T> callback, HproseErrorEvent errorEvent, Class<T> returnType) {
        invoke(name, args, callback, errorEvent, returnType, null);
    }

    public final <T> void invoke(String name, Object[] args, HproseCallback1<T> callback, Class<T> returnType, InvokeSettings settings) {
        invoke(name, args, callback, null, returnType, settings);
    }

    @SuppressWarnings("unchecked")
    public final <T> void invoke(final String name, Object[] args, final HproseCallback1<T> callback, final HproseErrorEvent errorEvent, Class<T> returnType, InvokeSettings settings) {
        if (settings == null) settings = new InvokeSettings();
        if (returnType != null) settings.setReturnType(returnType);
        final HproseErrorEvent errEvent = (errorEvent == null) ? onError : errorEvent;
        settings.setAsync(true);
        final ZibraContext context = getContext(settings);
        Promise.all(args).then(args1 -> {
            invokeHandler.handle(name, args1, context).then(
                    value -> callback.handler((T) value),
                    e -> {
                        if (errEvent != null) {
                            errEvent.handler(name, e);
                        }
                    }
            );
        });
    }

    public final void invoke(String name, Object[] args, HproseCallback<?> callback) {
        invoke(name, args, callback, null, null, null);
    }

    public final void invoke(String name, Object[] args, HproseCallback<?> callback, HproseErrorEvent errorEvent) {
        invoke(name, args, callback, errorEvent, null, null);
    }

    public final void invoke(String name, Object[] args, HproseCallback<?> callback, InvokeSettings settings) {
        invoke(name, args, callback, null, null, settings);
    }

    public final void invoke(String name, Object[] args, HproseCallback<?> callback, HproseErrorEvent errorEvent, InvokeSettings settings) {
        invoke(name, args, callback, errorEvent, null, settings);
    }

    public final <T> void invoke(String name, Object[] args, HproseCallback<T> callback, Class<T> returnType) {
        invoke(name, args, callback, null, returnType, null);
    }

    public final <T> void invoke(String name, Object[] args, HproseCallback<T> callback, HproseErrorEvent errorEvent, Class<T> returnType) {
        invoke(name, args, callback, errorEvent, returnType, null);
    }

    public final <T> void invoke(String name, Object[] args, HproseCallback<T> callback, Class<T> returnType, InvokeSettings settings) {
        invoke(name, args, callback, null, returnType, settings);
    }

    @SuppressWarnings("unchecked")
    public final <T> void invoke(final String name, final Object[] args, final HproseCallback<T> callback, final HproseErrorEvent errorEvent, Class<T> returnType, InvokeSettings settings) {
        if (settings == null) settings = new InvokeSettings();
        if (returnType != null) settings.setReturnType(returnType);
        final HproseErrorEvent errEvent = (errorEvent == null) ? onError : errorEvent;
        settings.setAsync(true);
        final ZibraContext context = getContext(settings);
        Promise.all(args).then(args1 -> {
            invokeHandler.handle(name, args1, context).then(
                    value -> callback.handler((T) value, args1),
                    e -> {
                        if (errEvent != null) {
                            errEvent.handler(name, e);
                        }
                    }
            );
        });
    }

    public final Object invoke(String name) throws Throwable {
        return invoke(name, nullArgs, (Class<?>) null, null);
    }

    public final Object invoke(String name, InvokeSettings settings) throws Throwable {
        return invoke(name, nullArgs, (Class<?>) null, settings);
    }

    public final Object invoke(String name, Object[] args) throws Throwable {
        return invoke(name, args, (Class<?>) null, null);
    }

    public final Object invoke(String name, Object[] args, InvokeSettings settings) throws Throwable {
        return invoke(name, args, (Class<?>) null, settings);
    }

    public final <T> T invoke(String name, Class<T> returnType) throws Throwable {
        return invoke(name, nullArgs, returnType, null);
    }

    public final <T> T invoke(String name, Class<T> returnType, InvokeSettings settings) throws Throwable {
        return invoke(name, nullArgs, returnType, settings);
    }

    public final <T> T invoke(String name, Object[] args, Class<T> returnType) throws Throwable {
        return invoke(name, args, returnType, null);
    }

    @SuppressWarnings("unchecked")
    public final <T> T invoke(String name, Object[] args, Class<T> returnType, InvokeSettings settings) throws Throwable {
        if (settings == null) settings = new InvokeSettings();
        if (returnType != null) settings.setReturnType(returnType);
        Type type = settings.getReturnType();
        Class<?> cls = ClassUtil.toClass(type);
        if (Promise.class.equals(cls)) {
            return (T) asyncInvoke(name, args, type, settings);
        }
        if (Future.class.equals(cls)) {
            return (T) asyncInvoke(name, args, type, settings).toFuture();
        }
        if (settings.isAsync()) {
            return (T) asyncInvoke(name, args, type, settings);
        }
        if (args != null) {
            for (int i = 0, n = args.length; i < n; ++i) {
                if (args[i] instanceof Promise) {
                    args[i] = ((Promise) args[i]).toFuture();
                }
            }
        }
        return ((Promise<T>) invokeHandler.handle(name, args, getContext(settings))).toFuture().get();
    }

    @SuppressWarnings("unchecked")
    private Promise<?> asyncInvoke(final String name, Object[] args, Type type, InvokeSettings settings) {
        settings.setAsync(true);
        if (type instanceof ParameterizedType) {
            type = ((ParameterizedType) type).getActualTypeArguments()[0];
            if (void.class.equals(type) || Void.class.equals(type)) {
                type = null;
            }
        } else {
            Class<?> cls = ClassUtil.toClass(type);
            if (Promise.class.equals(cls) || Future.class.equals(cls)) {
                type = null;
            }
        }
        settings.setReturnType(type);
        final ZibraContext context = getContext(settings);
        return Promise.all(args).then(new AsyncFunc<Object, Object[]>() {
            public Promise<Object> call(Object[] args) throws Throwable {
                return invokeHandler.handle(name, args, context);
            }
        });
    }

    private static class Topic<T> {
        Action<T> handler;
        final ConcurrentLinkedQueue<Action<T>> callbacks = new ConcurrentLinkedQueue<>();
    }

    private final ConcurrentHashMap<String, ConcurrentHashMap<String, Topic<?>>> allTopics = new ConcurrentHashMap<>();

    private Topic<?> getTopic(String name, String id, boolean create) {
        ConcurrentHashMap<String, Topic<?>> topics = allTopics.get(name);
        if (topics != null) {
            return topics.get(id);
        }
        if (create) {
            allTopics.put(name, new ConcurrentHashMap<>());
        }
        return null;
    }

    private static final InvokeSettings autoIdSettings = new InvokeSettings();
    private volatile String autoId = null;

    static {
        autoIdSettings.setReturnType(String.class);
        autoIdSettings.setSimple(true);
        autoIdSettings.setIdempotent(true);
        autoIdSettings.setFailswitch(true);
        autoIdSettings.setAsync(true);
    }

    @SuppressWarnings("unchecked")
    private synchronized String autoId() {
        if (autoId == null) {
            try {
                Promise<String> id = (Promise<String>) this.invoke("#", autoIdSettings);
                autoId = id.toFuture().get(timeout, TimeUnit.MILLISECONDS);
            } catch (Throwable e) {
                if (onError != null) {
                    onError.handler("autoId", e);
                }
            }
        }
        return autoId;
    }

    public final void subscribe(String name, Action<Object> callback) {
        subscribe(name, callback, Object.class, timeout);
    }

    public final void subscribe(String name, Action<Object> callback, int timeout) {
        subscribe(name, callback, Object.class, timeout);
    }

    public final void subscribe(String name, String id, Action<Object> callback) {
        subscribe(name, id, callback, Object.class, timeout);
    }

    public final void subscribe(String name, String id, Action<Object> callback, int timeout) {
        subscribe(name, id, callback, Object.class, timeout);
    }

    public final <T> void subscribe(String name, Action<T> callback, Type type) {
        subscribe(name, callback, type, timeout);
    }

    public final <T> void subscribe(String name, Action<T> callback, Type type, int timeout) {
        subscribe(name, autoId(), callback, type, timeout);
    }

    public final <T> void subscribe(String name, String id, Action<T> callback, Type type) {
        subscribe(name, id, callback, type, timeout);
    }

    public final <T> void subscribe(final String name, final String id, Action<T> callback, final Type type, final int timeout) {
        subscribe(name, id, callback, type, timeout, false);
    }

    public final void subscribe(String name, Action<Object> callback, boolean failswitch) {
        subscribe(name, callback, Object.class, timeout, failswitch);
    }

    public final void subscribe(String name, Action<Object> callback, int timeout, boolean failswitch) {
        subscribe(name, callback, Object.class, timeout, failswitch);
    }

    public final void subscribe(String name, String id, Action<Object> callback, boolean failswitch) {
        subscribe(name, id, callback, Object.class, timeout, failswitch);
    }

    public final void subscribe(String name, String id, Action<Object> callback, int timeout, boolean failswitch) {
        subscribe(name, id, callback, Object.class, timeout, failswitch);
    }

    public final <T> void subscribe(String name, Action<T> callback, Type type, boolean failswitch) {
        subscribe(name, callback, type, timeout, failswitch);
    }

    public final <T> void subscribe(String name, Action<T> callback, Type type, int timeout, boolean failswitch) {
        subscribe(name, autoId(), callback, type, timeout, failswitch);
    }

    public final <T> void subscribe(String name, String id, Action<T> callback, Type type, boolean failswitch) {
        subscribe(name, id, callback, type, timeout, failswitch);
    }

    @SuppressWarnings("unchecked")
    public final <T> void subscribe(final String name, final String id, Action<T> callback, final Type type, final int timeout, final boolean failswitch) {
        Topic<T> topic = (Topic<T>) getTopic(name, id, true);
        if (topic == null) {
            final Action<Throwable> cb = new Action<Throwable>() {
                public void call(Throwable e) throws Throwable {
                    Topic<T> topic = (Topic<T>) getTopic(name, id, false);
                    if (topic != null) {
                        InvokeSettings settings = new InvokeSettings();
                        settings.setIdempotent(true);
                        settings.setFailswitch(failswitch);
                        settings.setReturnType(type);
                        settings.setTimeout(timeout);
                        settings.setAsync(true);
                        Promise<T> result = (Promise<T>) invokeHandler.handle(name, new Object[]{id}, getContext(settings));
                        result.then(topic.handler, this);
                    }
                }
            };
            topic = new Topic<>();
            topic.handler = result -> {
                Topic topic1 = getTopic(name, id, false);
                if (topic1 != null) {
                    if (result != null) {
                        ConcurrentLinkedQueue<Action<T>> callbacks = topic1.callbacks;
                        for (Action<T> callback1 : callbacks) {
                            try {
                                callback1.call(result);
                            } catch (Throwable ignored) {
                            }
                        }
                    }
                    cb.call(null);
                }
            };
            topic.callbacks.offer(callback);
            allTopics.get(name).put(id, topic);
            try {
                cb.call(null);
            } catch (Throwable e) {
                if (onError != null) {
                    onError.handler(name, e);
                }
            }
        } else if (!topic.callbacks.contains(callback)) {
            topic.callbacks.offer(callback);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> void delTopic(ConcurrentHashMap<String, Topic<?>> topics, String id, Action<T> callback) {
        if (topics != null && topics.size() > 0) {
            if (callback != null) {
                Topic<T> topic = (Topic<T>) topics.get(id);
                if (topic != null) {
                    ConcurrentLinkedQueue<Action<T>> callbacks = topic.callbacks;
                    callbacks.remove(callback);
                    if (callbacks.isEmpty()) {
                        topics.remove(id);
                    }
                }
            } else {
                topics.remove(id);
            }
        }
    }

    public void unsubscribe(String name) {
        unsubscribe(name, null, null);
    }

    public <T> void unsubscribe(String name, Action<T> callback) {
        unsubscribe(name, null, callback);
    }

    public void unsubscribe(String name, String id) {
        unsubscribe(name, id, null);
    }

    public <T> void unsubscribe(String name, String id, final Action<T> callback) {
        final ConcurrentHashMap<String, Topic<?>> topics = allTopics.get(name);
        if (topics != null) {
            if (id == null) {
                if (autoId == null) {
                    for (String i : topics.keySet()) {
                        delTopic(topics, i, callback);
                    }
                } else {
                    delTopic(topics, autoId, callback);
                }
            } else {
                delTopic(topics, id, callback);
            }
        }
    }

    public String getId() {
        return autoId;
    }

}
