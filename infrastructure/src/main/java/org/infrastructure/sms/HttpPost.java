package org.infrastructure.sms;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * SMS servlet
 * @ClassName: HttpPost 
 * @Description: SMS servlet 
 * @author richard_du
 * @date 2014年4月30日 下午3:10:29 
 *
 */
public class HttpPost {
	
	/**
	 * doGet 有参
	 * @Title: doGet 
	 * @Description: doGet 有参
	 * @param @param reqUrl
	 * @param @param parameters
	 * @param @param recvEncoding
	 * @param @return    设定文件 
	 * @return String    返回类型 
	 * @throws
	 */
	public static String doGet(String reqUrl, Map<?, ?> parameters, String recvEncoding) {
		//url请求
		HttpURLConnection urlConnection = null;
		//响应内容
		String responseContent = null;
		//设置编码
		String charset = recvEncoding == "" ? HttpPost.requestEncoding : recvEncoding;
		try {
			StringBuffer params = new StringBuffer();
			//拼接url参数
			if (parameters != null) {
				for (Iterator<?> it = parameters.entrySet().iterator(); it.hasNext();) {
					Entry<?, ?> element=(Entry<?, ?>) it.next();
					params.append(element.getKey());
					params.append("=");
					params.append(URLEncoder.encode(element.getValue().toString(), charset));
					params.append("&");
				}
				//去掉多余的&
				if (params.length() > 0) {
					params = params.deleteCharAt(params.length() - 1);
				}
				
				URL url = new URL(reqUrl);
				urlConnection = (HttpURLConnection) url.openConnection();
				urlConnection.setRequestMethod("GET");
				
				System.setProperty("连接超时：", String.valueOf(HttpPost.connectTimeOut));
				System.setProperty("访问超时：", String.valueOf(HttpPost.readTimeOut));
				
				urlConnection.setDoOutput(true);// 设置是否向httpUrlConnection输出
				byte[] b = params.toString().getBytes();
				urlConnection.getOutputStream().write(b);
				urlConnection.getOutputStream().flush();
				urlConnection.getOutputStream().close();
				
				InputStream in = urlConnection.getInputStream();
				byte[] echo = new byte[10*1024];
				int len = in.read(echo);
				responseContent = (new String(echo,0,len).trim());
				
				int code = urlConnection.getResponseCode();
				if (code != 200) {
					responseContent = "ERROR" + code;
				}
			}
		} catch (Exception e) {
			System.out.println("网络故障:"+ e.toString());
			e.printStackTrace();
		} finally {
			if(urlConnection!=null){
				urlConnection.disconnect();
			}
		}
		return responseContent;
	}
	
	/**
	 * doGet 无参
	 * @Title: doGet 
	 * @Description: doGet 无参
	 * @param @param reqUrl
	 * @param @param recvEncoding
	 * @param @return    设定文件 
	 * @return String    返回类型 
	 * @throws
	 */
	public static String doGet(String reqUrl, String recvEncoding) {
		//url请求
		HttpURLConnection urlConnection = null;
		//响应内容
		String responseContent = null;
		//设置编码
		String charset = recvEncoding == "" ? HttpPost.requestEncoding : recvEncoding;
		try {
			StringBuffer params = new StringBuffer();
			//实际请求url
			String queryUrl = reqUrl;
			int paramIndex = reqUrl.indexOf("?");
			
			//拼接url参数
			if (paramIndex > 0) {
				queryUrl = reqUrl.substring(0, paramIndex);
				String parameters = reqUrl.substring(paramIndex + 1, reqUrl.length());
				String[] paramArray = parameters.split("&");
				for (int i = 0; i < paramArray.length; i++) {
					String string = paramArray[i];
					int index = string.indexOf("=");
					if (index > 0) {
						String parameter = string.substring(0, index);
						String value = string.substring(index + 1, string.length());
						params.append(parameter);
						params.append("=");
						params.append(URLEncoder.encode(value, charset));
						params.append("&");
					}
				}
				//去掉多余的&
				params = params.deleteCharAt(params.length() - 1);
				
				URL url = new URL(queryUrl);
				urlConnection = (HttpURLConnection) url.openConnection();
				urlConnection.setRequestMethod("GET");//GET
				
				System.setProperty("连接超时：", String.valueOf(HttpPost.connectTimeOut));
				System.setProperty("访问超时：", String.valueOf(HttpPost.readTimeOut));
				
				urlConnection.setDoOutput(true);// 设置是否向httpUrlConnection输出
				byte[] b = params.toString().getBytes();
				urlConnection.getOutputStream().write(b);
				urlConnection.getOutputStream().flush();
				urlConnection.getOutputStream().close();
				
				InputStream in = urlConnection.getInputStream();
				byte[] echo = new byte[10*1024];
				int len = in.read(echo);
				responseContent = (new String(echo,0,len).trim());
				
				int code = urlConnection.getResponseCode();
				if (code != 200) {
					responseContent = "ERROR" + code;
				}
			}
		} catch (Exception e) {
			System.out.println("网络故障:"+ e.toString());
			e.printStackTrace();
		} finally {
			if(urlConnection != null){
				urlConnection.disconnect();
			}
		}
		return responseContent;
	}
	
