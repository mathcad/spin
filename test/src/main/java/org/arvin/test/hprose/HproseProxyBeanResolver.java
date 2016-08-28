/**********************************************************\
|                                                          |
|                          hprose                          |
|                                                          |
| Official WebSite: http://www.hprose.com/                 |
|                   http://www.hprose.org/                 |
|                                                          |
\**********************************************************/
/**********************************************************\
 *                                                        *
 * HproseProxyBeanResolver.java                            *
 *                                                        *
 * HproseProxyBeanResolver for Java Spring Framework.      *
 *                                                        *
 * LastModified: Mar 13, 2016                             *
 * Author: Ma Bingyao <andot@hprose.com>                  *
 *                                                        *
\**********************************************************/
package org.arvin.test.hprose;

import hprose.client.HproseClient;
import hprose.client.HproseHttpClient;
import hprose.client.HproseTcpClient;
import hprose.common.FilterHandler;
import hprose.common.HproseFilter;
import hprose.common.InvokeHandler;
import hprose.io.HproseMode;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.remoting.support.UrlBasedRemoteAccessor;

public class HproseProxyBeanResolver extends UrlBasedRemoteAccessor implements BeanFactoryAware{
    private static final String ACCESS_TOKEN_HEADER_NAME = "ACCESS_TOKEN";
    private HproseClient client = null;
    private Exception exception = null;
    private boolean keepAlive = true;
    private int keepAliveTimeout = 300;
    private int timeout = 30000;
    private String proxyHost = null;
    private int proxyPort = 80;
    private String proxyUser = null;
    private String proxyPass = null;
    private HproseMode mode = HproseMode.MemberMode;
    private HproseFilter filter = null;
    private InvokeHandler invokeHandler = null;
    private FilterHandler beforeFilterHandler = null;
    private FilterHandler afterFilterHandler = null;
    private BeanFactory beanFactory;
    private String accessToken;

    @Override
    public void afterPropertiesSet() {
        super.afterPropertiesSet();
        try {
            client = HproseClient.create(getServiceUrl(), mode);
        }
        catch (Exception ex) {
            exception = ex;
        }
        if (client instanceof HproseHttpClient) {
            HproseHttpClient httpClient = (HproseHttpClient)client;
            httpClient.setHeader(ACCESS_TOKEN_HEADER_NAME, this.accessToken);
            httpClient.setKeepAlive(keepAlive);
            httpClient.setKeepAliveTimeout(keepAliveTimeout);
            httpClient.setTimeout(timeout);
            httpClient.setProxyHost(proxyHost);
            httpClient.setProxyPort(proxyPort);
            httpClient.setProxyUser(proxyUser);
            httpClient.setProxyPass(proxyPass);
            httpClient.use(invokeHandler);
            httpClient.beforeFilter.use(beforeFilterHandler);
            httpClient.afterFilter.use(afterFilterHandler);
        }
        if (client instanceof HproseTcpClient) {
            HproseTcpClient tcpClient = (HproseTcpClient)client;
            tcpClient.setTimeout(timeout);
        }
        client.setFilter(filter);
    }

// for HproseHttpClient
    public void setKeepAlive(boolean value) {
        keepAlive = value;
    }

    public void setKeepAliveTimeout(int value) {
        keepAliveTimeout = value;
    }

    public void setProxyHost(String value) {
        proxyHost = value;
    }

    public void setProxyPort(int value) {
        proxyPort = value;
    }

    public void setProxyUser(String value) {
        proxyUser = value;
    }

    public void setProxyPass(String value) {
        proxyPass = value;
    }

// for HproseClient
    public void setTimeout(int value) {
        timeout = value;
    }

    public void setMode(HproseMode value) {
        mode = value;
    }

    public void setFilter(HproseFilter filter) {
        this.filter = filter;
    }

    public void setInvokeHandler(InvokeHandler value) {
        invokeHandler = value;
    }

    public void setBeforeFilterHandler(FilterHandler value) {
        beforeFilterHandler = value;
    }

    public void setAfterFilterHandler(FilterHandler value) {
        afterFilterHandler = value;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    @SuppressWarnings("unchecked")
    public Object getObject() throws Exception {
        if (exception != null) {
            throw exception;
        }
        return client.useService(getServiceInterface());
    }

    public <T> T getServiceBean(Class<T> serviceInterface) throws Exception{
        if (exception != null)
            throw exception;

        return client.useService(serviceInterface);
    }

    public Class getObjectType() {
        return getServiceInterface();
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }
}