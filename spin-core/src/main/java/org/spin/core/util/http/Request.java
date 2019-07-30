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
import org.spin.core.throwable.SpinException;
import org.spin.core.util.CollectionUtils;
import org.spin.core.util.DateUtils;
import org.spin.core.util.JsonUtils;
import org.spin.core.util.StringUtils;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * Http请求的封装
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/1/31</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class Request<T extends HttpRequestBase> {

    public static final ContentType TEXT_PLAIN_UTF8 = ContentType.create(
        "text/plain", StandardCharsets.UTF_8);

    private final T request;
    private final RequestConfig.Builder configBuilder;

    private Map<String, String> formData = new HashMap<>();
    private Map<String, File> multiPartFormData = new HashMap<>();
    private volatile boolean formBuilt = true;

    Request(T request) {
        Assert.notNull(request, "请求不能为空");
        this.request = request;
        this.configBuilder = RequestConfig.custom()
            .setSocketTimeout(HttpExecutor.getSocketTimeout())
            .setConnectTimeout(HttpExecutor.getConnectTimeout());
    }

    // region config request

    /**
     * 自定义请求
     *
     * @param requestProc 请求配置逻辑
     * @return 当前请求本身
     */
    public Request<T> customRequest(FinalConsumer<T> requestProc) {
        if (null != requestProc) {
            requestProc.accept(request);
        }
        return this;
    }

    /**
     * 自定义请求
     *
     * @param configProc 请求配置逻辑
     * @return 当前请求本身
     */
    public Request<T> configRequest(FinalConsumer<RequestConfig.Builder> configProc) {
        if (null != configProc) {
            configProc.accept(this.configBuilder);
        }
        return this;
    }

    /**
     * 配置请求传输超时
     *
     * @param socketTimeout 请求传输超时
     * @return 当前请求本身
     */
    public Request<T> timeout(int socketTimeout) {
        configBuilder.setSocketTimeout(socketTimeout);
        return this;
    }

    /**
     * 配置请求连接超时
     *
     * @param connectTimeout 请求连接超时
     * @return 当前请求本身
     */
    public Request<T> connTimeout(int connectTimeout) {
        configBuilder.setConnectTimeout(connectTimeout);
        return this;
    }

    /**
     * 添加请求头部信息
     * <pre>
     *     再次设置相同的请求头，会覆盖之前的设置
     * </pre>
     *
     * @param headers 头部信息
     * @return 当前请求本身
     */
    public <E> Request<T> withHead(Map<String, E> headers) {
        if (null != headers) {
            for (Map.Entry<String, E> entry : headers.entrySet()) {
                request.setHeader(entry.getKey(), StringUtils.toString(entry.getValue()));
            }
        }
        return this;
    }

    /**
     * 添加请求头部信息
     * <pre>
     *     再次设置相同的请求头，会覆盖之前的设置
     * </pre>
     *
     * @param headers 头部信息
     * @return 当前请求本身
     */
    public Request<T> withHead(String... headers) {
        if (null != headers) {
            if (headers.length % 2 != 0) {
                throw new SpinException(ErrorCode.INVALID_PARAM, "键值对必须为偶数个");
            }
            for (int i = 0; i < headers.length; ) {
                String k = headers[i++];
                String v = headers[i++];
                if (StringUtils.isNotEmpty(k) && StringUtils.isNotEmpty(v)) {
                    request.setHeader(k, v);
                }
            }
        }
        return this;
    }

    /**
     * 添加请求form表单信息
     * <pre>
     *     1.再次设置相同的表单项，会覆盖之前的表单项
     *     2.表单项的值只允许字符串、数字，日期，与文件(会自动转为Multipart form)
     * </pre>
     *
     * @param formData form表单
     * @return 当前请求本身
     */
    public <E> Request<T> withForm(Map<String, E> formData) {
        if (!CollectionUtils.isEmpty(formData)) {
            for (Map.Entry<String, E> entry : formData.entrySet()) {
                String k = entry.getKey();
                Object v = entry.getValue();
                if (null == v) {
                    continue;
                }
                if (v instanceof CharSequence || v instanceof Number) {
                    this.formData.put(k, v.toString());
                } else if (v instanceof File) {
                    this.multiPartFormData.put(k, (File) v);
                } else if (v instanceof Date) {
                    this.formData.put(k, DateUtils.formatDateForSecond((Date) v));
                } else if (v instanceof TemporalAccessor) {
                    this.formData.put(k, DateUtils.formatDateForSecond((TemporalAccessor) v));
                } else {
                    throw new SpinException(ErrorCode.INVALID_PARAM, "不支持的参数类型: " + k);
                }
            }
            formBuilt = false;
        }
        return this;
    }

    /**
     * 添加请求form表单信息
     * <pre>
     *     再次设置相同的表单项，会覆盖之前的表单项
     * </pre>
     *
     * @param formData form表单
     * @return 当前请求本身
     */
    public Request<T> withForm(String... formData) {
        if (null != formData) {
            if (formData.length % 2 != 0) {
                throw new SpinException(ErrorCode.INVALID_PARAM, "键值对必须为偶数个");
            }
            formBuilt = false;
            for (int i = 0; i < formData.length; ) {
                String k = formData[i++];
                String v = formData[i++];
                if (StringUtils.isNotEmpty(k) && StringUtils.isNotEmpty(v)) {
                    this.formData.put(k, v);
                }
            }
        }
        return this;
    }

    /**
     * 添加请求multipart form表单信息
     * <pre>
     *     再次设置相同的表单项，会覆盖之前的表单项
     * </pre>
     *
     * @param paramName 表单项名称
     * @param param     表单项文件内容
     * @return 当前请求本身
     */
    public Request<T> withForm(String paramName, File param) {
        if (StringUtils.isNotEmpty(paramName) && null != param) {
            multiPartFormData.put(paramName, param);
            formBuilt = false;
        }
        return this;
    }

    /**
     * 设置JSON请求体
     * <pre>
     *     会取消之前设置的其他任何请求体(包括表单等)，并设置请求对应的ContentType
     * </pre>
     *
     * @param jsonObj 任意对象
     * @return 当前请求本身
     */
    public Request<T> withJsonBody(Object jsonObj) {
        if (null != jsonObj) {
            if (request instanceof HttpEntityEnclosingRequestBase) {
                StringEntity stringEntity;
                stringEntity = new StringEntity(JsonUtils.toJson(jsonObj), StandardCharsets.UTF_8);
                stringEntity.setContentEncoding(StandardCharsets.UTF_8.toString());
                stringEntity.setContentType(ContentType.APPLICATION_JSON.getMimeType());
                ((HttpEntityEnclosingRequestBase) request).setEntity(stringEntity);
            } else {
                throw new UnsupportedOperationException("当前请求不支持传递JSON请求体");
            }
        }
        return this;
    }

    /**
     * 设置XML请求体
     * <pre>
     *     会取消之前设置的其他任何请求体(包括表单等)，并设置请求对应的ContentType
     * </pre>
     *
     * @param xmlBody xml内容
     * @return 当前请求本身
     */
    public Request<T> withXmlBody(String xmlBody) {
        if (null != xmlBody) {
            if (request instanceof HttpEntityEnclosingRequestBase) {
                StringEntity stringEntity;
                stringEntity = new StringEntity(xmlBody, StandardCharsets.UTF_8);
                stringEntity.setContentEncoding(StandardCharsets.UTF_8.toString());
                stringEntity.setContentType(ContentType.APPLICATION_XML.getMimeType());
                ((HttpEntityEnclosingRequestBase) request).setEntity(stringEntity);
            } else {
                throw new UnsupportedOperationException("当前请求不支持传递XML请求体");
            }
        }
        return this;
    }

    /**
     * 设置请求体
     * <pre>
     *     会取消之前设置的其他任何请求体(包括表单等)，并设置请求对应的ContentType
     * </pre>
     *
     * @param body        请求体
     * @param charset     字符集
     * @param contentType content-type
     * @return 当前请求本身
     */
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

    // endregion

    // region execute

    /**
     * 执行自定义请求，并返回响应字符串
     *
     * @return 请求结果
     */
    public String execute() {
        buildForm();
        request.setConfig(configBuilder.build());
        return HttpExecutor.executeRequest(request, HttpExecutor::toStringProc);
    }

    /**
     * 执行自定义请求，并通过自定义方式转换请求结果
     *
     * @param entityProc 请求结果处理器
     * @param <E>        处理后的返回类型
     * @return 处理后的请求结果
     */
    public <E> E execute(EntityProcessor<E> entityProc) {
        buildForm();
        request.setConfig(configBuilder.build());
        return HttpExecutor.executeRequest(request, entityProc);
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
        buildForm();
        request.setConfig(configBuilder.build());
        return HttpExecutor.executeRequestAsync(request, entityProc, completedCallback, failedCallback, cancelledCallback);
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
        buildForm();
        request.setConfig(configBuilder.build());
        return HttpExecutor.executeRequestAsync(request, entityProc, completedCallback, failedCallback, null);
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
        buildForm();
        request.setConfig(configBuilder.build());
        return HttpExecutor.executeRequestAsync(request, HttpExecutor::toStringProc, completedCallback, failedCallback, cancelledCallback);
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
        buildForm();
        request.setConfig(configBuilder.build());
        return HttpExecutor.executeRequestAsync(request, HttpExecutor::toStringProc, completedCallback, failedCallback, null);
    }

    /**
     * 下载文件
     *
     * @param savePath 文件保存路径
     * @return 下载的文件信息(扩展名与大小)
     */
    public Map<String, String> download(String savePath) {
        buildForm();
        request.setConfig(configBuilder.build());
        return HttpExecutor.executeRequest(request, httpEntity -> HttpExecutor.downloadProc(httpEntity, savePath));
    }

    /**
     * 异步下载文件
     *
     * @param savePath 文件保存路径
     * @return 包含请求结果的Future对象
     */
    public Future<HttpResponse> downloadAsync(String savePath) {
        buildForm();
        request.setConfig(configBuilder.build());
        return HttpExecutor.executeRequestAsync(request, httpEntity -> HttpExecutor.downloadProc(httpEntity, savePath), null, null, null);
    }

    /**
     * 异步下载文件
     *
     * @param savePath          文件保存路径
     * @param completedCallback 文件下载完成后的回调
     * @return 包含请求结果的Future对象
     */
    public Future<HttpResponse> downloadAsync(String savePath,
                                              FinalConsumer<Map<String, String>> completedCallback) {
        buildForm();
        request.setConfig(configBuilder.build());
        return HttpExecutor.executeRequestAsync(request, httpEntity -> HttpExecutor.downloadProc(httpEntity, savePath), completedCallback, null, null);
    }

    /**
     * 异步下载文件
     *
     * @param savePath          文件保存路径
     * @param completedCallback 文件下载完成后的回调
     * @param failedCallback    文件下载失败后的回调
     * @return 包含请求结果的Future对象
     */
    public Future<HttpResponse> downloadAsync(String savePath,
                                              FinalConsumer<Map<String, String>> completedCallback,
                                              FinalConsumer<Exception> failedCallback) {
        buildForm();
        request.setConfig(configBuilder.build());
        return HttpExecutor.executeRequestAsync(request, httpEntity -> HttpExecutor.downloadProc(httpEntity, savePath), completedCallback, failedCallback, null);
    }

    // endregion

    /**
     * 根据用户指定的参数构造请求体的表单对象
     */
    private void buildForm() {
        if (formBuilt) {
            return;
        }
        if (multiPartFormData.isEmpty()) {
            if (!formData.isEmpty()) {
                if (request instanceof HttpEntityEnclosingRequestBase) {
                    List<NameValuePair> nvps = formData.entrySet().stream()
                        .map(p -> new BasicNameValuePair(p.getKey(), p.getValue()))
                        .collect(Collectors.toList());
                    ((HttpEntityEnclosingRequestBase) request).setEntity(new UrlEncodedFormEntity(nvps, StandardCharsets.UTF_8));
                } else {
                    throw new UnsupportedOperationException("当前请求不支持传递表单参数");
                }
            }
        } else {
            if (request instanceof HttpEntityEnclosingRequestBase) {
                MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create().setCharset(StandardCharsets.UTF_8);

                for (Map.Entry<String, String> entry : formData.entrySet()) {
                    StringBody formItem = new StringBody(entry.getValue(), TEXT_PLAIN_UTF8);
                    entityBuilder.addPart(entry.getKey(), formItem);
                }

                for (Map.Entry<String, File> fileEntry : multiPartFormData.entrySet()) {
                    FileBody fileItem = new FileBody(fileEntry.getValue());
                    entityBuilder.addPart(fileEntry.getKey(), fileItem);
                }

                entityBuilder.setContentType(ContentType.MULTIPART_FORM_DATA);
                ((HttpEntityEnclosingRequestBase) request).setEntity(entityBuilder.build());
            } else {
                throw new UnsupportedOperationException("当前请求不支持传递表单参数");
            }
        }
        formBuilt = true;
    }
}
