package org.infrastructure.sys;

/**
 * 基础常量
 * 
* @author zhou
* @contact 电话: 18963752887, QQ: 251915460
* @create 2015年3月19日 下午2:13:40 
* @version V1.0
 */
public class Constants {
	/**
	 * 信用贷款类型
	 */
	public static final int LOAN_TYPE_ONE = 1;
	
	/**
	 * 房产贷款类型
	 */
	public static final int LOAN_TYPE_TWO = 2;
	
	/**
	 * 汽车贷款类型
	 */
	public static final int LOAN_TYPE_THREE = 3;
	
	/**
	 * 企业贷款类型
	 */
	public static final int LOAN_TYPE_FOUR = 4;
	
	/**
	 * 应急贷款类型
	 */
	public static final int LOAN_TYPE_FIVE = 5;
	
	/**
	 * SMS用户名
	 */
	public static final String SMS_USER_NAME = "***";
	
	/**
	 * SMS密码
	 */
	public static final String SMS_PASS_CODE = "***";
	
	/**
	 * SMS 发送短信（gbk）DNS
	 */
	public static final String SMS_SEND_GBK_DNS = "http://*.*.*.*:8000/msm/sdk/http/sendsms.jsp";
	
	/**
	 * SMS 发送短信（utf-8）DNS
	 */
	public static final String SMS_SEND_UTF_DNS = "http://*.*.*.*:8000/msm/sdk/http/sendsmsutf8.jsp";
	
	/**
	 * SMS 修改密码DNS
	 */
	public static final String SMS_CHANGES_CODE_DNS = "http://*.*.*.*:8000/msm/sdk/http/changescode.jsp";
	
	/**
	 * SMS 余额DNS
	 */
	public static final String SMS_BALANCE_DNS = "http://*.*.*.*:8000/msm/sdk/http/balance.jsp";
	
	/**
	 * SMS 模板编码
	 */
	public static final String SMS_TEMPLETE_CODE = "MB-2013102300";
	/**
	 * 贷款申请短信通知模版(信贷经理)
	 */
	public static final String SMS_TEMPLETE_LOAN_ORDER_CODE = "MB-2014062303";
	/**
	 * 贷款申请短信通知模版(申请人)
	 */
	public static final String SMS_TEMPLETE_FOR_APPLICANT_CODE = "MB-2014090136";
	/**
	 * 商家提醒接受短信模版
	 */
	public static final String SMS_TEMPLETE_FOR_BUSINESS_CODE = "MB-2014092930";
	/**
	 * 通知贷款申请客户失败短信模版
	 */
	public static final String SMS_TEMPLETE_FOR_USER_APPLY_FAIL_CODE = "MB-2014093053";
	
	/**
	 * SMS 短信内容前缀
	 */
	public static final String SMS_CONTENT_PREFIX = "@1@=";
	
	/**
	 * SMS验证提示
	 */
	public static final String SMS_TIP_MOBILE = "手机号码最多100个";
	public static final String SMS_TIP_CONTENT = "短信内容不能超过300个字";
	public static final String SMS_TIP_NULL_MOBILE = "手机号码不能为空";
	public static final String SMS_TIP_EXTCODE = "分机账户不正确";
	public static final String SMS_TIP_SENDTIME = "发送时间格式不正确";
	public static final String SMS_TIP_MSGID = "短信id最大18位";
	
	/**
	 * 短信下发成功
	 */	 
	public static final String SMS_RECIVE_SUCCESS="0#1#1";
	
	/**
	 * 短信下发后 后台是否正确返回
	 */
	public static final String SMS_BACK_SUCCESS="1";
	public static final String SMS_BACK_FAILURE="0";
	
			
	 public static final String PAGE_NUM_KEY="currentPage"; //当前页数
	 
	 public static final String PAGESIZE_KEY="pageSize";	//当前页记录数
	 
	 public static final String AUTHENTICATION_TRUE="1"; //已经认证
	 
	 public static final String AUTHENTICATION_FALSE="0"; //未认证
	 
	 public static final String ROW_START="startRow";
	 
	 public static final String ROW_END="endRow";
	 
	 public static final String SEX_KEY="sex";
	 
	 	 
	 
	 public class UserContant{
		 
		 public static final int USER_EXIST=1;
		 
		 public static final int USER_NO_EXIST=0;
		 
		 public static final String SESSION_KEY="user_session";
		 
	 }
	 
	 /**
	  * 职业身份类型
	  * @author zx
	  *
	  */
	 public static class ApplicantType {
		 
		 public static final int TYPE_QYZ = 1;
		 
		 public static final int TYPE_GTH = 2;
		 
		 public static final int TYPE_SBZ =3;
		 
		 public static final int TYPE_WGDZY =4;
	 }
	 
	 /**
	  * 订单来源
	  * @author zx
	  *
	  */
	 public static class OrderSource {
		 /**贷多少WEB版在线申请**/
		 public static final short APPLY_ONLINE = 1;
		 /**贷多少WEB版贷款评估**/
		 public static final short LOAN_VALUATION = 2;
		 /**贷多少WEB版快速申请贷款**/
		 public static final short SIMPLE_LOAN_APPLY = 3;
	 }
	 
	 public static int REGISTER_TYPE_WEB = 1;
	 public static int REGISTER_TYPE_ADMIN = 2;
	 public static int REGISTER_TYPE_WAP = 3;
	 public static int REGISTER_TYPE_ANDROID = 4;
	 public static int REGISTER_TYPE_ISO = 5;
	 public static int REGISTER_TYPE_WP = 6;
	 
