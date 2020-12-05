package org.spin.core.util.http;

import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;
import org.spin.core.throwable.SpinException;
import org.spin.core.util.StringUtils;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.KeyStoreBuilderParameters;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.util.Map;

/**
 * 同步Http客户端持有者
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/7/11</p>
 *
 * @author xuweinan
 * @version 1.0
 */
class HttpExecutorSyncHolder {
    private static volatile CloseableHttpClient httpClient;

    static void initSync(int maxTotal, int maxPerRoute, HttpRequestRetryHandler defaultHttpRetryHandler,
                         byte[] keyStore, String keyStorePass, KeyStoreType keyStoreType,
                         Map<String, KeyStore.ProtectionParameter> keysPass,
                         byte[] trustStore, String trustStorePass, KeyStoreType trustStoreType,
                         HostnameVerifier hostnameVerifier) {
        SSLConnectionSocketFactory sslConnectionSocketFactory = null;
        if (null != keyStore && null != keyStoreType || null != trustStore && null != trustStoreType) {
            try {
                SSLContext sslContext = buildSSLContext(keyStore, keyStorePass, keyStoreType, keysPass,
                    trustStore, trustStorePass, trustStoreType);
                sslConnectionSocketFactory = new SSLConnectionSocketFactory(
                    sslContext.getSocketFactory(), new String[]{"TLSv1.2", "TLSv1.1", "TLSv1"}, null,
                    null == hostnameVerifier ? SSLConnectionSocketFactory.getDefaultHostnameVerifier() : hostnameVerifier);
            } catch (Exception e) {
                throw new SpinException("构建SSL安全上下文失败", e);
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

    static CloseableHttpResponse execute(HttpUriRequest request) throws IOException {
        return httpClient.execute(request);
    }

    static SSLContext buildSSLContext(byte[] keyStoreContent, String keyStorePass, KeyStoreType keyStoreType,
                                      Map<String, KeyStore.ProtectionParameter> keysPass,
                                      byte[] trustStoreContent, String trustStorePass, KeyStoreType trustStoreType) throws NoSuchAlgorithmException,
        KeyStoreException, IOException, CertificateException, KeyManagementException, InvalidAlgorithmParameterException {
        KeyManagerFactory keyManagerFactory = null;
        if (null != keyStoreContent && null != keyStoreType) {
            keyManagerFactory = KeyManagerFactory.getInstance("NewSunX509");
            KeyStore keyStore = KeyStore.getInstance(keyStoreType.getValue());

            try (InputStream keystoreInput = new ByteArrayInputStream(keyStoreContent)) {
                keyStore.load(keystoreInput, StringUtils.trimToEmpty(keyStorePass).toCharArray());
            }

            keyManagerFactory.init(new KeyStoreBuilderParameters(new KeyStoreBuilder(keyStore, keysPass)));
        }

        TrustManagerFactory trustManagerFactory = null;
        if (null != trustStoreContent && null != trustStoreType) {
            trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            KeyStore trustStore = KeyStore.getInstance(trustStoreType.getValue());

            try (InputStream truststoreInput = new ByteArrayInputStream(trustStoreContent)) {
                trustStore.load(truststoreInput, StringUtils.trimToEmpty(trustStorePass).toCharArray());
            }
            trustManagerFactory.init(trustStore);
        }

        SSLContext sslContext = SSLContexts.custom()
            .setProtocol("TLS")
            .loadTrustMaterial(new TrustAllStrategy())
            .build();
        sslContext.init(null == keyManagerFactory ? null : keyManagerFactory.getKeyManagers(), null == trustManagerFactory ? null : trustManagerFactory.getTrustManagers(), new SecureRandom());
        return sslContext;
    }
}
