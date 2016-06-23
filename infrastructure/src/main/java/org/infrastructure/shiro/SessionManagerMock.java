package org.infrastructure.shiro;

public class SessionManagerMock extends SessionManager {

	public SessionManagerMock() {
		// SessionUser testUser = new SessionUser() {
		//
		// @Override
		// public void setSessionId(String sessionId) {
		// }
		//
		// @Override
		// public String getPassword() {
		// return null;
		// }
		//
		// @Override
		// public String getName() {
		// return "testUser";
		// }
		//
		// @Override
		// public String getLoginName() {
		// return "testUser";
		// }
		//
		// @Override
		// public Long getId() {
		// return null;
		// }
		//
		// @Override
		// public void setId(Long id) {
		//
		// }
		// };
		// accountInfo = testUser;

	}

	SessionUser accountInfo = null;

	/**
	 * 得到Session中，当前登录用户
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Override
	public SessionUser getCurrentUser() {
		return accountInfo;
	}

	/**
	 * 更新session中的 user
	 * 
	 * @param accInfo
	 *            账户
	 */
	@Override
	public void setCurrentUser(SessionUser accInfo) {
		this.accountInfo = accInfo;
	}

}
