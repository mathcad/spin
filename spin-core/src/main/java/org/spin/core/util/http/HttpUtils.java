package org.spin.core.util.http;

import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.Assert;
import org.spin.core.ErrorCode;
import org.spin.core.function.FinalConsumer;
import org.spin.core.function.Handler;
import org.spin.core.throwable.SimplifiedException;
import org.spin.core.util.JsonUtils;
import org.spin.core.util.StringUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * 利用Apache HttpClient完成请求
 * <p>Created by xuweinan on 2018/4/9.</p>
 *
 * @author xuweinan
 * @version V1.1
 */
public abstract class HttpUtils {
    private static final Logger logger = LoggerFactory.getLogger(HttpUtils.class);
    private static final String SCHEMA = "http://";
    private static final int SOCKET_TIMEOUT = 10000;
    private static final int CONNECT_TIMEOUT = 10000;

    /**
     * get请求
     *
     * @param url url
     * @return 请求结果
     */
    public static String get(String url) {
        return get(url, null, null);
    }

    /**
     * get请求
     *
     * @param url    url
     * @param params 请求参数
     * @return 请求结果
     */
    public static String get(String url, Map<String, String> params) {
        return get(url, null, params);
    }

    /**
     * get请求
     *
     * @param url     url
     * @param headers 请求头部
     * @param params  请求参数map
     * @return 请求结果
     */
    public static String get(String url, Map<String, String> headers, Map<String, String> params) {
        HttpGet request = HttpMethod.GET.buildRequest(url, ub -> {
            if (params != null) {
                for (Map.Entry<String, String> e : params.entrySet()) {
                    ub.setParameter(e.getKey(), e.getValue());
                }
            }
        }, req -> {
            if (null != headers) {
                headers.forEach(req::setHeader);
            }
        });

        return excuteRequest(request, HttpUtils::resolveEntityToStr);
    }

    /**
     * post请求
     *
     * @param url    url
     * @param params 请求参数
     * @return 请求结果
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
     * @return 请求结果
     */
    public static String post(String url, Map<String, String> headers, Map<String, String> params) {
        HttpPost request = HttpMethod.POST.buildRequest(url, req -> {
            if (null != headers) {
                headers.forEach(req::setHeader);
            }
            if (null != params) {
                List<NameValuePair> nvps = params.entrySet().stream()
                    .map(p -> new BasicNameValuePair(p.getKey(), p.getValue()))
                    .collect(Collectors.toList());
                try {
                    req.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    throw new SimplifiedException(ErrorCode.NETWORK_EXCEPTION, "生成请求报文体错误", e);
                }
            }
        });

        return excuteRequest(request, HttpUtils::resolveEntityToStr);
    }

    /**
     * 使用post方式请求，传输json数据
     *
     * @param url     请求url
     * @param jsonObj 参数对象
     * @return 请求结果
     */
    public static String postJson(String url, Object jsonObj) {
        HttpPost request = HttpMethod.POST.buildRequest(url, req -> {
            if (null != jsonObj) {
                StringEntity stringEntity = null;
                try {
                    stringEntity = new StringEntity(JsonUtils.toJson(jsonObj));
                } catch (UnsupportedEncodingException e) {
                    throw new SimplifiedException(ErrorCode.NETWORK_EXCEPTION, "生成请求报文体错误", e);
                }
                stringEntity.setContentEncoding("UTF-8");
                stringEntity.setContentType("application/json");
                req.setEntity(stringEntity);
            }
        });

        return excuteRequest(request, HttpUtils::resolveEntityToStr);
    }

