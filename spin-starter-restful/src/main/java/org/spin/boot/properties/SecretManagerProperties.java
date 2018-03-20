package org.spin.boot.properties;

import org.spin.core.security.RSA;
import org.spin.core.util.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * <p>Created by xuweinan on 2017/9/16.</p>
 *
 * @author xuweinan
 */
@ConfigurationProperties(prefix = "spin.secret")
public class SecretManagerProperties {
    private String publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCdyKzf1RUozDVoVdbw6ZuYC+eFTLjY3wnkyzcVmdnkpEHXy9D0VUs/wqEZKxO3AuLxcV8QwOxtsBjkfzIKIxk29P5JWBhyAuXKRQQooUv8iB3ncN8eK3tpHmawH3a0TgnSPg+3bD37uLqRc2ENEY0qUiBhQkoa0uDMj6/013HJOwIDAQAB";
    private String privateKey = "MIICeAIBADANBgkqhkiG9w0BAQEFAASCAmIwggJeAgEAAoGBAJ3IrN/VFSjMNWhV1vDpm5gL54VMuNjfCeTLNxWZ2eSkQdfL0PRVSz/CoRkrE7cC4vFxXxDA7G2wGOR/MgojGTb0/klYGHIC5cpFBCihS/yIHedw3x4re2keZrAfdrROCdI+D7dsPfu4upFzYQ0RjSpSIGFCShrS4MyPr/TXcck7AgMBAAECgYEAibDAo8gIYgTqqnUWUEAcRvBEhv/v41moAaARHumWyz9IMjAr1bzFIQwQl60O1EtRjk9YHX+uEv50ipoxKcV9TyfxpBrgZpWkll3JErkNEqt5yepXrPwedY4jYzJiVs2Cnar2hyUTaIphzZqL1uoeeX+0uwexxOLtBKYjNiHNZcECQQDT8nAjM1oTJU/FLFosEsbPfDZmzuCnW6baKBbLX1AHtlHgwcdLta8GX47ApOaZngR57veeOGyN13Sch7+i3EahAkEAvpRAmu2i9YQ4L9ZfLsp6aA/ZjHCd09ttG65jA9NiLjX62pfcyFgAt9eCCU/sBKEWraCl650QPN9OAecCppvuWwJBAIjuV/6V/bri3z+vIO7ajrGcOXWAcOoPL6RAREHOaWEiLJH9/+ltDxAaCptxrj5PNeslNbt2DsQxD/jVRz1L/SECQQCQ4WeT4CBIgXGtfEzz513TCmmaSGrTijaSGqqPV/2Fn+fKkjR34d75482pgqashkIVUNGSIt8bR6+n5pSvUE+NAkBMfjDWrO41NOjwC1HcmDO5cFuUhctAnad6GxIWHLiEn/u17YZkXCTRoVCh5SSuAYpOzbiwbx/c63kO6E6eH5rX";

    private PublicKey rsaPubkey;

    private PrivateKey rsaPrikey;

    /**
     * token过期时间
     */
    private String tokenExpireTime = "2h";

    /**
     * key过期时间
     */
    private String keyExpireTime = "15d";

    @PostConstruct
    public void init() {
        if (StringUtils.isNotEmpty(publicKey)) {
            rsaPubkey = RSA.getRSAPublicKey(publicKey);
        }
        if (StringUtils.isNotEmpty(privateKey)) {
            rsaPrikey = RSA.getRSAPrivateKey(privateKey);
        }
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public PublicKey getRsaPubkey() {
        return rsaPubkey;
    }

    public PrivateKey getRsaPrikey() {
        return rsaPrikey;
    }

    public String getTokenExpireTime() {
        return tokenExpireTime;
    }

    public void setTokenExpireTime(String tokenExpireTime) {
        this.tokenExpireTime = tokenExpireTime;
    }

    public String getKeyExpireTime() {
        return keyExpireTime;
    }

    public void setKeyExpireTime(String keyExpireTime) {
        this.keyExpireTime = keyExpireTime;
    }
}
