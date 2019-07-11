package org.spin.core.util.http;

import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;
import org.spin.core.throwable.SimplifiedException;
import org.spin.core.util.StringUtils;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

/**
 * 同步Http客户端持有者
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/7/11</p>
 *
 * @author xuweinan
 * @version 1.0
 */
class HttpExecutorSyncHolder {
    private static CloseableHttpClient httpClient;

    static CloseableHttpClient getClient() {
        return httpClient;
    }

    static void initSync(int maxTotal, int maxPerRoute, HttpRequestRetryHandler defaultHttpRetryHandler, byte[] certificate, String password, String algorithm) {
        SSLConnectionSocketFactory sslConnectionSocketFactory = null;
        if (null != certificate && StringUtils.isNotEmpty(algorithm)) {
            try (InputStream certInput = new ByteArrayInputStream(certificate)) {
                SSLContext sslContext = buildSSLContext(certInput, password, algorithm);
                sslConnectionSocketFactory = new SSLConnectionSocketFactory(
                    sslContext.getSocketFactory(),
                    SSLConnectionSocketFactory.getDefaultHostnameVerifier());
            } catch (Exception e) {
                throw new SimplifiedException(e);
            }
        }
        PoolingHttpClientConnectionManager connectionManager = null == sslConnectionSocketFactory ? new PoolingHttpClientConnectionManager() : new PoolingHttpClientConnectionManager(
            RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .register("https", sslConnectionSocketFactory)
                .build());
        connectionManager.setMaxTotal(maxTotal);
        connectionManager.setDefaultMaxPerRoute(maxPerRoute);
        httpClient = HttpClients.custom().setRetryHandler(defaultHttpRetryHandler).setConnectionManager(connectionManager)
            .build();
    }

    static SSLContext buildSSLContext(InputStream certsInput, String password, String algorithm) throws NoSuchAlgorithmException, KeyStoreException, IOException, CertificateException, UnrecoverableKeyException, KeyManagementException {
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
        KeyStore keyStore = KeyStore.getInstance(algorithm);
        keyStore.load(certsInput, StringUtils.trimToEmpty(password).toCharArray());

        keyManagerFactory.init(keyStore, StringUtils.trimToEmpty(password).toCharArray());

        SSLContext sslContext = SSLContexts.custom()
            .setProtocol("TLS")
            .loadTrustMaterial((chain, authType) -> true)
            .build();
        sslContext.init(keyManagerFactory.getKeyManagers(), null, new SecureRandom());
        return sslContext;
    }
}
