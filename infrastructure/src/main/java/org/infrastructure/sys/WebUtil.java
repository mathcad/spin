package org.infrastructure.sys;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public final class WebUtil {
	/**
	 * 获得sessionID
	 * @return
	 */
	public static String getSessionId() {
		HttpSession session = getHttpSession();
		if (session != null) {
			return session.getId();
		}
		return null;
	}
	/**
	 * 获得HttpSession
	 * @return
	 */
	public static HttpSession getHttpSession() {
		
		return getCurrentServletRequest().getSession(true);
	}
	/**
	 * 获得HttpServletRequest
	 * @return
	 */
	public static HttpServletRequest getCurrentServletRequest() {
		return ((ServletRequestAttributes) RequestContextHolder
				.currentRequestAttributes()).getRequest();
	}
	/**
	 * 获得指定名称的cookie值
	 * @param name
	 * @return
	 */
	public static String getCookieValue(String name) {
		Cookie[] cookies = getCurrentServletRequest().getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals(name)) {
					return cookie.getValue();
				}
			}
		}
		
		return null;
	}
}
