package org.infrastructure.wx.wx.base;

public interface EventType {
	
	/**
	 * 成员关注事件
	 */
	String SUBSCRIBE = "subscribe";

	/**
	 * 成员取消关注事件
	 */
	String UNSUBSCRIBE = "unsubscribe";

	/**
	 * 上报地理位置事件
	 */
	String LOCATION = "LOCATION";

	/**
	 * 上报菜单事件 点击菜单拉取消息的事件推送
	 */
	String CLICK = "CLICK";

	/**
	 * 上报菜单事件 点击菜单跳转链接的事件推送
	 */
	String VIEW = "VIEW";

	/**
	 * 扫码推事件的事件
	 */
	String SCANCODE_PUSH = "scancode_push";

	/**
	 * 扫码推事件且弹出“消息接收中”提示框事件
	 */
	String SCANCODE_WAITMSG = "scancode_waitmsg";

	/**
	 * 弹出系统拍照发图的事件
	 */
	String PIC_SYSPHOTO = "pic_sysphoto";

	/**
	 * 弹出拍照或者相册发图的事件
	 */
	String PIC_PHOTO_OR_ALBUM = "pic_photo_or_album";

	/**
	 * 弹出微信相册发图器的事件
	 */
	String PIC_WEIXIN = "pic_weixin";

	/**
	 * 弹出地理位置选择器的事件
	 */
	String LOCATION_SELECT = "location_select";

	/**
	 * 成员进入应用的事件
	 */
	String ENTER_AGENT = "enter_agent";

	/**
	 * 异步任务完成事件
	 */
	String BATCH_JOB_RESULT = "batch_job_result";
}
