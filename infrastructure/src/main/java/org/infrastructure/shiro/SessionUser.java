package org.infrastructure.shiro;

/**
 * 在线用户的实体接口
 * 
 * @author xuweinan
 * @version V1.0
 */
public interface SessionUser {

	public Long getId();

	public void setId(Long id);

	public String getLoginName();

	public String getPassword();

	public String getRealName();

	public void setSessionId(String sessionId);
}
