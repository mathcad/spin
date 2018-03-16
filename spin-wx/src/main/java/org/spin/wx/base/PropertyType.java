package org.spin.wx.base;

public final class PropertyType {
    private PropertyType() {
    }

    /**
     * 企业号CorpID
     */
    public static final String TO_USER_NAME = "ToUserName";

    /**
     * 成员UserID
     */
    public static final String FROM_USER_NAME = "FromUserName";

    /**
     * 消息创建时间（整型）
     */
    public static final String CREATE_TIME = "CreateTime";

    /**
     * 消息类型
     */
    public static final String MSG_TYPE = "MsgType";

    /**
     * 文本消息内容
     */
    public static final String CONTENT = "Content";

    /**
     * 消息id，64位整型
     */
    public static final String MSG_ID = "MsgId";

    /**
     * 企业应用的id，整型。可在应用的设置页面查看
     */
    public static final String AGENT_ID = "AgentID";

    /**
     * 图片消息的图片链接
     */
    public static final String PIC_URL = "PicUrl";

    /**
     * 媒体文件id，可以调用获取媒体文件接口拉取数据
     */
    public static final String MEDIA_ID = "MediaId";

    /**
     * 语音消息的语音格式，如amr，speex等
     */
    public static final String FORMAT = "Format";

    /**
     * 视频消息缩略图的媒体id，可以调用获取媒体文件接口拉取数据
     */
    public static final String THUMB_MEDIA_ID = "ThumbMediaId";

    /**
     * 地理位置纬度
     */
    public static final String LOCATION_X = "Location_X";

    /**
     * 地理位置经度
     */
    public static final String LOCATION_Y = "Location_Y";

    /**
     * 地图缩放大小
     */
    public static final String SCALE = "Scale";

    /**
     * 地理位置信息
     */
    public static final String LABEL = "Label";

    /**
     * 事件类型
     */
    public static final String EVENT = "Event";

    /**
     * 上报地理位置事件中的地理位置纬度
     */
    public static final String LATITUDE = "Latitude";

    /**
     * 上报地理位置事件中的地理位置经度
     */
    public static final String LONGITUDE = "Longitude";

    /**
     * 上报地理位置事件中的地理位置精度
     */
    public static final String PRECISION = "Precision";

    /**
     * 事件KEY值
     */
    public static final String EVENT_KEY = "EventKey";

    /**
     * 扫描信息
     */
    public static final String SCAN_CODE_INFO = "ScanCodeInfo";

    /**
     * 扫描类型，一般是qrcode
     */
    public static final String SCAN_TYPE = "ScanType";

    /**
     * 扫描结果，即二维码对应的字符串信息
     */
    public static final String SCAN_RESULT = "ScanResult";

    /**
     * 发送的图片信息
     */
    public static final String SEND_PICS_INFO = "SendPicsInfo";

    /**
     * 发送的图片数量
     */
    public static final String SEND_PICS_COUNT = "Count";

    /**
     * 图片列表
     */
    public static final String PIC_LIST = "PicList";

    /**
     * 图片的MD5值，开发者若需要，可用于验证接收到图片
     */
    public static final String PIC_MD5_SUM = "PicMd5Sum";

    /**
     * 发送的位置信息
     */
    public static final String SEND_LOCATION_INFO = "SendLocationInfo";

    /**
     * 朋友圈POI的名字，可能为空
     */
    public static final String POI_NAME = "Poiname";

    /**
     * 异步任务id，最大长度为64字符
     */
    public static final String JOB_ID = "JobId";

    /**
     * 操作类型，字符串，目前分别有： 1. sync_user(增量更新成员) 2. replace_user(全量覆盖成员) 3.
     * invite_user(邀请成员关注) 4. replace_party(全量覆盖部门)
     */
    public static final String JOB_TYPE = "JobType";

    /**
     * 返回码
     */
    public static final String ERR_CODE = "ErrCode";

    /**
     * 对返回码的文本描述内容
     */
    public static final String ERR_MSG = "ErrMsg";

}
