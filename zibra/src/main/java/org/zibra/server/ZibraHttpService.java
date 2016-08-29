package org.zibra.server;

import org.zibra.common.HproseMethods;
import org.zibra.common.ZibraContext;
import org.zibra.io.ByteBufferStream;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;

public class ZibraHttpService extends ZibraService {
    private boolean crossDomainEnabled = false;
    private boolean p3pEnabled = false;
    private boolean getEnabled = true;
    private int asyncTimeout = 300000;
    private final HashMap<String, Boolean> origins = new HashMap<>();
    private final static ThreadLocal<HttpContext> currentContext = new ThreadLocal<>();

    public static HttpContext getCurrentContext() {
        return currentContext.get();
    }

    @Override
    public HproseMethods getGlobalMethods() {
        if (globalMethods == null) {
            globalMethods = new ZibraHttpMethods();
        }
        return globalMethods;
    }

    @Override
    public void setGlobalMethods(HproseMethods methods) {
        if (methods instanceof ZibraHttpMethods) {
            this.globalMethods = methods;
        } else {
            throw new ClassCastException("methods must be a ZibraHttpMethods instance");
        }
    }

    public boolean isCrossDomainEnabled() {
        return crossDomainEnabled;
    }

    public void setCrossDomainEnabled(boolean enabled) {
        crossDomainEnabled = enabled;
    }

    public boolean isP3pEnabled() {
        return p3pEnabled;
    }

    public void setP3pEnabled(boolean enabled) {
        p3pEnabled = enabled;
    }

    public boolean isGetEnabled() {
        return getEnabled;
    }

    public void setGetEnabled(boolean enabled) {
        getEnabled = enabled;
    }

    public int getAsyncTimeout() {
        return asyncTimeout;
    }

    public void setAsyncTimeout(int asyncTimeout) {
        this.asyncTimeout = asyncTimeout;
    }

    public void addAccessControlAllowOrigin(String origin) {
        origins.put(origin, true);
    }

    public void removeAccessControlAllowOrigin(String origin) {
        origins.remove(origin);
    }

    @Override
    protected Object[] fixArguments(Type[] argumentTypes, Object[] arguments, ServiceContext context) {
        int count = arguments.length;
        HttpContext httpContext = (HttpContext) context;
        if (argumentTypes.length != count) {
            Object[] args = new Object[argumentTypes.length];
            System.arraycopy(arguments, 0, args, 0, count);
            Class<?> argType = (Class<?>) argumentTypes[count];
            if (argType.equals(ZibraContext.class) || argType.equals(ServiceContext.class)) {
                args[count] = context;
            } else if (argType.equals(HttpContext.class)) {
                args[count] = httpContext;
            } else if (argType.equals(HttpServletRequest.class)) {
                args[count] = httpContext.getRequest();
            } else if (argType.equals(HttpServletResponse.class)) {
                args[count] = httpContext.getResponse();
            } else if (argType.equals(HttpSession.class)) {
                args[count] = httpContext.getSession();
            } else if (argType.equals(ServletContext.class)) {
                args[count] = httpContext.getApplication();
            } else if (argType.equals(ServletConfig.class)) {
                args[count] = httpContext.getConfig();
            }
            return args;
        }
        return arguments;
    }

    protected void sendHeader(HttpContext httpContext) {
        if (event != null && ZibraHttpServiceEvent.class.isInstance(event)) {
            ((ZibraHttpServiceEvent) event).onSendHeader(httpContext);
        }
        HttpServletRequest request = httpContext.getRequest();
        HttpServletResponse response = httpContext.getResponse();
        response.setContentType("text/plain");
        if (p3pEnabled) {
            response.setHeader("P3P", "CP=\"CAO DSP COR CUR ADM DEV TAI PSA PSD " +
                    "IVAi IVDi CONi TELo OTPi OUR DELi SAMi " +
                    "OTRi UNRi PUBi IND PHY ONL UNI PUR FIN " +
                    "COM NAV INT DEM CNT STA POL HEA PRE GOV\"");
        }
        if (crossDomainEnabled) {
            String origin = request.getHeader("Origin");
            if (origin != null && !origin.equals("null")) {
                if (origins.isEmpty() || origins.containsKey(origin)) {
                    response.setHeader("Access-Control-Allow-Origin", origin);
                    response.setHeader("Access-Control-Allow-Credentials", "true");
                }
            } else {
                response.setHeader("Access-Control-Allow-Origin", "*");
            }
        }
    }

