package org.spin.wx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.TypeIdentifier;
import org.spin.core.throwable.SimplifiedException;
import org.spin.core.util.JsonUtils;

import java.util.Date;
import java.util.Map;

/**
 * 封装了微信的JSAPI_TICKET
 * <p>Created by xuweinan on 2016/10/20.</p>
 *
 * @author xuweinan
 */
public class ApiTicket {
    private static final Logger logger = LoggerFactory.getLogger(ApiTicket.class);

    private static final TypeIdentifier<Map<String, String>> type = new TypeIdentifier<Map<String, String>>() {
    };

    private String jsapiTicket;
    private int expiresIn = 0;
    private long expiredSince = 0L;

    public ApiTicket() {
    }

    public ApiTicket(String json) {
        Map<String, String> resMap = JsonUtils.fromJson(json, type);
        if (null != resMap && resMap.containsKey("ticket")) {
            this.setExpiresIn(Integer.parseInt(resMap.get("expires_in")));
            this.setTicket(resMap.get("ticket"));
            if (logger.isDebugEnabled())
                logger.debug("Current ApiTicket is: {}, expired since: {}", this.getTicket(), new Date(this.getExpiredSince()));
            return;
        }
        throw new SimplifiedException("获取jsapi_ticket失败:[" + json + "]");
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
}
