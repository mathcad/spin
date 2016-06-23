package org.infrastructure.web.view;


import org.infrastructure.jpa.dto.Page;
import org.springframework.web.servlet.ModelAndView;


/**
 * 扩展 ModelAndView
 * 
 * 使用Gson序列化实体对象
 * 
 * @author zhou
 *
 */
public class ModelGsonView extends ModelAndView{
	/**
	 * 错误代码，Session无效，前台需要重新登录
	 */
	public static final String STATUS_SESSION_INVALID = "session_invalid";

	
	GsonView gsonView = null;
	
	/**
	 * 新建实体类型
	 */
	public ModelGsonView(){		
		gsonView = new GsonView();
		this.setView(gsonView);
	}
	
	/**
	 * 创建成功调用
	 * 
	 * @return
	 */
	public static ModelGsonView Ok() {
		return new ModelGsonView().ok();
	}

	/**
	 * 创建成功调用并返回分页查询结果
	 * 
	 * @param page
	 * @return json结果集
	 */
	public static ModelGsonView Ok(Page page) {
		return new ModelGsonView().ok(page);
	}
	
	/**
	 * 返回调用结果
	 * 
	 * @param success true成功;false失败
	 * @param msg 消息
	 * @return
	 */
	public ModelGsonView result(boolean success,String msg) {
		this.getModel().put("success", success);
		if (msg != null)
			this.getModel().put("msg", msg);

		return this;
	}
	
	/**
	 * 调用成功，同名方法重载success(msg)
	 * 
	 * @param msg
	 * @return
	 */
	public ModelGsonView ok(String msg){
		return this.result(true, msg);
	}
	
	/**
	 * 状态码（对应到）
	 * 
	 * @param msg
	 * @return
	 */
	public ModelGsonView statusCode(String status){
		return this.add("STATUS_CODE", status);
	}
		
	/**
	 * 调用成功
	 * 
	 * @return
	 */
	public ModelGsonView ok(){
		return this.result(true, null);
	}
	
	/**
	 * 调用成功
	 * 并add数据
	 * 
	 * @param msg
	 * @return
	 */
	public ModelGsonView ok(String key,Object value){
		return this.result(true,null).add(key, value);
	}
	
	/**
	 * 分页数据加载
	 * 
	 * @param pagination
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public ModelGsonView ok(Page page){
		this.result(true,null);
		this.add("data", page.data);
		return this.add("total",page.total);		
	}
	
	
	/**
	 * 调用失败，重载result(true,msg)
	 * 
	 * @param msg
	 * @return
	 */
	public ModelGsonView error(String msg){
		return this.result(false, msg);
	}
		
	/**
	 * put值and return this
	 * 
	 * @param key
	 * @param value
	 */
	public ModelGsonView add(String key,Object value){
		this.getModel().put(key, value);
		return this;
	}
	
	/**
	 * 放入分页数据
	 * 
	 * @param pgData
	 * @return
	 */
	public ModelGsonView set(Page pgData){
		this.ok();
		this.getModel().put("success", true);
		this.getModel().put("total", pgData.total);
		this.getModel().put("data", pgData.data);
		return this;
	}
	
	/**
	 * 返回内部的GsonView
	 * 
	 * @return
	 */
	public GsonView getGsonView(){
		return this.gsonView;
	}
	
	/**
	 * 一旦设置了此属性，Model中其他属性都家被忽略
	 * 直接生成jsonData到response中
	 * 
	 * @param jsonData
	 */
	public ModelGsonView setJsonData(Object jsonData) {
		this.getModel().put(GsonView.JSON_DATA_KEY, jsonData);
		return this;
	}
	
}
