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
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 利用Apache HttpClient完成请求
 *
 * @author xuweinan
 * @version V1.0
 */
public abstract class HttpUtils {
    private static final Logger logger = LoggerFactory.getLogger(HttpUtils.class);
    private static final String schema = "http://";
    private static final int socketTimeout = 1000;
    private static final int connectTimeout = 10000;

    /**
     * get请求
     *
     * @param url     url
     * @param headers 请求头部
     * @param params  请求参数map
     */
    public static String get(String url, Map<String, String> headers, Map<String, String> params) {
        URIBuilder uriBuilder = null;
        try {
            uriBuilder = new URIBuilder(fixUrl(url));
            if (params != null) {
                for (String key : params.keySet()) {
                    uriBuilder.setParameter(key, params.get(key));
                }
            }
            return get(uriBuilder.build(), headers);
        } catch (URISyntaxException e) {
            logger.error("url格式错误: " + url, e);
            throw new SimplifiedException(ErrorCode.NETWORK_EXCEPTION, "url格式错误: " + url + e.getMessage());
        }
    }

    /**
     * get请求
     *
     * @param url     url
     * @param headers 请求头部
     * @param params  请求参数
     */
    public static String get(String url, Map<String, String> headers, String... params) {
        return get(getUriFromString(url, params), headers);
    }

    /**
     * get请求
     *
     * @param url    url
     * @param params 请求参数
     */
    public static String get(String url, String... params) {
        return get(getUriFromString(url, params), null);
    }

    /**
     * get请求
     *
     * @param uri     uri
     * @param headers 请求头部
     */
    public static String get(URI uri, Map<String, String> headers) {
        RequestConfig requestConfig = RequestConfig.custom()
            .setSocketTimeout(socketTimeout)
            .setConnectTimeout(connectTimeout)
            .build();

        HttpGet request = new HttpGet(uri);
        request.setConfig(requestConfig);
        if (null != headers) {
            headers.forEach(request::setHeader);
        }

        return excuteRequest(request, HttpUtils::resolveEntityToStr);
    }

    /**
     * post请求
     *
     * @param url    url
     * @param params 请求参数
     */
    public static String post(String url, Map<String, String> params) {
        return post(url, null, params);
    }

    /**
     * post请求
     *
     * @param url     url
     * @param headers 请求头部
     * @param params  请求参数
     */
    public static String post(String url, Map<String, String> headers, Map<String, String> params) {
        RequestConfig requestConfig = RequestConfig.custom()
            .setSocketTimeout(socketTimeout)
            .setConnectTimeout(connectTimeout)
            .build();

        List<NameValuePair> nvps = params.entrySet().stream()
            .map(p -> new BasicNameValuePair(p.getKey(), p.getValue()))
            .collect(Collectors.toList());

        HttpPost request = new HttpPost(fixUrl(url));
        if (null != headers) {
            headers.forEach(request::setHeader);
        }
        request.setConfig(requestConfig);
        try {
            request.setEntity(new UrlEncodedFormEntity(nvps));
        } catch (UnsupportedEncodingException e) {
            logger.error("生成请求报文体错误", e);
            throw new SimplifiedException(ErrorCode.NETWORK_EXCEPTION, "生成请求报文体错误");
        }

        return excuteRequest(request, HttpUtils::resolveEntityToStr);
    }

    /**
     * 使用post方式请求，传输json数据
     *
     * @param url     请求url
     * @param jsonObj json参数
     */
    public static String postJson(String url, Object jsonObj) {
        RequestConfig requestConfig = RequestConfig.custom()
            .setSocketTimeout(socketTimeout)
            .setConnectTimeout(connectTimeout)
            .build();

        StringEntity stringEntity = null;
        try {
            stringEntity = new StringEntity(JsonUtils.toJson(jsonObj));
        } catch (UnsupportedEncodingException e) {
            logger.error("生成请求报文体错误", e);
            throw new SimplifiedException(ErrorCode.NETWORK_EXCEPTION, "生成请求报文体错误");
        }
        stringEntity.setContentEncoding("UTF-8");
        stringEntity.setContentType("application/json");

        HttpPost request = new HttpPost(fixUrl(url));
        request.setConfig(requestConfig);
        request.setEntity(stringEntity);

        return excuteRequest(request, HttpUtils::resolveEntityToStr);
    }

    /**
     * get方式下载文件
     *
     * @param url      url
     * @param savePath 保存路径
     */
    public static Map<String, String> download(String url, String savePath) {
        URI uri = getUriFromString(url);
        RequestConfig requestConfig = RequestConfig.custom()
            .setSocketTimeout(socketTimeout)
            .setConnectTimeout(connectTimeout)
            .build();
        HttpGet request = new HttpGet(uri);
        request.setConfig(requestConfig);

        return excuteRequest(request, httpEntity -> {
            Map<String, String> map = new HashMap<>();
            String saveFile = savePath;
            String contentType = httpEntity.getContentType().getValue();
            String extention = contentType.substring(contentType.indexOf('/') + 1, contentType.length());
            if (StringUtils.isNotBlank(savePath))
                saveFile = savePath + "." + extention;
            try (FileOutputStream fos = new FileOutputStream(saveFile)) {
                byte[] bytes = EntityUtils.toByteArray(httpEntity);
                fos.write(bytes);
                map.put("extention", StringUtils.isBlank(extention) ? "" : "." + extention);
                map.put("bytes", Integer.toString(bytes.length));
            } catch (IOException e) {
                throw new SimplifiedException("无法保存文件:[" + saveFile + "]", e);
            }
            return map;
        });
    }

    /**
     * 执行自定义请求，并通过自定义方式转换请求结果
     *
     * @param request   请求
     * @param processor 请求结果处理器
     * @param <T>       处理后的返回类型
     */
    public static <T> T excuteRequest(HttpUriRequest request, EntityProcessor<T> processor) {
        HttpEntity entity = null;
        T res = null;
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
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
        }
        return res;
    }

    private static String fixUrl(String url) {
        return url.toLowerCase().startsWith("http") ? url : schema + url;
    }

    private static String resolveEntityToStr(HttpEntity entity) {
        try {
            return EntityUtils.toString(entity, getContentCharSet(entity));
        } catch (IOException e) {
            throw new SimplifiedException(ErrorCode.NETWORK_EXCEPTION, "转换请求结果发生错误", e);
        }
    }

    private static URI getUriFromString(String url, String... params) {
        return URI.create(StringUtils.plainFormat(url, params));
    }

    private static String getContentCharSet(final HttpEntity entity) throws ParseException {
        if (entity == null) {
            throw new IllegalArgumentException("schema entity may not be null");
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

    /**
     * 请求结果处理接口，处理http请求返回的HttpEntity结果
     *
     * @param <T> 处理后的数据类型
     */
    @FunctionalInterface
    public interface EntityProcessor<T> {
        T process(HttpEntity entity);
    }
}
