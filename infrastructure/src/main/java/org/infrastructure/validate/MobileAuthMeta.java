package org.infrastructure.validate;

public class MobileAuthMeta {
	
	private String mobile;
	
	private String code;
	
	public MobileAuthMeta() {
	}

	public MobileAuthMeta(String mobile, String code) {
		super();
		this.mobile = mobile;
		this.code = code;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
}
