package org.spin.wx.offiaccount;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.ErrorCode;
import org.spin.core.gson.annotation.SerializedName;
import org.spin.core.gson.reflect.TypeToken;
import org.spin.core.throwable.SimplifiedException;
import org.spin.core.trait.FriendlyEnum;
import org.spin.core.util.http.Http;
import org.spin.wx.*;

import java.util.List;

/**
 * 自定义菜单
 * <p>https://developers.weixin.qq.com/doc/offiaccount/Custom_Menus/Creating_Custom-Defined_Menu.html</p>
 * <p>Created by xuweinan on 2021/12/13</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class CustomMenus {
    private static final Logger logger = LoggerFactory.getLogger(CustomMenus.class);
    /**
     * 自定义菜单
     * {
     * "industry_id1":"1",
     * "industry_id2":"4"
     * }
     */
    private static final WxUrl<MenuCreateFormVo, WxResponse<Void>> CREATE_MENU = new WxUrl<>("https://api.weixin.qq.com/cgi-bin/menu/create?access_token=%s", new TypeToken<WxResponse<Void>>() {
    });

    public static void customMenu(MenuCreateFormVo menuVo, String... configName) {
        AccessToken accessToken = WxTokenManager.getToken(WxHelper.extractConfigName(configName));
        WxResponse<Void> res;
        try {
            res = Http.POST.withUrl(CREATE_MENU.format(accessToken.getToken())).withJsonBody(menuVo).execute(CREATE_MENU.getResultType());
        } catch (Exception e) {
            logger.error("自定义菜单异常", e);
            throw new SimplifiedException(ErrorCode.NETWORK_EXCEPTION, "自定义菜单异常", e);
        }
        if (!res.isOk()) {
            throw res.toException();
        }
    }
}

class MenuCreateFormVo {
    /**
     * 必填
     * <p>一级菜单数组，个数应为1~3个</p>
     */
    private List<MenuButton> button;

    public List<MenuButton> getButton() {
        return button;
    }

    public void setButton(List<MenuButton> button) {
        this.button = button;
    }
}

class MenuButton {

    /**
     * 必填
     * <p>菜单的响应动作类型，view表示网页类型，click表示点击类型，miniprogram表示小程序类型</p>
     */
    private MenuType type;

    /**
     * 必填
     * <p>菜单标题，不超过16个字节，子菜单不超过60个字节</p>
     */
    private String name;

    /**
     * click等点击类型必须
     * <p>菜单KEY值，用于消息接口推送，不超过128字节</p>
     */
    private String key;

    /**
     * view、miniprogram类型必须
     * <p>网页 链接，用户点击菜单可打开链接，不超过1024字节。 type为miniprogram时，不支持小程序的老版本客户端将打开本url。</p>
     */
    private String url;

    /**
     * media_id类型和view_limited类型必须
     * <p>调用新增永久素材接口返回的合法media_id</p>
     */
    @SerializedName("media_id")
    private String mediaId;

    /**
     * miniprogram类型必须
     * <p>小程序的appid（仅认证公众号可配置）</p>
     */
    private String appid;

    /**
     * miniprogram类型必须
     * <p>小程序的页面路径</p>
     */
    private String pagepath;

    /**
     * article_id类型和article_view_limited类型必须
     * <p>发布后获得的合法 article_id</p>
     */
    private String article_id;

    /**
     * 非必填
     * <p>发布后获得的合法 article_id</p>
     */
    @SerializedName("sub_button")
    private List<MenuButton> subButton;

    public MenuType getType() {
        return type;
    }

    public void setType(MenuType type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMediaId() {
        return mediaId;
    }

    public void setMediaId(String mediaId) {
        this.mediaId = mediaId;
    }

    public String getAppid() {
        return appid;
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }

    public String getPagepath() {
        return pagepath;
    }

    public void setPagepath(String pagepath) {
        this.pagepath = pagepath;
    }

    public String getArticle_id() {
        return article_id;
    }

    public void setArticle_id(String article_id) {
        this.article_id = article_id;
    }

    public List<MenuButton> getSubButton() {
        return subButton;
    }

    public void setSubButton(List<MenuButton> subButton) {
        this.subButton = subButton;
    }
}

enum MenuType implements FriendlyEnum<String> {
    VALUE("value", "点击类型"),
    VIEW("view", "网页类型"),
    SCANCODE_PUSH("scancode_push", "扫码事件"),
    SCANCODE_WAITMSG("scancode_waitmsg", "扫码推事件且弹出“消息接收中”提示框"),
    PIC_SYSPHOTO("pic_sysphoto", "弹出系统拍照发图"),
    PIC_PHOTO_OR_ALBUM("pic_photo_or_album", "弹出拍照或者相册发图"),
    PIC_WEIXIN("pic_weixin", "弹出微信相册发图器"),
    LOCATION_SELECT("location_select", "弹出地理位置选择器"),
    MEDIA_ID("media_id", "下发消息（除文本消息）"),
    ARTICLE_ID("article_id", "图文消息"),
    ARTICLE_VIEW_LIMITED("article_view_limited", "类似 view_limited，但不使用 media_id 而使用 article_id"),
    MINIPROGRAM("miniprogram", "小程序类型"),
    ;

    private final String value;
    private final String desc;

    MenuType(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    @Override
    public String getValue() {
        return value;
    }


    @Override
    public String getDescription() {
        return desc;
    }
}
