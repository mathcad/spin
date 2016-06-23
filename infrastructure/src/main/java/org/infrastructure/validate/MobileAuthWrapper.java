package org.infrastructure.validate;




public class MobileAuthWrapper {
	
	private String mobile;
	
	private String AuthCode;
	
	public MobileAuthWrapper() {
		auth = new MobileAuthMeta();
	}
	
//	@MobileAuth
	private MobileAuthMeta auth;

	public MobileAuthMeta getAuth() {
		return auth;
	}

	public void setAuth(MobileAuthMeta auth) {
		this.auth = auth;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
		auth.setMobile(mobile);
	}

	public String getAuthCode() {
		return AuthCode;
	}

	public void setAuthCode(String authCode) {
		AuthCode = authCode;
		auth.setCode(authCode);
	}
}
