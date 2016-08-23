package org.infrastructure.util;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * 利用Apache HttpClient完成请求
 *
 * @author xuweinan
 * @version V1.0
 */
public class HttpUtils {
    static final Logger logger = Logger.getLogger(HttpUtils.class);

    /**
     * 使用get方式请求数据
     */
    public static String httpGetRequest(String url, Map<String, String> params, Charset reqCharset) {
        String result = null;
        if (!url.startsWith("http"))
            url = "http://" + url;
        CloseableHttpClient httpclient = null;
        try {
            httpclient = HttpClients.createDefault();
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(1000).setConnectTimeout(1000).build();
            URIBuilder uriBuilder = new URIBuilder(url).setCharset(reqCharset);

            if (params != null) {
                for (String key : params.keySet()) {
                    uriBuilder.setParameter(key, params.get(key));
                }
            }

            HttpGet request = new HttpGet(uriBuilder.build());
            request.setConfig(requestConfig);

            CloseableHttpResponse response = httpclient.execute(request);
            int code = response.getStatusLine().getStatusCode();
            result = EntityUtils.toString(response.getEntity());
            if (code != 200) {
                throw new RuntimeException("错误状态码:" + code + result);
            }
        } catch (Exception e) {
            throw new RuntimeException("远程连接到" + url + "，发生错误:" + e.getMessage());
        } finally {
            if (httpclient != null)
                try {
                    httpclient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        return result;
    }

    /**
     * 使用get方式利用HttpClient请求数据
     */
    public static String post(String url, Map<String, String> params, String respCharset) {
        /*
        if(url.startsWith("https")){
			Protocol https = new Protocol("https",new org.infrastructure.ssl.HTTPSSecureProtocolSocketFactory(), 443);
	        Protocol.registerProtocol("https", https);
		}
		
		try {
			
			HttpClient client = new HttpClient();
			client.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, "UTF-8");
			PostMethod method = new PostMethod(url);
			if (params != null) {
				List<NameValuePair> nvList = new ArrayList<NameValuePair>();
				for (String key : params.keySet()) {
					NameValuePair nvp = new NameValuePair();
					nvp.setName(key);
					nvp.setValue(params.get(key));
					nvList.add(nvp);
				}
				method.setQueryString(nvList.toArray(new NameValuePair[] {}));
			}

			int code = client.executeMethod(method);
			InputStream resStream = method.getResponseBodyAsStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(resStream, respCharset));
			StringBuffer resBuffer = new StringBuffer();
			String resTemp = "";
			while ((resTemp = br.readLine()) != null) {
				resBuffer.append(resTemp);
			}
			String response = resBuffer.toString();

			if (code != 200)
				throw new BizException("错误状态码:" + code + response);

			return response;
		} catch (Exception e) {
			logger.error("", e);
			throw new BizException("远程连接到" + url + "，发生错误:" + e.getMessage());
		} finally{
			if(url.startsWith("https")){
				Protocol.unregisterProtocol("https");
			}
		}
		*/
        return "";
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