	public static String doPost(String reqUrl, Map<String, String> parameters, String recvEncoding) {
		//url请求
		HttpURLConnection urlConnection = null;
		//响应内容
		String responseContent = null;
		//设置编码
		String charset = recvEncoding == "" ? HttpPost.requestEncoding : recvEncoding;
		try {
			StringBuffer params = new StringBuffer();
			//拼接url参数
			if (parameters != null) {
				for (Iterator<?> it = parameters.entrySet().iterator(); it.hasNext();) {
					Entry<?, ?> element=(Entry<?, ?>) it.next();
					params.append(element.getKey());
					params.append("=");
					params.append(URLEncoder.encode(element.getValue().toString(), charset));
					params.append("&");
				}
				//去掉多余的&
				if (params.length() > 0) {
					params = params.deleteCharAt(params.length() - 1);
				}
				
				URL url = new URL(reqUrl);
				urlConnection = (HttpURLConnection) url.openConnection();
				urlConnection.setRequestMethod("POST");//POST
				urlConnection.setConnectTimeout(HttpPost.connectTimeOut);
				urlConnection.setReadTimeout(HttpPost.readTimeOut);
				urlConnection.setDoOutput(true);// 设置是否向httpUrlConnection输出
				byte[] b = params.toString().getBytes();
				urlConnection.getOutputStream().write(b, 0, b.length);
				urlConnection.getOutputStream().flush();
				urlConnection.getOutputStream().close();
				
				InputStream in = urlConnection.getInputStream();
				
				BufferedReader inR = new BufferedReader(
	                    new InputStreamReader(in));
				String line;
	            while ((line = inR.readLine()) != null) {
	            	if (responseContent == null)
	            		responseContent = "";
	            	responseContent += line;
	            }
				
//				byte[] echo = new byte[10*1024];
//				int len = in.read(echo);
//				responseContent = (new String(echo,0,len).trim());
				
				int code = urlConnection.getResponseCode();
				if (code != 200) {
					responseContent = "ERROR" + code;
				}
			}
		} catch (Exception e) {
			System.out.println("网络故障:"+ e.toString());
			e.printStackTrace();
		} finally {
			if(urlConnection!=null){
				urlConnection.disconnect();
			}
		}
		return responseContent;
	}
	
	/**
	 * 连接超时
	 */
	private static int connectTimeOut = 5000;
	
	/**
	 * 访问超时
	 */
	private static int readTimeOut = 10000;
	
	/**
	 * 编码
	 */
	private static String requestEncoding = "UTF-8";
	
	public static int getConnectTimeOut() {
		return connectTimeOut;
	}
	public static void setConnectTimeOut(int connectTimeOut) {
		HttpPost.connectTimeOut = connectTimeOut;
	}
	public static int getReadTimeOut() {
		return readTimeOut;
	}
	public static void setReadTimeOut(int readTimeOut) {
		HttpPost.readTimeOut = readTimeOut;
	}
	public static String getRequestEncoding() {
		return requestEncoding;
	}
	public static void setRequestEncoding(String requestEncoding) {
		HttpPost.requestEncoding = requestEncoding;
	}
}
