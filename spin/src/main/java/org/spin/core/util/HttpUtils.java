package org.spin.core.util;

import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.ErrorCode;
import org.spin.core.throwable.SimplifiedException;

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
    public static String httpGetRequest(String url, Map<String, String> headers, Map<String, String> params) throws URISyntaxException {
        if (!url.startsWith("http"))
            url = HTTP + url;
        URIBuilder uriBuilder = new URIBuilder(url);
        if (params != null) {
            for (String key : params.keySet()) {
                uriBuilder.setParameter(key, params.get(key));
            }
        }
        return httpGetRequest(uriBuilder.build(), headers);
    }

    public static String httpGetRequest(String url, Map<String, String> headers, String... params) throws URISyntaxException {
        return httpGetRequest(getUriFromString(url, params), headers);
    }

    public static String httpGetRequest(String url, String... params) throws URISyntaxException {
        return httpGetRequest(getUriFromString(url, params), null);
    }

    public static String httpGetRequestWithHead(String url, String headerKey, String headerVal) throws URISyntaxException {
        Map<String, String> header = new HashMap<>();
        header.put(headerKey, headerVal);
        return httpGetRequest(getUriFromString(url), header);
    }

    public static String httpGetRequest(URI uri, Map<String, String> headers) {
        String result;
        RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(1000).setConnectTimeout(1000).build();
        HttpGet request = new HttpGet(uri);
        request.setConfig(requestConfig);
        if (null != headers) {
            headers.forEach(request::setHeader);
        }
        try {
            result = excuteRequest(request, entity -> {
                try {
                    return EntityUtils.toString(entity, getContentCharSet(entity));
                } catch (IOException e) {
                    throw new SimplifiedException(ErrorCode.NETWORK_EXCEPTION, "转换请求结果发生错误", e);
                }
            });
        } catch (Exception e) {
            throw new SimplifiedException(ErrorCode.NETWORK_EXCEPTION, "远程连接到" + uri.toString() +
                "，发生错误:" + e.getMessage());
        }
        return result;
    }

    public static String httpPostRequest(String url, Map<String, String> params) {
        return httpPostRequest(url, null, params);
    }

    /**
     * 使用post方式请求，传输json数据
     *
     * @param url     请求url
     * @param jsonObj json参数
     * @return
     */
    public static String httpPostJsonRequest(String url, Object jsonObj) {
        if (!url.startsWith("http"))
            url = HTTP + url;
        String result;
        try {
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(1000).setConnectTimeout(1000).build();
            HttpPost request = new HttpPost(url);
            StringEntity stringEntity = new StringEntity(JsonUtils.toJson(jsonObj));
            stringEntity.setContentEncoding("UTF-8");
            stringEntity.setContentType("application/json");
            request.setConfig(requestConfig);
            request.setEntity(stringEntity);
            result = excuteRequest(request, entity -> {
                try {
                    return EntityUtils.toString(entity, getContentCharSet(entity));
                } catch (IOException e) {
                    throw new SimplifiedException(ErrorCode.NETWORK_EXCEPTION, "转换请求结果发生错误", e);
                }
            });
        } catch (Exception e) {
            logger.error("远程连接到" + url + "，发生错误:", e);
            throw new SimplifiedException(ErrorCode.NETWORK_EXCEPTION, "远程连接到" + url + "，发生错误:" + e
                .getMessage());
        }
        return result;
    }

    public static String httpPostRequest(String url, Map<String, String> headers, Map<String, String> params) {
        if (!url.startsWith("http"))
            url = HTTP + url;
        List<NameValuePair> nvps = params.entrySet().stream().map(p -> new BasicNameValuePair(p.getKey(), p.getValue
            ())).collect(Collectors.toList());
        String result;
        try {
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(1000).setConnectTimeout(1000).build();
            HttpPost request = new HttpPost(url);
            if (null != headers) {
                headers.forEach(request::setHeader);
            }
            request.setConfig(requestConfig);
            request.setEntity(new UrlEncodedFormEntity(nvps));
            result = excuteRequest(request, entity -> {
                try {
                    return EntityUtils.toString(entity, getContentCharSet(entity));
                } catch (IOException e) {
                    throw new SimplifiedException(ErrorCode.NETWORK_EXCEPTION, "转换请求结果发生错误", e);
                }
            });
        } catch (Exception e) {
            throw new SimplifiedException(ErrorCode.NETWORK_EXCEPTION, "远程连接到" + url + "，发生错误:" + e
                .getMessage());
        }
        return result;
    }

    public static Map<String, String> download(String url, String savePath) throws IOException {
        final Map<String, String> rs = new HashMap<>();
        URI uri;
        try {
            uri = getUriFromString(url);
        } catch (URISyntaxException e) {
            throw new SimplifiedException(ErrorCode.NETWORK_EXCEPTION, "无法打开指定的URL连接", e);
        }
        RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(1000).setConnectTimeout(1000).build();
        HttpGet request = new HttpGet(uri);
        request.setConfig(requestConfig);

        excuteRequest(request, httpEntity -> {
            String saveFile = savePath;
            String contentType = httpEntity.getContentType().getValue();
            String extention = contentType.substring(contentType.indexOf('/') + 1, contentType.length());
            if (StringUtils.isNotBlank(savePath))
                saveFile = savePath + "." + extention;
            try (FileOutputStream fos = new FileOutputStream(saveFile)) {
                byte[] bytes = EntityUtils.toByteArray(httpEntity);
                fos.write(bytes);
                rs.put("extention", StringUtils.isBlank(extention) ? "" : "." + extention);
                rs.put("bytes", Integer.toString(bytes.length));
            } catch (IOException e) {
                throw new SimplifiedException("无法保存文件:[" + saveFile + "]", e);
            }
            return rs;
        });
        return rs;
    }


    private static <T> T excuteRequest(HttpUriRequest request, EntityProcessor<T> processor) {
        CloseableHttpClient httpclient = null;
        HttpEntity entity = null;
        T res = null;
        try {
            httpclient = HttpClients.createDefault();
            CloseableHttpResponse response = httpclient.execute(request);
            int code = response.getStatusLine().getStatusCode();
            entity = response.getEntity();
            if (code != 200) {
                throw new SimplifiedException(ErrorCode.NETWORK_EXCEPTION, "错误状态码:" + code);
            }
            res = processor.process(entity);
        } catch (Exception e) {
            logger.error("远程连接到" + request.getURI() + "，发生错误:", e);
            throw new SimplifiedException(ErrorCode.NETWORK_EXCEPTION, "远程连接到"
                + request.getURI()
                + "，发生错误: "
                + e.getMessage());
        } finally {
            if (httpclient != null) {
                try {
                    httpclient.close();
                } catch (IOException e) {
                    logger.error("Can not close current HttpClient:[{}]", request.getURI(), e);
                }
            }
        }
        return res;
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

    @FunctionalInterface
    public interface EntityProcessor<T> {
        T process(HttpEntity entity);
    }
}
