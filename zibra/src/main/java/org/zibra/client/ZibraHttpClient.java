package org.zibra.client;

import org.zibra.common.ZibraException;
import org.zibra.common.InvokeSettings;
import org.zibra.io.ByteBufferStream;
import org.zibra.io.ZibraMode;
import org.zibra.util.Base64;
import org.zibra.util.concurrent.Call;
import org.zibra.util.concurrent.Promise;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

public class ZibraHttpClient extends ZibraClient {
    private final ConcurrentHashMap<String, String> headers = new ConcurrentHashMap<>();
    private static boolean disableGlobalCookie = false;
    private static CookieManager globalCookieManager = new CookieManager();
    private final CookieManager cookieManager = disableGlobalCookie ? new CookieManager() : globalCookieManager;
    private boolean keepAlive = true;
    private int keepAliveTimeout = 300;
    private String proxyHost = null;
    private int proxyPort = 80;
    private String proxyUser = null;
    private String proxyPass = null;
    private HostnameVerifier hv = null;
    private SSLSocketFactory sslsf = null;

    public static void setDisableGlobalCookie(boolean value) {
        disableGlobalCookie = value;
    }

    public static boolean isDisableGlobalCookie() {
        return disableGlobalCookie;
    }

    public ZibraHttpClient() {
        super();
    }

    public ZibraHttpClient(String uri) {
        super(uri);
    }

    public ZibraHttpClient(ZibraMode mode) {
        super(mode);
    }

    public ZibraHttpClient(String uri, ZibraMode mode) {
        super(uri, mode);
    }

    public ZibraHttpClient(String[] uris) {
        super(uris);
    }

    public ZibraHttpClient(String[] uris, ZibraMode mode) {
        super(uris, mode);
    }

    public static ZibraClient create(String uri, ZibraMode mode) throws IOException, URISyntaxException {
        String scheme = (new URI(uri)).getScheme().toLowerCase();
        if (!scheme.equals("http") && !scheme.equals("https")) {
            throw new ZibraException("This client doesn't support " + scheme + " scheme.");
        }
        return new ZibraHttpClient(uri, mode);
    }

    public static ZibraClient create(String[] uris, ZibraMode mode) throws IOException, URISyntaxException {
        for (String uri1 : uris) {
            String scheme = (new URI(uri1)).getScheme().toLowerCase();
            if (!scheme.equals("http") && !scheme.equals("https")) {
                throw new ZibraException("This client doesn't support " + scheme + " scheme.");
            }
        }
        return new ZibraHttpClient(uris, mode);
    }

    public void setHeader(String name, String value) {
        String nl = name.toLowerCase();
        if (!nl.equals("content-type") &&
            !nl.equals("content-length") &&
            !nl.equals("connection") &&
            !nl.equals("keep-alive") &&
            !nl.equals("host")) {
            if (value == null) {
                headers.remove(name);
            }
            else {
                headers.put(name, value);
            }
        }
    }

    public String getHeader(String name) {
        return headers.get(name);
    }

    public boolean isKeepAlive() {
        return keepAlive;
    }

    public void setKeepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
    }

    public int getKeepAliveTimeout() {
        return keepAliveTimeout;
    }

    public void setKeepAliveTimeout(int keepAliveTimeout) {
        this.keepAliveTimeout = keepAliveTimeout;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    public int getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
    }

    public String getProxyUser() {
        return proxyUser;
    }

    public void setProxyUser(String proxyUser) {
        this.proxyUser = proxyUser;
    }

    public String getProxyPass() {
        return proxyPass;
    }

    public void setProxyPass(String proxyPass) {
        this.proxyPass = proxyPass;
    }

    public HostnameVerifier getHostnameVerifier() {
        return hv;
    }

    public void setHostnameVerifier(HostnameVerifier hv) {
        this.hv = hv;
    }

    public SSLSocketFactory getSSLSocketFactory() {
        return sslsf;
    }

    public void setSSLSocketFactory(SSLSocketFactory sslsf) {
        this.sslsf = sslsf;
    }

    private ByteBuffer syncSendAndReceive(ByteBuffer request, int timeout) throws Throwable {
        URL url = new URL(uri);
        Properties prop = System.getProperties();
        prop.put("http.keepAlive", Boolean.toString(keepAlive));
        if (proxyHost != null) {
            prop.put("http.proxyHost", proxyHost);
            prop.put("http.proxyPort", Integer.toString(proxyPort));
        }
        else {
            prop.remove("http.proxyHost");
            prop.remove("http.proxyPort");
        }
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        if (url.getProtocol().equals("https")) {
            if (hv != null) ((HttpsURLConnection)conn).setHostnameVerifier(hv);
            if (sslsf != null) ((HttpsURLConnection)conn).setSSLSocketFactory(sslsf);
        }
        conn.setConnectTimeout(timeout);
        conn.setReadTimeout(timeout);
        conn.setRequestProperty("Cookie", cookieManager.getCookie(url.getHost(),
                                                                  url.getFile(),
                                                                  url.getProtocol().equals("https")));
        if (keepAlive) {
            conn.setRequestProperty("Connection", "keep-alive");
            conn.setRequestProperty("Keep-Alive", Integer.toString(keepAliveTimeout));
        }
        else {
            conn.setRequestProperty("Connection", "close");
        }
        if (proxyUser != null && proxyPass != null) {
            conn.setRequestProperty("Proxy-Authorization",
                "Basic " + Base64.encode((proxyUser + ":" + proxyPass).getBytes()));
        }
        for (Entry<String, String> entry : headers.entrySet()) {
            conn.setRequestProperty(entry.getKey(), entry.getValue());
        }
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setUseCaches(false);
        conn.setRequestProperty("Content-Type", "application/hprose");
        conn.setRequestProperty("Content-Length", Integer.toString(request.remaining()));
        OutputStream ostream = null;
        try {
            ostream = conn.getOutputStream();
            ByteBufferStream stream = new ByteBufferStream(request);
            stream.writeTo(ostream);
            ostream.flush();
        }
        finally {
            if (ostream != null) ostream.close();
        }
        List<String> cookieList = new ArrayList<>();
        int i = 1;
        String key;
        while((key=conn.getHeaderFieldKey(i)) != null) {
            if (key.equalsIgnoreCase("set-cookie") ||
                key.equalsIgnoreCase("set-cookie2")) {
                cookieList.add(conn.getHeaderField(i));
            }
            ++i;
        }
        cookieManager.setCookie(cookieList, url.getHost());
        InputStream istream = null;
        ByteBufferStream response = new ByteBufferStream();
        try {
            istream = conn.getInputStream();
            response.readFrom(istream);
            response.flip();
            return response.buffer;
        }
        catch (IOException e) {
            InputStream estream = null;
            try {
                estream = conn.getErrorStream();
                if (estream != null) {
                    response.readFrom(estream);
                    response.flip();
                    return response.buffer;
                }
                else {
                    throw e;
                }
            }
            finally {
                if (estream != null) estream.close();
            }
        }
        finally {
            if (istream != null) istream.close();
        }
    }

    @Override
    protected Promise<ByteBuffer> sendAndReceive(final ByteBuffer request, ClientContext context) {
        final InvokeSettings settings = context.getSettings();
        Call<ByteBuffer> call = () -> syncSendAndReceive(request, settings.getTimeout());
        if (settings.isAsync()) {
            return new Promise<>(call);
        }
        else {
            return Promise.sync(call);
        }
    }
}
