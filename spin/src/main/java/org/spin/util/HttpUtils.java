package org.spin.util;

import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.sys.ErrorCode;
import org.spin.throwable.SimplifiedException;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 利用Apache HttpClient完成请求
 *
 * @author xuweinan
 * @version V1.0
 */
public abstract class HttpUtils {
    private static final Logger logger = LoggerFactory.getLogger(HttpUtils.class);
    private static final String HTTP = "http://";

    /**
     * 使用get方式请求数据
     */
    public static String httpGetRequest(String url, Map<String, String> params) throws URISyntaxException {
        if (!url.startsWith("http"))
            url = HTTP + url;
        URIBuilder uriBuilder = new URIBuilder(url);
        if (params != null) {
            for (String key : params.keySet()) {
                uriBuilder.setParameter(key, params.get(key));
            }
        }
        return httpGetRequest(uriBuilder.build());
    }

    public static String httpGetRequest(String url, String... params) throws URISyntaxException {
        return httpGetRequest(getUriFromString(url, params));
    }

    public static String httpGetRequest(URI uri) {
        String result;
        CloseableHttpClient httpclient = null;
        try {
            httpclient = HttpClients.createDefault();
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(1000).setConnectTimeout(1000).build();
            HttpGet request = new HttpGet(uri);
            request.setConfig(requestConfig);
            CloseableHttpResponse response = httpclient.execute(request);
            int code = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();
            result = EntityUtils.toString(entity, getContentCharSet(entity));
            if (code != 200) {
                throw new SimplifiedException(ErrorCode.NETWORK_EXCEPTION, "错误状态码:" + code + result);
            }
        } catch (Exception e) {
            throw new SimplifiedException(ErrorCode.NETWORK_EXCEPTION, "远程连接到" + uri.toString() +
                "，发生错误:" + e.getMessage());
        } finally {
            if (httpclient != null)
                try {
                    httpclient.close();
                } catch (IOException e) {
                    logger.error("Can not close current HttpClient:[{}]", uri.toString(), e);
                }
        }
        return result;
    }

    public static String httpPostRequest(String url, Map<String, String> params) throws URISyntaxException {
        if (!url.startsWith("http"))
            url = HTTP + url;
        List<NameValuePair> nvps = params.entrySet().stream().map(p -> new BasicNameValuePair(p.getKey(), p.getValue
            ())).collect(Collectors.toList());
        String result;
        CloseableHttpClient httpclient = null;
        try {
            httpclient = HttpClients.createDefault();
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(1000).setConnectTimeout(1000).build();
            HttpPost request = new HttpPost(url);
            request.setConfig(requestConfig);
            request.setEntity(new UrlEncodedFormEntity(nvps));
            CloseableHttpResponse response = httpclient.execute(request);
            int code = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();
            result = EntityUtils.toString(entity, getContentCharSet(entity));
            if (code != 200) {
                throw new SimplifiedException(ErrorCode.NETWORK_EXCEPTION, "错误状态码:" + code + result);
            }
        } catch (Exception e) {
            throw new SimplifiedException(ErrorCode.NETWORK_EXCEPTION, "远程连接到" + url + "，发生错误:" + e
                .getMessage());
        } finally {
            if (httpclient != null)
                try {
                    httpclient.close();
                } catch (IOException e) {
                    logger.error("Can not close current HttpClient:[{}]", url, e);
                }
        }
        return result;
    }

    public static Map<String, String> download(String url, String savePath) throws IOException {
        Map<String, String> rs = new HashMap<>();
        URI uri;
        try {
            uri = getUriFromString(url);
        } catch (URISyntaxException e) {
            throw new SimplifiedException(ErrorCode.NETWORK_EXCEPTION, "无法打开指定的URL连接", e);
        }
        CloseableHttpClient httpclient;
        httpclient = HttpClients.createDefault();
        RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(1000).setConnectTimeout(1000).build();
        HttpGet request = new HttpGet(uri);
        request.setConfig(requestConfig);
        CloseableHttpResponse response = httpclient.execute(request);
        int code = response.getStatusLine().getStatusCode();
        if (code != 200) {
            throw new SimplifiedException(ErrorCode.NETWORK_EXCEPTION, "错误状态码:" + code);
        }
        HttpEntity entity = response.getEntity();
        String saveFile = savePath;
        String contentType = entity.getContentType().getValue();
        String extention = contentType.substring(contentType.indexOf('/') + 1, contentType.length());
        if (StringUtils.isNotBlank(savePath))
            saveFile = savePath + "." + extention;
        byte[] bytes = EntityUtils.toByteArray(entity);
        try (FileOutputStream fos = new FileOutputStream(saveFile)) {
            fos.write(bytes);
        } catch (FileNotFoundException e) {
            throw new SimplifiedException("无法保存文件:[" + saveFile + "]", e);
        }
        rs.put("extention", StringUtils.isBlank(extention) ? "" : "." + extention);
        rs.put("bytes", Integer.toString(bytes.length));
        return rs;
    }

    private static URI getUriFromString(String url, String... params) throws URISyntaxException {
        if (!url.startsWith("http"))
            url = HTTP + url;
        final StringBuilder u = new StringBuilder(url);
        Optional.ofNullable(params).ifPresent(p -> Arrays.stream(p).forEach(c -> {
            int b = u.indexOf("{}");
            if (b > 0)
                u.replace(b, b + 2, c);
        }));
        return new URI(u.toString());
    }

    private static String getContentCharSet(final HttpEntity entity) throws ParseException {
        if (entity == null) {
            throw new IllegalArgumentException("HTTP entity may not be null");
        }
        String charset = null;
        if (entity.getContentType() != null) {
            HeaderElement values[] = entity.getContentType().getElements();
            if (values.length > 0) {
                NameValuePair param = values[0].getParameterByName("charset");
                if (param != null) {
                    charset = param.getValue();
                }
            }
        }

        if (StringUtils.isEmpty(charset)) {
            charset = "UTF-8";
        }
        return charset;
    }
}
