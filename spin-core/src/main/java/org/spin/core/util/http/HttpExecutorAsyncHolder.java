package org.spin.core.util.http;

import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.conn.NoopIOSessionStrategy;
import org.apache.http.nio.conn.ssl.SSLIOSessionStrategy;
import org.spin.core.throwable.SpinException;

import javax.net.ssl.SSLContext;
import java.security.KeyStore;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * 异步Http客户端持有者
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/7/11</p>
 *
 * @author xuweinan
 * @version 1.0
 */
class HttpExecutorAsyncHolder {
    private static final ThreadFactory THREAD_FACTORY = Executors.defaultThreadFactory();
    private static volatile CloseableHttpAsyncClient httpAsyncClient;

    static CloseableHttpAsyncClient getClient() {
        return httpAsyncClient;
    }

    static void initAsync(int maxTotal, int maxPerRoute,
                          byte[] keyStore, String keyStorePass, KeyStoreType keyStoreType,
                          Map<String, KeyStore.ProtectionParameter> keysPass,
                          byte[] trustStore, String trustStorePass, KeyStoreType trustStoreType) {
        if (null != keyStore && null != keyStoreType) {
            try {
                SSLContext sslContext = HttpExecutorSyncHolder.buildSSLContext(keyStore, keyStorePass, keyStoreType, keysPass,
                    trustStore, trustStorePass, trustStoreType);
                SSLIOSessionStrategy sslStrategy = new SSLIOSessionStrategy(sslContext, new DefaultHostnameVerifier());
                final org.apache.http.nio.reactor.ConnectingIOReactor ioreactor = new DefaultConnectingIOReactor(IOReactorConfig.DEFAULT, THREAD_FACTORY);
                final PoolingNHttpClientConnectionManager poolingmgr =
                    new PoolingNHttpClientConnectionManager(
                        ioreactor,
                        RegistryBuilder.<org.apache.http.nio.conn.SchemeIOSessionStrategy>create()
                            .register("http", NoopIOSessionStrategy.INSTANCE)
                            .register("https", sslStrategy)
                            .build());
                httpAsyncClient = HttpAsyncClients.custom().setConnectionManager(poolingmgr).setMaxConnTotal(maxTotal).setMaxConnPerRoute(maxPerRoute).build();
                httpAsyncClient.start();
                return;
            } catch (Exception e) {
                throw new SpinException("构建SSL安全上下文失败", e);
            }
        }

        httpAsyncClient = org.apache.http.impl.nio.client.HttpAsyncClients.custom().setMaxConnTotal(maxTotal).setMaxConnPerRoute(maxPerRoute).build();
        httpAsyncClient.start();
    }
}
