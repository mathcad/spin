package org.infrastructure.fn.intef.tenpay;

/**
 * 财付通接口配置
 * 
 * @author zhou
 * @contact 电话: 18963752887, QQ: 251915460
 * @create 2015年5月18日 上午11:06:01
 * @version V1.0
 */
@SuppressWarnings("serial")
public class TenpayConfig implements java.io.Serializable{
	
	private TenpayConfig(){
	}
	
	/**代付接口使用*/
	public   String caPath = "G:/财付通/cacert.pem";
	
	/**代付帐号的CA证书*/
	public    String certPath	= "G:/财付通/1234458901_20150318170420.pfx";
	
	/**代付商户号*/
	public  	String payPartner = "1234458901";
	
	/**代付商户号登录密码*/
	public String opPassword="141676";
	
	/**代付商户号密钥*/
	public  	String payKey = "25e54020a3f99bffe7b80c0523310646";
	
    /**代付账户的证书密码，系统上线前请注意修改为正确值(通过短信方式发送到合同登记的手机号，)*/
	public   String certPassword = "220453";

	/**代收商户号*/
	public  	String partner = "1234458701";

	/**代收商户号密钥*/
	public  	String key = "28a400e86b90567f712bb61b23814679";
	
	/**交易完成后跳转的URL*/
	public   String gateUrl ="https://mch.tenpay.com/cgi-bin/mchbatchtransfer.cgi";
	
	/**收款成功同步返回地址*/
	public   String return_url = "http://219.137.192.66:8080/www/fn/tenpay/payReturn.action";

	/**收款成功报告地址*/
	public   String notify_url = "http://219.137.192.66:8080/www/fn/tenpay/payNotify.action";
	
	/**收款异常报告地址*/
	public   String error_url = "http://219.137.192.66:8080/www/fn/tenpay/error.action";
	
	/**字符编码格式 目前支持 gbk 或 utf-8*/
	public  String input_charset = "utf-8";
	
	/**网关url地址*/ 
	public  String gatway="https://gw.tenpay.com/gateway/pay.htm";
	
	/**通知*/
	public  String notifyway="https://gw.tenpay.com/gateway/simpleverifynotifyid.xml";

	public String getCaPath() {
		return caPath;
	}

	public void setCaPath(String caPath) {
		this.caPath = caPath;
	}

	public String getCertPath() {
		return certPath;
	}

	public void setCertPath(String certPath) {
		this.certPath = certPath;
	}

	public String getPayPartner() {
		return payPartner;
	}

	public void setPayPartner(String payPartner) {
		this.payPartner = payPartner;
	}

	public String getOpPassword() {
		return opPassword;
	}

	public void setOpPassword(String opPassword) {
		this.opPassword = opPassword;
	}

	public String getPayKey() {
		return payKey;
	}

	public void setPayKey(String payKey) {
		this.payKey = payKey;
	}

	public String getCertPassword() {
		return certPassword;
	}

	public void setCertPassword(String certPassword) {
		this.certPassword = certPassword;
	}

	public String getPartner() {
		return partner;
	}

	public void setPartner(String partner) {
		this.partner = partner;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getGateUrl() {
		return gateUrl;
	}

	public void setGateUrl(String gateUrl) {
		this.gateUrl = gateUrl;
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

	public String getInput_charset() {
		return input_charset;
	}

	public void setInput_charset(String input_charset) {
		this.input_charset = input_charset;
	}

	public String getGatway() {
		return gatway;
	}

	public void setGatway(String gatway) {
		this.gatway = gatway;
	}

	public String getNotifyway() {
		return notifyway;
	}

	public void setNotifyway(String notifyway) {
		this.notifyway = notifyway;
	}
}
