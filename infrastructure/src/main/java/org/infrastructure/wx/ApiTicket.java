package org.infrastructure.wx;

import org.infrastructure.sys.TypeIdentifier;
import org.infrastructure.throwable.SimplifiedException;
import org.infrastructure.util.HttpUtils;
import org.infrastructure.util.JSONUtils;
import org.infrastructure.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
                    result = HttpUtils.httpGetRequest("https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token={}&type=jsapi", token);
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
        Map<String, String> resMap = JSONUtils.fromJson(json, type);
        if (resMap.containsKey("ticket")) {
            tmp.setExpiresIn(Integer.parseInt(resMap.get("expires_in")));
            tmp.jsapiTicket = resMap.get("ticket");
            if (logger.isDebugEnabled())
                logger.debug("Current ApiTicket is: {}, expired since: {}", tmp.jsapiTicket, new Date(tmp.getExpiredSince()));
            return tmp;
        }
        throw new SimplifiedException("获取jsapi_ticket失败:[" + json + "]");
    }
}