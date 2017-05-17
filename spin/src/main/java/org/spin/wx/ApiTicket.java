package org.spin.wx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.TypeIdentifier;
import org.spin.core.throwable.SimplifiedException;
import org.spin.core.util.HttpUtils;
import org.spin.core.util.JsonUtils;
import org.spin.core.util.StringUtils;
import org.spin.wx.wx.base.WxUrl;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 封装了微信的JSAPI_TICKET
 * <p>
 * Created by xuweinan on 2016/10/20.
 *
 * @author xuweinan
 */
public class ApiTicket {
    private static final Logger logger = LoggerFactory.getLogger(ApiTicket.class);
    private static final TypeIdentifier<HashMap<String, String>> type = new TypeIdentifier<HashMap<String, String>>() {
    };
    private static ApiTicket instance;
    private String jsapiTicket;
    private int expiresIn;
    private long expiredSince;

    private ApiTicket() {
    }

    public static ApiTicket getInstance() {
        if (instance == null || StringUtils.isEmpty(instance.jsapiTicket) || System.currentTimeMillis() > instance.getExpiredSince()) {
            synchronized (ApiTicket.class) {
                instance = new ApiTicket();
                String token = AccessToken.getDefaultInstance().getToken();
                String result;
                try {
                    result = HttpUtils.get(WxUrl.ApiTicketUrl.getUrl(token));
                } catch (Throwable e) {
                    throw new SimplifiedException("获取access_token失败", e);
                }
                instance = parseTicket(result);
                return instance;
            }
        }
        return instance;
    }

    public String getTicket() {
        return jsapiTicket;
    }

    public void setTicket(String ticket) {
        this.jsapiTicket = ticket;
    }

    public int getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(int expiresIn) {
        this.expiresIn = expiresIn;
        this.expiredSince = System.currentTimeMillis() + expiresIn * 900;
    }

    public long getExpiredSince() {
        return expiredSince;
    }

    private static ApiTicket parseTicket(String json) {
        ApiTicket tmp = new ApiTicket();
        Map<String, String> resMap = JsonUtils.fromJson(json, type);
        if (null != resMap && resMap.containsKey("ticket")) {
            tmp.setExpiresIn(Integer.parseInt(resMap.get("expires_in")));
            tmp.jsapiTicket = resMap.get("ticket");
            if (logger.isDebugEnabled())
                logger.debug("Current ApiTicket is: {}, expired since: {}", tmp.jsapiTicket, new Date(tmp.getExpiredSince()));
            return tmp;
        }
        throw new SimplifiedException("获取jsapi_ticket失败:[" + json + "]");
    }
}
