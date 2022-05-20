package org.spin.wx.offiaccount;

import org.spin.core.gson.reflect.TypeToken;
import org.spin.wx.WxUrl;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2021/12/13</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public interface WxUrls {


    /**
     * 设置所属行业
     * {
     * "industry_id1":"1",
     * "industry_id2":"4"
     * }
     */
    WxUrl<Void, Void> SET_INDUSTRY = new WxUrl<>("https://api.weixin.qq.com/cgi-bin/template/api_set_industry?access_token=%s", new TypeToken<Void>() {
    });

    /**
     * 获取模板ID
     */
//    WxUrl TMPL_ID = new WxUrl("https://api.weixin.qq.com/cgi-bin/template/api_add_template?access_token=%s", resultType);
}
