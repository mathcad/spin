package org.infrastructure.sms;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.infrastructure.sys.Constants;
import org.infrastructure.util.StringUtils;

/**
 * SMS 接口
 */
public class SMSUtils {
	private static final Log log = LogFactory.getLog(SMSUtils.class);
	
	/**
	 * 修改SMS服务账号密码
	 * @Title: updatePassword 
	 * @Description:  修改SMS服务账号密码
	 * @param @param username
	 * @param @param oldPassword
	 * @param @param newPassword
	 * @param @return
	 * @param @throws UnsupportedEncodingException    设定文件 
	 * @return String    返回类型  0:成功 ，100：失败， 101：用户账号不存在或密码错误，102：账号已禁用，103：参数不正确，104：提交过于频繁,超过1分钟内限定的流量
	 * @throws
	 */
	public static String updatePassword(String username, String oldPassword, String newPassword) throws UnsupportedEncodingException {
		Map<String, String> map = new HashMap<String, String>();
		map.put("username", username);
		map.put("scode", oldPassword);
		map.put("newscode", newPassword);
		return HttpPost.doPost(Constants.SMS_CHANGES_CODE_DNS, map, "GBK");
	}
	
	/**
	 * 账户余额查询
	 * @Title: balance 
	 * @Description: 账户余额查询 
	 * @param @param username
	 * @param @param password
	 * @param @return    设定文件 
	 * @return String    返回类型 
	 * @throws
	 */
	public static String balance(String username, String password) throws UnsupportedEncodingException {
		Map<String, String> map = new HashMap<String, String>();
		map.put("username", username);
		map.put("scode", password);
		return HttpPost.doPost(Constants.SMS_BALANCE_DNS, map, "GBK");
	}
	
	/**
	 * 发送短信
	 * @Title: sendSMS 
	 * @Description: 发送短信 
	 * @param @param username 
	 * @param @param password
	 * @param @param mobiles 手机号
	 * @param @param content 内容
	 * @param @param extcode 分机号 
	 * @param @param sendtime 定时
	 * @param @param msgid id
	 * @param @param msgtype 类型
	 * @param @param signtag 签名
	 * @param @param tempid 模板
	 * @param @param encoding 编码
	 * @param @return
	 * @param @throws UnsupportedEncodingException    设定文件 
	 * @return String    返回类型 
	 * @throws
	 */
	public static String sendSMS(String username, String password, String[] mobiles, 
			String content, String extcode, String sendtime, String msgid, 
			String msgtype, String signtag, String tempid, String encoding) throws UnsupportedEncodingException {
		Map<String, String> map = new HashMap<String, String>();
		map.put("username", username);
		map.put("scode", password);
		
		if (mobiles == null) {
			return Constants.SMS_TIP_NULL_MOBILE;
		}else if(mobiles.length > 100) {
			return Constants.SMS_TIP_MOBILE;
		}else {
			StringBuilder sb = new StringBuilder();
			for (String s : mobiles) {
				if (StringUtils.isNotBlank(s)) {
					sb.append(s);
				}
			}
			map.put("mobile", sb.toString());
		}
		if (tempid != null ) {
			map.put("tempid", tempid);
		}else {
			map.put("tempid", Constants.SMS_TEMPLETE_CODE);
		}
		
		if (content != null ) {
			if (content.length() > 300) {
				return Constants.SMS_TIP_MOBILE;
			}else{
				map.put("content", Constants.SMS_CONTENT_PREFIX + content);
			}
		}
		
		if (extcode != null) {
			if (extcode.length() > 4) {
				return Constants.SMS_TIP_EXTCODE;
			}else {
				map.put("extcode", extcode);
			}
		}
		
		if (sendtime != null) {
			if (sendtime.length() != 14) {
				return Constants.SMS_TIP_SENDTIME;
			}else{
				map.put("sendtime", sendtime);
			}
		}
		
		if (msgid != null) {
			if (msgid.length() > 18) {
				return Constants.SMS_TIP_MSGID;
			}else {
				map.put("msgid", msgid);
			}
		}
		
		if (msgtype != null) {
			map.put("msgtype", msgtype);
		}
		
		if (signtag != null) {
			map.put("signtag", signtag);
		}
		if (encoding != null && "UTF-8".equals(encoding.toUpperCase())) {
			return HttpPost.doPost(Constants.SMS_SEND_UTF_DNS, map, encoding);
		}
		return HttpPost.doPost(Constants.SMS_SEND_GBK_DNS, map, "GBK");
	}
	
	/**
	 * 及时发送短信
	 * @Title: sendSMS 
	 * @Description: 及时发送短信 
	 * @param @param mobiles 手机号
	 * @param @param content 内容
	 * @param @return
	 * @param @throws UnsupportedEncodingException    设定文件 
	 * @return String    返回类型 
	 * @throws
	 */
	public static String sendSMS(String[] mobiles, String content) throws UnsupportedEncodingException{
		return sendSMS(Constants.SMS_USER_NAME, Constants.SMS_PASS_CODE, mobiles, content, null, null, null, null, null, Constants.SMS_TEMPLETE_CODE, null);
	}
	/**
	 * 及时发送短信
	 * @Title: sendSMS 
	 * @Description: 及时发送短信 
	 * @param @param mobile 单个手机号
	 * @param @param content 内容
	 * @param @return
	 * @param @throws UnsupportedEncodingException    设定文件 
	 * @return String    返回类型 
	 * @throws
	 */
	public static String sendSMS(String mobile, String content) throws UnsupportedEncodingException{
		String[] mobiles=new String[1];
		if(null!=mobile && !("").equals(mobile.trim())){
			mobiles[0]=mobile;
		 return sendSMS(Constants.SMS_USER_NAME, Constants.SMS_PASS_CODE, mobiles, content, null, null, null, null, null, Constants.SMS_TEMPLETE_CODE, null);
		 }
		return null;
	}
	/**
	 * 发送手机短信通知
	 * @param mobile 手机号码
	 * @param content 内容
	 * @param smsTemplete 模版编号
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	public static Boolean sendSMS(String mobile, String content, String smsTemplete) throws Exception {
		String logContent = "向手机"+mobile+"发送模板为:"+smsTemplete+"变量为:"+content+"的短信";
		String[] mobiles=new String[1];
		if(null!=mobile && !("").equals(mobile.trim())){
			mobiles[0]=mobile;
			 String statusCode = sendSMS(Constants.SMS_USER_NAME, Constants.SMS_PASS_CODE, mobiles,
						 content, null, null, null, null, null, smsTemplete, null);
			 if (!statusCode.startsWith("0")) {
				 System.err.println("SMS信息发送失败， 错误码：" + statusCode);
				 log.info(logContent+",发送失败");
			 } else {
				 log.info(logContent+",发送成功");
				 return true;
			 }
		 }
		return false;
	}
}
         