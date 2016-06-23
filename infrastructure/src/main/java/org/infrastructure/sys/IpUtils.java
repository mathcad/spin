package org.infrastructure.sys;

import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

/**
 * 
 * 获取ip地址
 * 
 * @author zx
 * @contact 电话: 18963752887, QQ: 251915460
 * @create 2016年4月13日 下午1:29:43
 * @version V1.0
 */
public class IpUtils {
	
	/**
	 * 获取ip地址
	 * 
	 * @param request
	 * @return
	 * @version 1.0
	 */
	public static String getIpAddr(HttpServletRequest request) {  
		String ip = request.getHeader("x-forwarded-for");  
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
			ip = request.getHeader("Proxy-Client-IP");  
		}  
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
			ip = request.getHeader("WL-Proxy-Client-IP");  
		}  
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
			ip = request.getRemoteAddr();  
		}  
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
			ip = request.getHeader("http_client_ip");  
		}  
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
			ip = request.getHeader("HTTP_X_FORWARDED_FOR");  
		}  
		// 如果是多级代理，那么取第一个ip为客户ip  
		if (ip != null && ip.indexOf(",") != -1) {  
			ip = ip.substring(ip.lastIndexOf(",") + 1, ip.length()).trim();  
		}  
		return ip;  
	}
	
	/**
	 * 是否远程的ipv4客户端地址 
	 * 10.0.0; 192.168；172.*；127.*都属于局域网地址
	 * 
	 * @return
	 * @version 1.0
	 */
	public static boolean isIpv4(String ip,boolean remoteOnly){
		String ipV4Pattern = "((?:(?:25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d)))\\.){3}(?:25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d))))";
		boolean ipv4 = Pattern.matches(ipV4Pattern,ip);
		if(remoteOnly){
			//局域网地址
			if(ip.startsWith("192.168.") || ip.startsWith("10.0.") || ip.startsWith("172.16.")|| ip.startsWith("127.")){
				ipv4 =false;
			}
		}
		return ipv4;
	}

}
