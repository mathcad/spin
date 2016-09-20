package org.infrastructure.util;

import org.apache.http.NameValuePair;
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
import org.infrastructure.sys.ErrorAndExceptionCode;
import org.infrastructure.throwable.SimplifiedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Iterator;
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
            for (Iterator<String> iterator = params.keySet().iterator(); iterator.hasNext(); ) {
                String key = iterator.next();
                uriBuilder.setParameter(key, params.get(key));
            }
        }
        return httpGetRequest(uriBuilder.build());
    }

    public static String httpGetRequest(String url, String... params) throws URISyntaxException {
        if (!url.startsWith("http"))
            url = HTTP + url;
        final StringBuilder u = new StringBuilder(url);
        Optional.ofNullable(params).ifPresent(p -> Arrays.stream(p).forEach(c -> {
            int b = u.indexOf("{}");
            if (b > 0)
                u.replace(b, b + 2, c);
        }));
        return httpGetRequest(new URI(u.toString()));
    }

    public static String httpGetRequest(URI uri) {
        String result = null;
        CloseableHttpClient httpclient = null;
        try {
            httpclient = HttpClients.createDefault();
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(1000).setConnectTimeout(1000).build();
            HttpGet request = new HttpGet(uri);
            request.setConfig(requestConfig);
            CloseableHttpResponse response = httpclient.execute(request);
            int code = response.getStatusLine().getStatusCode();
            result = EntityUtils.toString(response.getEntity());
            if (code != 200) {
                throw new SimplifiedException(ErrorAndExceptionCode.NETWORK_EXCEPTION, "错误状态码:" + code + result);
            }
        } catch (Exception e) {
            throw new SimplifiedException(ErrorAndExceptionCode.NETWORK_EXCEPTION, "远程连接到" + uri.toString() + "，发生错误:" + e.getMessage());
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
        List<NameValuePair> nvps = params.entrySet().stream().map(p -> new BasicNameValuePair(p.getKey(), p.getValue())).collect(Collectors.toList());
        String result = null;
        CloseableHttpClient httpclient = null;
        try {
            httpclient = HttpClients.createDefault();
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(1000).setConnectTimeout(1000).build();
            HttpPost request = new HttpPost(url);
            request.setConfig(requestConfig);
            request.setEntity(new UrlEncodedFormEntity(nvps));
            CloseableHttpResponse response = httpclient.execute(request);
            int code = response.getStatusLine().getStatusCode();
            result = EntityUtils.toString(response.getEntity());
            if (code != 200) {
                throw new SimplifiedException(ErrorAndExceptionCode.NETWORK_EXCEPTION, "错误状态码:" + code + result);
            }
        } catch (Exception e) {
            throw new SimplifiedException(ErrorAndExceptionCode.NETWORK_EXCEPTION, "远程连接到" + url + "，发生错误:" + e.getMessage());
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

    /**
     * 上传文件
     */
    public static String upload(File file, String postname, String url) {
/*
        if(url.startsWith("https")){
			Protocol https = new Protocol("https",new org.infrastructure.ssl.HTTPSSecureProtocolSocketFactory(), 443);
	        Protocol.registerProtocol("https", https);
		}
		
		PostMethod filePost = new PostMethod(url);
		HttpClient client = new HttpClient();

		try {
			// 通过以下方法可以模拟页面参数提交
			
			Part[] parts = { new FilePart(postname, file) };
			
			filePost.setRequestEntity(new MultipartRequestEntity(parts,	filePost.getParams()));
			
			client.getHttpConnectionManager().getParams()
					.setConnectionTimeout(5000);

			int status = client.executeMethod(filePost);
			if (status == HttpStatus.SC_OK) {
				logger.info("上传成功");
				return filePost.getResponseBodyAsString();
			} else {
				logger.error("上传失败");
				throw new BizException("错误码:" + status + "," + filePost.getResponseBodyAsString());
			}
		} catch (Exception ex) {
			logger.error("上传失败",ex);
			throw new BizException("上传失败" + ex.getMessage());
		} finally {
			if(url.startsWith("https")){
				Protocol.unregisterProtocol("https");
			}
			filePost.releaseConnection();
		}
		*/
        return "";
    }
}