    /**
     * get方式下载文件
     *
     * @param url      url
     * @param savePath 保存路径
     * @return 下载结果
     */
    public static Map<String, String> download(String url, String savePath) {
        HttpGet request = HttpMethod.GET.buildRequest(url);

        return excuteRequest(request, httpEntity -> {
            Map<String, String> map = new HashMap<>();
            String saveFile = savePath;
            String contentType = httpEntity.getContentType().getValue();
            String extention = contentType.substring(contentType.indexOf('/') + 1);
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
     * @param request    请求
     * @param entityProc 请求结果处理器
     * @param <T>        处理后的返回类型
     * @return 处理后的请求结果
     */
    public static <T> T excuteRequest(HttpUriRequest request, EntityProcessor<T> entityProc) {
        return excuteRequest(request, null, entityProc);
    }

    /**
     * 执行自定义请求，并通过自定义方式转换请求结果
     *
     * @param request    请求对象，可以通过Method枚举构造
     * @param configProc 请求配置
     * @param entityProc 请求结果处理器
     * @param <T>        处理后的返回类型
     * @return 处理后的请求结果
     */
    public static <T> T excuteRequest(HttpUriRequest request, FinalConsumer<RequestConfig.Builder> configProc, EntityProcessor<T> entityProc) {
        RequestConfig.Builder builder = RequestConfig.custom()
            .setSocketTimeout(SOCKET_TIMEOUT)
            .setConnectTimeout(CONNECT_TIMEOUT);
        if (null != configProc) {
            configProc.accept(builder);
        }

        RequestConfig requestConfig = builder.build();

        if (request instanceof HttpRequestBase && null == ((HttpRequestBase) request).getConfig()) {
            ((HttpRequestBase) request).setConfig(requestConfig);
        }

        HttpEntity entity = null;
        T res = null;
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            CloseableHttpResponse response = httpclient.execute(request);
            int code = response.getStatusLine().getStatusCode();
            entity = response.getEntity();
            response.close();
            if (code != 200) {
                throw new SimplifiedException(ErrorCode.NETWORK_EXCEPTION, "错误状态码:" + code);
            }
            res = Assert.notNull(entityProc, "请求结果处理器不能为空").process(entity);
        } catch (Exception e) {
            logger.error("远程连接到" + request.getURI() + "，发生错误:", e);
            throw new SimplifiedException(ErrorCode.NETWORK_EXCEPTION, "远程连接到"
                + request.getURI()
                + "，发生错误: "
                + e.getMessage());
        }
        return res;
    }

    /**
     * get请求
     *
     * @param url url
     * @return 请求结果
     */
    public static Future<HttpResponse> getAsync(String url, FinalConsumer<String> completedCallback,
                                                FinalConsumer<Exception> failedCallback) {
        return getAsync(url, null, null, completedCallback, failedCallback);
    }

    /**
     * get请求
     *
     * @param url    url
     * @param params 请求参数
     * @return 请求结果
     */
    public static Future<HttpResponse> getAsync(String url, Map<String, String> params, FinalConsumer<String> completedCallback,
                                                FinalConsumer<Exception> failedCallback) {
        return getAsync(url, null, params, completedCallback, failedCallback);
    }

    /**
     * get请求
     *
     * @param url     url
     * @param headers 请求头部
     * @param params  请求参数map
     * @return 请求结果
     */
    public static Future<HttpResponse> getAsync(String url, Map<String, String> headers, Map<String, String> params, FinalConsumer<String> completedCallback,
                                                FinalConsumer<Exception> failedCallback) {
        HttpGet request = HttpMethod.GET.buildRequest(url, ub -> {
            if (params != null) {
                for (Map.Entry<String, String> e : params.entrySet()) {
                    ub.setParameter(e.getKey(), e.getValue());
                }
            }
        }, req -> {
            if (null != headers) {
                headers.forEach(req::setHeader);
            }
        });

        return excuteRequestAsync(request, HttpUtils::resolveEntityToStr, completedCallback, failedCallback, null);
    }

    /**
     * post请求
     *
     * @param url    url
     * @param params 请求参数
     * @return 请求结果
     */
    public static Future<HttpResponse> postAsync(String url, Map<String, String> params, FinalConsumer<String> completedCallback,
                                                 FinalConsumer<Exception> failedCallback) {
        return postAsync(url, null, params, completedCallback, failedCallback);
    }

    /**
     * post请求
     *
     * @param url     url
     * @param headers 请求头部
     * @param params  请求参数
     * @return 请求结果
     */
    public static Future<HttpResponse> postAsync(String url, Map<String, String> headers, Map<String, String> params, FinalConsumer<String> completedCallback,
                                                 FinalConsumer<Exception> failedCallback) {
        HttpPost request = HttpMethod.POST.buildRequest(url, req -> {
            if (null != headers) {
                headers.forEach(req::setHeader);
            }
            if (null != params) {
                List<NameValuePair> nvps = params.entrySet().stream()
                    .map(p -> new BasicNameValuePair(p.getKey(), p.getValue()))
                    .collect(Collectors.toList());
                try {
                    req.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    throw new SimplifiedException(ErrorCode.NETWORK_EXCEPTION, "生成请求报文体错误", e);
                }
            }
        });

        return excuteRequestAsync(request, HttpUtils::resolveEntityToStr, completedCallback, failedCallback, null);
    }

    /**
     * 使用post方式请求，传输json数据
     *
     * @param url     请求url
     * @param jsonObj 参数对象
     * @return 请求结果
     */
    public static Future<HttpResponse> postJsonAsync(String url, Object jsonObj, FinalConsumer<String> completedCallback,
                                                     FinalConsumer<Exception> failedCallback) {
        HttpPost request = HttpMethod.POST.buildRequest(url, req -> {
            if (null != jsonObj) {
                StringEntity stringEntity = null;
                try {
                    stringEntity = new StringEntity(JsonUtils.toJson(jsonObj));
                } catch (UnsupportedEncodingException e) {
                    throw new SimplifiedException(ErrorCode.NETWORK_EXCEPTION, "生成请求报文体错误", e);
                }
                stringEntity.setContentEncoding("UTF-8");
                stringEntity.setContentType("application/json");
                req.setEntity(stringEntity);
            }
        });

        return excuteRequestAsync(request, HttpUtils::resolveEntityToStr, completedCallback, failedCallback, null);
    }

    /**
     * get方式下载文件
     *
     * @param url      url
     * @param savePath 保存路径
     * @return 下载结果
     */
    public static Future<HttpResponse> downloadAsync(String url, String savePath, FinalConsumer<Map<String, String>> completedCallback,
                                                     FinalConsumer<Exception> failedCallback) {
        HttpGet request = HttpMethod.GET.buildRequest(url);

        return excuteRequestAsync(request, httpEntity -> {
            Map<String, String> map = new HashMap<>();
            String saveFile = savePath;
            String contentType = httpEntity.getContentType().getValue();
            String extention = contentType.substring(contentType.indexOf('/') + 1);
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
        }, completedCallback, failedCallback, null);
    }

    /**
     * 异步执行自定义请求，并通过自定义方式转换请求结果
     *
     * @param request           请求对象，可以通过Method枚举构造
     * @param entityProc        请求结果处理器
     * @param completedCallback 请求成功时的回调
     * @param failedCallback    请求失败时的回调
     * @param cancelledCallback 请求取消后的回调
     * @param <T>               处理后的返回类型
     * @return 包含请求结果的Future对象
     */
    public static <T> Future<HttpResponse> excuteRequestAsync(HttpUriRequest request, EntityProcessor<T> entityProc,
                                                              FinalConsumer<T> completedCallback,
                                                              FinalConsumer<Exception> failedCallback,
                                                              Handler cancelledCallback) {
        return excuteRequestAsync(request, null, entityProc, completedCallback, failedCallback, cancelledCallback);
    }

    /**
     * 异步执行自定义请求，并通过自定义方式转换请求结果
     *
     * @param request           请求对象，可以通过Method枚举构造
     * @param configProc        请求配置
     * @param entityProc        请求结果处理器
     * @param completedCallback 请求成功时的回调
     * @param failedCallback    请求失败时的回调
     * @param cancelledCallback 请求取消后的回调
     * @param <T>               处理后的返回类型
     * @return 包含请求结果的Future对象
     */
    public static <T> Future<HttpResponse> excuteRequestAsync(HttpUriRequest request, FinalConsumer<RequestConfig.Builder> configProc, EntityProcessor<T> entityProc,
                                                              FinalConsumer<T> completedCallback,
                                                              FinalConsumer<Exception> failedCallback,
                                                              Handler cancelledCallback) {
        RequestConfig.Builder builder = RequestConfig.custom()
            .setSocketTimeout(SOCKET_TIMEOUT)
            .setConnectTimeout(CONNECT_TIMEOUT);
        if (null != configProc) {
            configProc.accept(builder);
        }

        RequestConfig requestConfig = builder.build();

        if (request instanceof HttpRequestBase && null == ((HttpRequestBase) request).getConfig()) {
            ((HttpRequestBase) request).setConfig(requestConfig);
        }

        try (CloseableHttpAsyncClient asyncClient = HttpAsyncClients.createDefault()) {
            asyncClient.start();
            return asyncClient.execute(request, new FutureCallback<HttpResponse>() {
                @Override
                public void completed(HttpResponse result) {
                    int code = result.getStatusLine().getStatusCode();
                    HttpEntity entity = result.getEntity();
                    if (code != 200) {
                        failed(new SimplifiedException(ErrorCode.NETWORK_EXCEPTION, "错误状态码:" + code));
                    }
                    if (null != completedCallback) {
                        try {
                            T res = entityProc.process(entity);
                            completedCallback.accept(res);
                        } catch (Exception e) {
                            failedCallback.accept(e);
                        }
                    } else {
                        logger.info("请求[{}]执行成功:\n{}", request.getURI(), entity);
                    }
                }

                @Override
                public void failed(Exception ex) {
                    if (null != failedCallback) {
                        failedCallback.accept(ex);
                    } else {
                        logger.error(String.format("请求[%s]执行成功", request.getURI()), ex);
                    }
                }

                @Override
                public void cancelled() {
                    if (null != failedCallback) {
                        cancelledCallback.handle();
                    } else {
                        logger.error("请求[{}]被取消", request.getURI());
                    }
                }
            });
        } catch (Exception e) {
            logger.error("远程连接到" + request.getURI() + "，发生错误:", e);
            throw new SimplifiedException(ErrorCode.NETWORK_EXCEPTION, "远程连接到"
                + request.getURI()
                + "，发生错误: "
                + e.getMessage());
        }
    }

    public static String fixUrl(String url) {
        return url.toLowerCase().startsWith("http") ? url : SCHEMA + url;
    }

    private static String resolveEntityToStr(HttpEntity entity) {
        try {
            return EntityUtils.toString(entity, getContentCharSet(entity));
        } catch (IOException e) {
            throw new SimplifiedException(ErrorCode.NETWORK_EXCEPTION, "转换请求结果发生错误", e);
        }
    }

    private static String getContentCharSet(final HttpEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("schema entity may not be null");
        }
        String charset = null;
        if (entity.getContentType() != null) {
            HeaderElement[] values = entity.getContentType().getElements();
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
