package org.spin.wx;

import org.spin.wx.aes.AesException;

import java.util.Base64;

/**
 * 微信配置信息
 * <p>Created by xuweinan on 2017/11/21.</p>
 *
 * @author xuweinan
 */
public class WxConfigInfo {
    /**
     * appID
     */
    private String appId;

    /**
     * appSecret
     */
    private String appSecret;

    /**
     * 应用Token
     */
    private String token;

    /**
     * 应用AES秘钥
     */
    private byte[] encodingAesKey;

    /**
     * 商户号
     */
    private String payMchId;

    /**
     * 商户密钥
     */
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

    public void setEncodingAesKey(String encodingAesKey) throws AesException {
        if (encodingAesKey.length() != 43) {
            throw new AesException(AesException.IllegalAesKey);
        }
        this.encodingAesKey = Base64.getDecoder().decode(encodingAesKey + "=");
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