	 public static String LOAN_MERCHANTS =  "1";//商家
	 public static String LOAN_PERSON =  "0";//贷款人
	 
	 public static short USER_PURPOSE_LOAN = 1;//贷款
	 public static short USER_PURPOSE_SEND = 2;//放款
	 
	 public static int DEFAULT_QUOTA = 10;//默认贷款额度
	 public static int DEFAULT_TERM = 12;//默认贷款期限
	 
	 public static int BANK_TYPE = 1;//银行小贷公司
	 public static int SIMALL_TYPE = 2;//中介
	 public static int P2P_TYPE = 2;//中介
	 
	 /**
	  * 找回密码
	  */
	 public static String FIND_PWD_SESSION_CODE = "findPwdCodeBy_PHONE";
	 public static String FIND_PWD_COOKIEA_CODE = "findPwdCodeBY_EMAIL";
	 
	 public static String LOGIN_USER_AUTH = "LoginUserAuth";//登录用户SESSIONID身份验证	
	 public static String USER_AUTO_REGISTER = "USER_AUTO_REGISTER";//系统自动注册用户
	 public static String LOGIN_USER = "LoginUser";
	 public static String REGISTER_STEP_ONE_USER = "RegisterStepOneUser";
	 public static String LOGIN_USER_VO = "LoginUserVO";
	 public static String IMAGE_AUTH = "authImage";
	 
	 public static String ACCOUNT_NONO1 = "000";//贷款人和商家登录页面做区分,客户登录通道
	 public static String ACCOUNT_NONO2 = "111";//贷款人和商家登录页面做区分,商家登录通道
	 public static String ACCOUNT_ERROR = "0";//用户名或者密码有误
	 public static String ACCOUNT_SUCCESS = "1";//贷款会员登录成功
	 public static String ACCOUNT_LOAN_SUCCESS = "10";//贷款会员登录成功
	 public static String ACCOUNT_INS_SUCCESS = "11";//贷款会员登录成功
	 public static String ACCOUNT_IMAGE = "2";//验证码有误
	 public static String ACCOUNT_EXCEPTION = "-1";//登录异常
	 public static String CRREATE_IMAGE_CODE_ERROR = "-1";//生成验证码异常
	 
	 public static short USER_TYPE_INT_ONE = 1;//贷款人
	 public static short USER_TYPE_INT_TWO = 2;//贷款公司
	 public static short USER_TYPE_INT_THREE = 3;//顾问
	 public static short USER_TYPE_INT_FOUR = 4;//机构
	 
	 public static int USER_TYPE_ONE = 1;//贷款人
	 public static int USER_TYPE_TWO = 2;//贷款公司
	 public static int USER_TYPE_THREE = 3;//顾问
	 public static int USER_TYPE_FOUR = 4;//机构
	 
	 public static String TRACK_CHANNEL="track_channel";
	 
	 public static String[] LETTERS = {
		 "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"
	 };
	 
	 //首页默认产品条数
	 public static int INDEX_PRODUCT_SIZE = 5;
	 
	 public static String MEDIA_PARTNER = "1";//友情链接
	 public static String BANK_PARTNER = "2";//友情链接
	 public static String LINK_PARTNER = "3";//友情链接
	 
	 public static final String BIND_MOBILE_TYPE = "1";
	 public static final String UNBIND_MOBILE_TYPE = "0";
	
	 public static final String CITY_SITE = "citySite";
	
	 public static enum NewsType{
		loan_strategy("贷款必读",1),
		loan_message("新手贷款",2),
		loan_company("企业贷款",6),
		financial_strategy("理财必读",11),
		financial_message("P2P理财",12),
		financial_school("信用卡",16);
		
		private NewsType(String name, int value){
			this.name = name;
			this.value = value;
		}
		
		private String name;
		private int value;
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public int getValue() {
			return value;
		}
		public void setValue(int value) {
			this.value = value;
		}
	 }
	 
	 public static final int INDEX_RECOMMEND_CONSULTANT_SIZE = 3;
	 
	 public static final int INDEX_NEWS_SIZE = 8;
	 
	 //评价种类
	 public static enum EVA_YPE {
		 eva_sum("综合评价", 0),
		 eva_service("服务态度", 1),
		 eva_speed("受理速度", 2),
		 eva_major("专业能力", 3);
		 
		 private EVA_YPE(String name, int value){
			this.name = name;
			this.value = value;
		 }
		 private String name;
		 private int value;
		 public String getName() {
			 return name;
		 }
		 public void setName(String name) {
			 this.name = name;
		 }
		 public int getValue() {
			 return value;
		 }
		 public void setValue(int value) {
			 this.value = value;
		 }
	 }
	 
	 public static final int BUSINESS_EVA_DEFAULT_SIZE = 5;
	 
	 public static final String DEFUALT_NAME = "匿名";
	 
	 public static final String REALNAME_V_STATUS_ONE = "1";
	 public static final String REALNAME_V_STATUS_TWO = "2";
	 
	 public static final int CITY_ALL_COUNTRY = 999999;
	 
	 
	 public static final String KEY_SESSION_RSA_KEYS = "KEY_SESSION_RSA_KEYS";
	 public static final String KEY_SESSION_AES_KEYS = "KEY_SESSION_AES_KEYS";
}
