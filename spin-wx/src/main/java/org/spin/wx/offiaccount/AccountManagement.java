package org.spin.wx.offiaccount;

import org.spin.core.gson.reflect.TypeToken;
import org.spin.wx.WxUrl;

/**
 * 账号管理
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2021/12/13</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class AccountManagement {
    /**
     * 设置所属行业
     * {
     * "industry_id1":"1",
     * "industry_id2":"4"
     * }
     */
    WxUrl<Void, Void> CREATE_MENU = new WxUrl<>("https://api.weixin.qq.com/cgi-bin/menu/create?access_token=%s", new TypeToken<Void>() {
    });

}