    public void handle(HttpContext httpContext) {
        handle(httpContext, null);
    }

    public void handle(HttpContext httpContext, ZibraHttpMethods methods) {
        sendHeader(httpContext);
        String method = httpContext.getRequest().getMethod();
        if (method.equals("GET")) {
            if (getEnabled) {
                ByteBufferStream ostream = null;
                try {
                    httpContext.setMethods(methods);
                    ostream = doFunctionList(httpContext);
                    httpContext.getResponse().setContentLength(ostream.available());
                    ostream.writeTo(httpContext.getResponse().getOutputStream());
                } catch (Throwable ex) {
                    fireErrorEvent(ex, httpContext);
                } finally {
                    if (ostream != null) {
                        ostream.close();
                    }
                }
            } else {
                try {
                    httpContext.getResponse().sendError(HttpServletResponse.SC_FORBIDDEN);
                } catch (IOException ex) {
                    fireErrorEvent(ex, httpContext);
                }
            }
        } else if (method.equals("POST")) {
            if (httpContext.getRequest().isAsyncSupported()) {
                asyncHandle(httpContext, methods);
            } else {
                syncHandle(httpContext, methods);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void asyncHandle(final HttpContext httpContext, final ZibraHttpMethods methods) {
        final AsyncContext async = httpContext.getRequest().startAsync();
        async.setTimeout(asyncTimeout);
        async.addListener(new AsyncListener() {
            public void onComplete(AsyncEvent ae) throws IOException {
            }

            public void onTimeout(AsyncEvent ae) throws IOException {
                ((HttpServletResponse) ae.getSuppliedResponse()).sendError(HttpServletResponse.SC_REQUEST_TIMEOUT);
            }

            public void onError(AsyncEvent ae) throws IOException {
            }

            public void onStartAsync(AsyncEvent ae) throws IOException {
            }
        });
        async.start(() -> {
            final ByteBufferStream istream = new ByteBufferStream();
            try {
                istream.readFrom(async.getRequest().getInputStream());
            } catch (Throwable e) {
                fireErrorEvent(e, httpContext);
                istream.close();
                async.complete();
                return;
            }
            currentContext.set(httpContext);
            handle(istream.buffer, methods, httpContext).then(value -> {
                try {
                    ByteBufferStream ostream = new ByteBufferStream(value);
                    async.getResponse().setContentLength(ostream.available());
                    ostream.writeTo(async.getResponse().getOutputStream());
                } finally {
                    ByteBufferStream.free(value);
                }
            }).catchError(e -> {
                fireErrorEvent(e, httpContext);
            }).whenComplete((Runnable) () -> {
                currentContext.remove();
                istream.close();
                async.complete();
            });
        });
    }

    @SuppressWarnings("unchecked")
    private void syncHandle(final HttpContext httpContext, ZibraHttpMethods methods) {
        final ByteBufferStream istream = new ByteBufferStream();
        try {
            istream.readFrom(httpContext.getRequest().getInputStream());
        } catch (Throwable e) {
            fireErrorEvent(e, httpContext);
            istream.close();
            return;
        }
        currentContext.set(httpContext);
        handle(istream.buffer, methods, httpContext).then(value -> {
            try {
                ByteBufferStream ostream = new ByteBufferStream(value);
                httpContext.getResponse().setContentLength(ostream.available());
                ostream.writeTo(httpContext.getResponse().getOutputStream());
            } finally {
                ByteBufferStream.free(value);
            }
        }).catchError(e -> {
            fireErrorEvent(e, httpContext);
        }).whenComplete((Runnable) () -> {
            currentContext.remove();
            istream.close();
        });
    }
}
