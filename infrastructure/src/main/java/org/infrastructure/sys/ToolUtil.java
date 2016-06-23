package org.infrastructure.sys;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.infrastructure.util.RandomStringUtils;
import org.infrastructure.util.StringUtils;


public final class ToolUtil {

	public static String getUUID() {
		return UUID.randomUUID().toString().replaceAll("-", "");
	}

	/**
	 * 
	 * @Title: getIpAddr 
	 * @Description:  获取ip
	 * @param @param request
	 * @param @return    设定文件 
	 * @return String    返回类型 
	 * @throws
	 */
	public static String getIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
	
	/**
	 * 日期转字符串
	 * @Title: dateToString 
	 * @Description: TODO(这里用一句话描述这个方法的作用) 
	 * @param @param date
	 * @param @param format
	 * @param @return    设定文件 
	 * @return String    返回类型 
	 * @throws
	 */
	public static String dateToString(String format, Date date) {
		SimpleDateFormat sdf=new SimpleDateFormat(format);  
        return sdf.format(date);
	}
	
	
	
	/**
	 * 随机生产4位验证码
	 * 
	 * @Title: getAuthCode
	 * @Description: TODO(这里用一句话描述这个方法的作用)
	 * @param @return 设定文件
	 * @return int 返回类型
	 * @throws
	 */
	public static String getAuthCode(){
		return RandomStringUtils.randomNumeric(4);
	}
	
	/**
	 * 删除一个字符串两头的逗号
	 * @Title: deleteSymbolString 
	 * @Description: TODO(这里用一句话描述这个方法的作用) 
	 * @param @param s
	 * @param @return    设定文件 
	 * @return String    返回类型 
	 * @throws
	 */
	public static Integer[] deleteSymbolString(String s) {
		if (StringUtils.isNotBlank(s)) {
			String[] stringIds = s.split(",");
			List<Integer> ids = new ArrayList<Integer>(stringIds.length);
			for (int i = 0; i < stringIds.length; i++) {
				try {
					if(StringUtils.isNotBlank(stringIds[i])) {
						ids.add(Integer.valueOf(stringIds[i]));
					}
				} catch (NumberFormatException e) {
					continue;
				}
			}
			return ids.toArray(new Integer[]{});
		}
		return null;
	}
	
	public static void main(String[] args){
		Random random = new Random();
		System.out.println(String.format("%04d", 1));
	}
}
