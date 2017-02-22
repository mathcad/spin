package org.spin.wx;

import org.spin.throwable.SimplifiedException;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 微信端基础配置
 * <p>
 * Created by xuweinan on 2016/9/28.
 *
 * @author xuweinan
 */
public class WxConfig {

    private static final Map<String, ConfigInfo> infos = new HashMap<>();

    public static ConfigInfo getConfig(String name) {
        ConfigInfo info = infos.get(name);
        if (null == info) {
            throw new SimplifiedException("There is no specified wx config");
        }
        return info;
    }

    public static void putConfig(String name, ConfigInfo configInfo) {
        infos.put(name, configInfo);
    }

    public static class ConfigInfo {
        /** appID */
        private String appId;

        /** appSecret */
        private String appSecret;

        /** 应用Token */
        private String token;

        /** 应用AES秘钥 */
        private byte[] encodingAesKey;

        /** 商户号 */
        private String payMchId;

        /** 商户密钥 */
        private String mchKey;

        public String getAppId() {
            return appId;
        }

        public void setAppId(String appId) {
            this.appId = appId;
        }

        public String getAppSecret() {
            return appSecret;
        }

        public void setAppSecret(String appSecret) {
            this.appSecret = appSecret;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public byte[] getEncodingAesKey() {
            return encodingAesKey;
        }

        public void setEncodingAesKey(String encodingAesKey) {
            this.encodingAesKey = Base64.getDecoder().decode(encodingAesKey + "=");
        }

        public void setEncodingAesKey(byte[] encodingAesKey) {
            this.encodingAesKey = encodingAesKey;
        }

        public String getPayMchId() {
            return payMchId;
        }

        public void setPayMchId(String payMchId) {
            this.payMchId = payMchId;
        }

        public String getMchKey() {
            return mchKey;
        }

        public void setMchKey(String mchKey) {
            this.mchKey = mchKey;
        }
    }
}