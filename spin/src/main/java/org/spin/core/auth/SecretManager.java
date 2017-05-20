package org.spin.core.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.ErrorCode;
import org.spin.core.SpinContext;
import org.spin.core.security.RSA;
import org.spin.core.throwable.SimplifiedException;
import org.spin.core.util.DateUtils;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.UUID;

/**
 * 认证信息管理类
 * Created by xuweinan on 2017/3/27.
 *
 * @author xuweinan
 */
public class SecretManager {
    private static final Logger logger = LoggerFactory.getLogger(SecretManager.class);

    /** 分隔符 */
    private static final String SEPARATOR = "\n";
    private PublicKey rsaPubkey;
    private PrivateKey rsaPrikey;

    /** token过期时间,单位毫秒 */
    private Long tokenExpiredIn = 7200000L;

    /** key过期时间,单位毫秒 */
    private Long keyExpiredIn = 1296000000L;

    private SecretDao secretDao;

    public SecretManager(SecretDao secretDao) {
        this.secretDao = secretDao;
    }

    /**
     * 清除过期认证信息
     */
    public void clearTokenKeyCache() {
        secretDao.clearExpiredKey(keyExpiredIn);
        secretDao.clearExpiredToken(tokenExpiredIn);
    }

    /**
     * 从密钥中获取用户信息
     */
    public KeyInfo getKeyInfo(String key) {
        KeyInfo keyInfo = secretDao.getKeyInfoByKey(key);
        if (null == keyInfo) {
            try {
                String[] info = RSA.decrypt(rsaPrikey, key).split(SEPARATOR);
                keyInfo = new KeyInfo(info[0], key, info[1], Long.parseLong(info[2]));
                if (DateUtils.isTimeOut(keyInfo.getGenerateTime(), SpinContext.KeyExpireTime)) {
                    throw new SimplifiedException(ErrorCode.SECRET_EXPIRED);
                } else {
                    return secretDao.saveKey(keyInfo);
                }
            } catch (Exception e) {
                logger.debug("Extract info from key Error: {}", e);
                throw new SimplifiedException(ErrorCode.SECRET_INVALID);
            }
        } else {
            if (DateUtils.isTimeOut(keyInfo.getGenerateTime(), SpinContext.KeyExpireTime)) {
                secretDao.removeKey(keyInfo);
                throw new SimplifiedException(ErrorCode.SECRET_EXPIRED);
            } else {
                return keyInfo;
            }
        }
    }

    /**
     * 判断token是否有效
     *
     * @param token token
     * @return 正常情况返回userId，否则抛出异常
     */
    public String validateToken(String token) {
        TokenInfo tokenInfo = secretDao.getTokenInfoByToken(token);
        // token不存在
        if (tokenInfo == null)
            throw new SimplifiedException(ErrorCode.TOKEN_INVALID);
        Long generateTime = tokenInfo.getGenerateTime();
        // token过期，否则返回token对应的UserId
        if (DateUtils.isTimeOut(generateTime, SpinContext.TokenExpireTime)) {
            throw new SimplifiedException(ErrorCode.TOKEN_EXPIRED);
        } else {
            return tokenInfo.getIdentifier();
        }
    }

    /**
     * 通过密钥换取token
     *
     * @param key 密钥
     * @return 返回token, 如果key无效，抛出异常
     */
    public TokenInfo generateToken(String key) {
        // 检查key是否合法
        KeyInfo keyInfo = getKeyInfo(key);
        // 生成新token
        String token = UUID.randomUUID().toString();
        return secretDao.saveToken(keyInfo.getIdentifier(), token, key);
    }

    /**
     * 生成密钥
     *
     * @param userId 用户id
     * @param pwd    密码摘要
     * @return KeyInfo
     */
    public KeyInfo generateKey(String userId, String pwd) {
        Long generateTime = System.currentTimeMillis();
        String ecodeStr = userId + SEPARATOR + pwd + SEPARATOR + generateTime;
        // 生成密钥
        String key;
        try {
            key = RSA.encrypt(rsaPubkey, ecodeStr);
        } catch (Exception ignore) {
            throw new SimplifiedException(ErrorCode.ENCRYPT_FAIL);
        }
        return secretDao.saveKey(userId, key, pwd, generateTime);
    }

    public void invalidToken(String identifier) {
        secretDao.removeToken(identifier);
    }

    public void invalidTokenByTokenStr(String tokenStr) {
        secretDao.removeTokenByTokenStr(tokenStr);
    }

    public void invalidKey(String identifier) {
        secretDao.removeKey(identifier);
    }

    public void invalidKeyByKeyStr(String keyStr) {
        secretDao.removeKeyByKey(keyStr);
    }

    public Long getTokenExpiredIn() {
        return tokenExpiredIn;
    }

    public void setTokenExpiredIn(Long tokenExpiredIn) {
        this.tokenExpiredIn = tokenExpiredIn;
    }

    public void setTokenExpiredIn(String tokenExpiredIn) {
        this.tokenExpiredIn = DateUtils.periodToMs(tokenExpiredIn);
    }

    public Long getKeyExpiredIn() {
        return keyExpiredIn;
    }

    public void setKeyExpiredIn(Long keyExpiredIn) {
        this.keyExpiredIn = keyExpiredIn;
    }

    public void setKeyExpiredIn(String keyExpiredIn) {
        this.keyExpiredIn = DateUtils.periodToMs(keyExpiredIn);
    }

    public void setRsaPubkey(PublicKey rsaPubkey) {
        this.rsaPubkey = rsaPubkey;
    }

    public void setRsaPrikey(PrivateKey rsaPrikey) {
        this.rsaPrikey = rsaPrikey;
    }

    public void setRsaPubkey(String rsaPubkeyStr) {
        try {
            this.rsaPubkey = RSA.getRSAPublicKey(rsaPubkeyStr);
        } catch (InvalidKeySpecException e) {
            logger.error("RSA公钥不合法", e);
            throw new SimplifiedException(ErrorCode.KEY_FAIL, "公钥不合法");
        }
    }

    public void setRsaPrikey(String rsaPrikeyStr) {
        try {
            this.rsaPrikey = RSA.getRSAPrivateKey(rsaPrikeyStr);
        } catch (InvalidKeySpecException e) {
            logger.error("RSA私钥不合法", e);
            throw new SimplifiedException(ErrorCode.KEY_FAIL, "RSA私钥不合法");
        }
    }
}
