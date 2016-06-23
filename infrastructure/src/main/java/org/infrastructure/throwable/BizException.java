package org.infrastructure.throwable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * 业务异常
 * 
 * @author zhou
 *
 */
public class BizException extends RuntimeException {
	private static final long serialVersionUID = -3134828827080936834L;

	/**
	 * 错误代码
	 */
	private String code;

	/**
	 * 错误消息
	 */
	private String msg;

	public BizException() {
	}

	/**
	 * 错误消息+代码
	 * 
	 * @param msg
	 */
	public BizException(String msg, String code) {
		super(msg);
		this.code = code;
	}

	/**
	 * 错误消息+代码+堆栈
	 * 
	 * @param msg
	 * @param code
	 * @param t
	 */
	public BizException(String msg, String code, Throwable t) {
		super(t);
		this.code = code;
		this.msg = msg;
	}

	/**
	 *
	 * @param msg
	 */
	public BizException(String msg) {
		super(msg);
	}

	public BizException(String msg, Throwable t) {
		super(t);
		this.msg = msg;
	}

	@Override
	public String getMessage() {
		if (msg != null)
			return msg;
		else if (hasValidationMsg()) {
			StringBuilder sb = new StringBuilder();
			sb.append("属性验证异常");
			for (String key : validationMsgMap.keySet()) {
				sb.append("\r");
				sb.append(key).append(":");
				sb.append(validationMsgMap.get(key));
			}
			return sb.toString();
		} else
			return super.getMessage();
	}

	/**
	 * 从异常堆栈中获取BizException类型的异常
	 * 
	 * @param t
	 * @return
	 * @version 1.0
	 */
	public static BizException fromStack(Throwable t) {
		while (t != null) {
			if (t instanceof BizException) {
				return (BizException) t;
			}
			t = t.getCause();
		}
		return null;
	}

	LinkedHashMap<String, List<String>> validationMsgMap = new LinkedHashMap<String, List<String>>();

	/**
	 * 追加验证异常的消息
	 * 
	 * @param property
	 *            属性值
	 * @param errorMsg
	 *            错误异常
	 * @version 1.0
	 */
	public void addValidationMsg(String property, String errorMsg) {
		if (!validationMsgMap.containsKey(property)) {
			validationMsgMap.put(property, new ArrayList<String>());
		}
		validationMsgMap.get(property).add(errorMsg);
	}

	/**
	 * 是否有验证异常消息
	 * 
	 * @return
	 * @version 1.0
	 */
	public boolean hasValidationMsg() {
		return validationMsgMap.size() > 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Throwable#toString()
	 */
	@Override
	public String toString() {
		return this.getMessage();
	}

	/**
	 * 从错误列表显示异常
	 * 
	 * @param code
	 * @return
	 * @version 1.0
	 */
	public static BizException fromCodes(String code) {
		BizException e = new BizException();
		e.code = code;
		return e;
	}

	/**
	 * 错误吗
	 * 
	 * @return
	 * @version 1.0
	 */
	public String getCode() {
		return code;
	}
}
