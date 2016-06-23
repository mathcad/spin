package org.infrastructure.fn.intef.alipay;

/* *
 *类名：AlipayConfig
 *功能：基础配置类
 *详细：设置帐户有关信息及返回路径
 *版本：3.3
 *日期：2012-08-10
 *说明：
 *以下代码只是为了方便商户测试而提供的样例代码，商户可以根据自己网站的需要，按照技术文档编写,并非一定要使用该代码。
 *该代码仅供学习和研究支付宝接口使用，只是提供一个参考。
	
 *提示：如何获取安全校验码和合作身份者ID
 *1.用您的签约支付宝账号登录支付宝网站(www.alipay.com)
 *2.点击“商家服务”(https://b.alipay.com/order/myOrder.htm)
 *3.点击“查询合作者身份(PID)”、“查询安全校验码(Key)”

 *安全校验码查看时，输入支付密码后，页面呈灰色的现象，怎么办？
 *解决方法：
 *1、检查浏览器配置，不让浏览器做弹框屏蔽设置
 *2、更换浏览器或电脑，重新登录查询。
 */
@SuppressWarnings("serial")
public class AlipayConfig implements java.io.Serializable{
	
	private AlipayConfig(){
		
	}
	
	/**AES的公钥**/
	public String AESkey="0NlyDhKu1HKCnZw5mSzK0Q==";
//	public String AESkey="4DqQBgfw0inGn2Ea8NtRBg==";
	
	/**收款代付合作身份者ID，以2088开头由16位纯数字组成的字符串*/
	public  String partner = "2088121482273633";
//	public  String partner = "2088911492397119";
	
	/**收款支付宝账号*/
	public  String seller_email = "finance@gsh56.com";
//	public  String seller_email = "yzwjs@pgl-world.com";
	
	/**收款账户的私钥*/
	public  String key = "8rmuhetm4we3rrqzse2sepsx38moldrx";
//	public  String key = "ovews76lcr71vafb1507gvhobjqy01i6";

	/**调试用，创建TXT日志文件夹路径*/
	public  String log_path = "D:\\";
	
	/**保存付款提交的文件**/
	public String pay_path ="C:\\pay_path\\";
	
	/**徽商支付**/
	public String hs_path ="C:\\hs_path\\";
	
	/**查询余额保存的文件**/
	public String blanceQuery_path ="C:\\blanceQuery_path\\";
	
	/**保存付款查询的文件**/
	public String payQuery_path ="C:\\payQuery_path\\";

	/**字符编码格式 目前支持 gbk 或 utf-8*/
	public  String input_charset = "utf-8";
	
	/**签名方式 不需修改*/
	public   String sign_type = "MD5";
	
	/**收款后，前台返回交易完成后跳转的URL*/
	public   String return_url = "http://60.167.109.187:88/www/fn/alipay/payReturn.action";
//	public   String return_url = "http://219.137.192.66:8080/www/fn/alipay/payReturn.action";

	/**收款后，后台异步接收财付通通知的URL*/
	public   String notify_url = "http://60.167.109.187:88/www/fn/alipay/payNotify.action";
//	public   String notify_url = "http://219.137.192.66:8080/www/fn/alipay/payNotify.action";
	
	/**收款后，后台异步支付报错URL*/
	public   String error_url = "http://60.167.109.187:88/www/fn/alipay/error.action";
//	public   String error_url = "http://219.137.192.66:8080/www/fn/alipay/error.action";
	
	/**验证收款后异步结果验证有效性*/
	public  String HTTPS_VERIFY_URL = "https://mapi.alipay.com/gateway.do?service=notify_verify&";
	
	/**收款网关支付宝提供给商户的服务接入网关URL(新)*/
	public  String ALIPAY_GATEWAY_NEW = "https://mapi.alipay.com/gateway.do?";

	public String getPartner() {
		return partner;
	}

	public void setPartner(String partner) {
		this.partner = partner;
	}

	public String getSeller_email() {
		return seller_email;
	}

	public void setSeller_email(String seller_email) {
		this.seller_email = seller_email;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getLog_path() {
		return log_path;
	}

	public void setLog_path(String log_path) {
		this.log_path = log_path;
	}

	public String getPay_path() {
		return pay_path;
	}

	public void setPay_path(String pay_path) {
		this.pay_path = pay_path;
	}

	public String getPayQuery_path() {
		return payQuery_path;
	}

	public void setPayQuery_path(String payQuery_path) {
		this.payQuery_path = payQuery_path;
	}

	public String getInput_charset() {
		return input_charset;
	}

	public void setInput_charset(String input_charset) {
		this.input_charset = input_charset;
	}

	public String getSign_type() {
		return sign_type;
	}

	public void setSign_type(String sign_type) {
		this.sign_type = sign_type;
	}

	public String getReturn_url() {
		return return_url;
	}

	public void setReturn_url(String return_url) {
		this.return_url = return_url;
	}

	public String getNotify_url() {
		return notify_url;
	}

	public void setNotify_url(String notify_url) {
		this.notify_url = notify_url;
	}

	public String getError_url() {
		return error_url;
	}

	public void setError_url(String error_url) {
		this.error_url = error_url;
	}

	public String getHTTPS_VERIFY_URL() {
		return HTTPS_VERIFY_URL;
	}

	public void setHTTPS_VERIFY_URL(String hTTPS_VERIFY_URL) {
		HTTPS_VERIFY_URL = hTTPS_VERIFY_URL;
	}

	public String getALIPAY_GATEWAY_NEW() {
		return ALIPAY_GATEWAY_NEW;
	}

	public void setALIPAY_GATEWAY_NEW(String aLIPAY_GATEWAY_NEW) {
		ALIPAY_GATEWAY_NEW = aLIPAY_GATEWAY_NEW;
	}

	public String getAESkey() {
		return AESkey;
	}

	public void setAESkey(String aESkey) {
		AESkey = aESkey;
	}

	public String getBlanceQuery_path() {
		return blanceQuery_path;
	}

	public void setBlanceQuery_path(String blanceQuery_path) {
		this.blanceQuery_path = blanceQuery_path;
	}

	public String getHs_path() {
		return hs_path;
	}

	public void setHs_path(String hs_path) {
		this.hs_path = hs_path;
	}
	
}
