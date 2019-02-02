package org.spin.core.util.http;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.message.BasicNameValuePair;
import org.spin.core.Assert;
import org.spin.core.ErrorCode;
import org.spin.core.function.FinalConsumer;
import org.spin.core.function.Handler;
import org.spin.core.throwable.SimplifiedException;
import org.spin.core.util.CollectionUtils;
import org.spin.core.util.JsonUtils;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/1/31</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class Request<T extends HttpRequestBase> {

    public static final ContentType TEXT_PLAIN_UTF8 = ContentType.create(
        "text/plain", StandardCharsets.UTF_8);

    private T request;


    Request(T request) {
        Assert.notNull(request, "请求不能为空");
        this.request = request;
    }

    public Request<T> customRequest(FinalConsumer<T> requestProc) {
        if (null != requestProc) {
            requestProc.accept(request);
        }
        return this;
    }

    public Request<T> configRequest(FinalConsumer<RequestConfig.Builder> configProc) {
        RequestConfig.Builder builder = RequestConfig.custom()
            .setSocketTimeout(HttpUtils.getSocketTimeout())
            .setConnectTimeout(HttpUtils.getConnectTimeout());
        if (null != configProc) {
            configProc.accept(builder);
        }

        RequestConfig requestConfig = builder.build();
        request.setConfig(requestConfig);
        return this;
    }

    public Request<T> withHead(Map<String, String> headers) {
        if (null != headers) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                request.setHeader(entry.getKey(), entry.getValue());
            }
        }
        return this;
    }

    public Request<T> withForm(Map<String, String> formData) {
        if (!CollectionUtils.isEmpty(formData)) {
            if (request instanceof HttpEntityEnclosingRequestBase) {
                List<NameValuePair> nvps = formData.entrySet().stream()
                    .map(p -> new BasicNameValuePair(p.getKey(), p.getValue()))
                    .collect(Collectors.toList());
                try {
                    ((HttpEntityEnclosingRequestBase) request).setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    throw new SimplifiedException(ErrorCode.NETWORK_EXCEPTION, "生成请求报文体错误", e);
                }
            } else {
                throw new UnsupportedOperationException("当前请求不支持传递表单参数");
            }
        }
        return this;
    }

    public Request<T> withMultipartForm(Map<String, Object> formData) {
        if (!CollectionUtils.isEmpty(formData)) {
            if (request instanceof HttpEntityEnclosingRequestBase) {
                MultipartEntityBuilder reqEntity = MultipartEntityBuilder.create().setCharset(StandardCharsets.UTF_8);

                boolean hasFile = false;
                for (Map.Entry<String, Object> entry : formData.entrySet()) {
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    if (value instanceof CharSequence || value instanceof Number) {
                        StringBody formItem = new StringBody(value.toString(), TEXT_PLAIN_UTF8);
                        reqEntity.addPart(key, formItem);
                    } else if (value instanceof File) {
                        FileBody fileItem = new FileBody((File) value);
                        reqEntity.addPart(key, fileItem);
                        hasFile = true;
                    }
                }

                if (hasFile) {
                    reqEntity.setContentType(ContentType.MULTIPART_FORM_DATA);
                } else {
                    reqEntity.setContentType(ContentType.APPLICATION_FORM_URLENCODED);
                }

                ((HttpEntityEnclosingRequestBase) request).setEntity(reqEntity.build());
            } else {
                throw new UnsupportedOperationException("当前请求不支持传递表单参数");
            }
        }
        return this;
    }

    public Request<T> withJsonBody(Object jsonObj) {
        if (null != jsonObj) {
            if (request instanceof HttpEntityEnclosingRequestBase) {
                StringEntity stringEntity;
                stringEntity = new StringEntity(JsonUtils.toJson(jsonObj), StandardCharsets.UTF_8);
                stringEntity.setContentEncoding(StandardCharsets.UTF_8.toString());
                stringEntity.setContentType(ContentType.APPLICATION_JSON.getMimeType());
                ((HttpEntityEnclosingRequestBase) request).setEntity(stringEntity);
            } else {
                throw new UnsupportedOperationException("当前请求不支持传递表单参数");
            }
        }
        return this;
    }

    public Request<T> withXmlBody(Object jsonObj) {
        if (null != jsonObj) {
            if (request instanceof HttpEntityEnclosingRequestBase) {
                StringEntity stringEntity;
                stringEntity = new StringEntity(JsonUtils.toJson(jsonObj), StandardCharsets.UTF_8);
                stringEntity.setContentEncoding(StandardCharsets.UTF_8.toString());
                stringEntity.setContentType(ContentType.APPLICATION_XML.getMimeType());
                ((HttpEntityEnclosingRequestBase) request).setEntity(stringEntity);
            } else {
                throw new UnsupportedOperationException("当前请求不支持传递表单参数");
            }
        }
        return this;
    }

    public Request<T> withBody(String body, Charset charset, ContentType contentType) {
        if (null != body) {
            if (request instanceof HttpEntityEnclosingRequestBase) {
                StringEntity stringEntity;
                stringEntity = new StringEntity(body, charset);
                stringEntity.setContentEncoding(charset.toString());
                stringEntity.setContentType(contentType.getMimeType());
                ((HttpEntityEnclosingRequestBase) request).setEntity(stringEntity);
            } else {
                throw new UnsupportedOperationException("当前请求不支持传递" + contentType.getMimeType() + "参数");
            }
        }
        return this;
    }

    /**
     * 执行自定义请求，并返回响应字符串
     *
     * @return 请求结果
     */
    public String execute() {
        return HttpUtils.executeRequest(request, null, HttpUtils::resolveEntityToStr);
    }

    /**
     * 执行自定义请求，并通过自定义方式转换请求结果
     *
     * @param entityProc 请求结果处理器
     * @param <E>        处理后的返回类型
     * @return 处理后的请求结果
     */
    public <E> E execute(EntityProcessor<E> entityProc) {
        return HttpUtils.executeRequest(request, null, entityProc);
    }


    /**
     * 异步执行自定义请求，并通过自定义方式转换请求结果
     *
     * @param entityProc        请求结果处理器
     * @param completedCallback 请求成功时的回调
     * @param failedCallback    请求失败时的回调
     * @param cancelledCallback 请求取消后的回调
     * @param <E>               处理后的返回类型
     * @return 包含请求结果的Future对象
     */
    public <E> Future<HttpResponse> executeAsync(EntityProcessor<E> entityProc,
                                                 FinalConsumer<E> completedCallback,
                                                 FinalConsumer<Exception> failedCallback,
                                                 Handler cancelledCallback) {

        return HttpUtils.executeRequestAsync(request, null, entityProc, completedCallback, failedCallback, cancelledCallback);
    }

    /**
     * 异步执行自定义请求，并通过自定义方式转换请求结果
     *
     * @param entityProc        请求结果处理器
     * @param completedCallback 请求成功时的回调
     * @param failedCallback    请求失败时的回调
     * @param <E>               处理后的返回类型
     * @return 包含请求结果的Future对象
     */
    public <E> Future<HttpResponse> executeAsync(EntityProcessor<E> entityProc,
                                                 FinalConsumer<E> completedCallback,
                                                 FinalConsumer<Exception> failedCallback) {
        return HttpUtils.executeRequestAsync(request, null, entityProc, completedCallback, failedCallback, null);
    }

    /**
     * 异步执行自定义请求，并通过自定义方式转换请求结果
     *
     * @param completedCallback 请求成功时的回调
     * @param failedCallback    请求失败时的回调
     * @param cancelledCallback 请求取消后的回调
     * @return 包含请求结果的Future对象
     */
    public Future<HttpResponse> executeAsync(FinalConsumer<String> completedCallback,
                                             FinalConsumer<Exception> failedCallback,
                                             Handler cancelledCallback) {
        return HttpUtils.executeRequestAsync(request, null, HttpUtils::resolveEntityToStr, completedCallback, failedCallback, cancelledCallback);
    }

    /**
     * 异步执行自定义请求，并通过自定义方式转换请求结果
     *
     * @param completedCallback 请求成功时的回调
     * @param failedCallback    请求失败时的回调
     * @return 包含请求结果的Future对象
     */
    public Future<HttpResponse> executeAsync(FinalConsumer<String> completedCallback,
                                             FinalConsumer<Exception> failedCallback) {
        return HttpUtils.executeRequestAsync(request, null, HttpUtils::resolveEntityToStr, completedCallback, failedCallback, null);
    }

    public Map<String, String> download(String savePath) {
        return HttpUtils.executeRequest(request, null, httpEntity -> HttpUtils.downloadProc(httpEntity, savePath));
    }

    public Future<HttpResponse> downloadAsync(String savePath) {
        return HttpUtils.executeRequestAsync(request, null, httpEntity -> HttpUtils.downloadProc(httpEntity, savePath), null, null, null);
    }

    public Future<HttpResponse> downloadAsync(String savePath,
                                              FinalConsumer<Map<String, String>> completedCallback) {
        return HttpUtils.executeRequestAsync(request, null, httpEntity -> HttpUtils.downloadProc(httpEntity, savePath), completedCallback, null, null);
    }

    public Future<HttpResponse> downloadAsync(String savePath,
                                              FinalConsumer<Map<String, String>> completedCallback,
                                              FinalConsumer<Exception> failedCallback) {
        return HttpUtils.executeRequestAsync(request, null, httpEntity -> HttpUtils.downloadProc(httpEntity, savePath), completedCallback, failedCallback, null);
    }
}
