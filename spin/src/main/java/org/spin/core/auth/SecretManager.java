package org.spin.core.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.ErrorCode;
import org.spin.core.SpinContext;
import org.spin.core.security.RSA;
import org.spin.core.throwable.SimplifiedException;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.UUID;

/**
 * 认证信息管理类
 * Created by xuweinan on 2017/3/27.
 *
 * @author xuweinan
 */
public class SecretManager {
    private static final Logger logger = LoggerFactory.getLogger(SecretManager.class);
    /**
     * 分隔符
     */
    private static final String SEPARATOR = "\n";
    private static PublicKey RSA_PUBKEY;
    private static PrivateKey RSA_PRIKEY;

    private SecretDao secretDao;

    public SecretManager(SecretDao secretDao) {
        this.secretDao = secretDao;
    }

    /**
     * 清除过期认证信息
     */
    public void clearTokenKeyCache() {
        secretDao.clearExpiredKey(SpinContext.KeyExpireTime);
        secretDao.clearExpiredToken(SpinContext.TokenExpireTime);
    }

    /**
     * 从密钥中获取用户信息
     */
    public KeyInfo getKeyInfo(String key) {
        KeyInfo keyInfo = secretDao.getKeyInfoByKey(key);
        if (null == keyInfo) {
            try {
                String[] info = RSA.decrypt(RSA_PRIKEY, key).split(SEPARATOR);
                keyInfo = new KeyInfo(info[0], key, info[1], Long.parseLong(info[2]));
                if (isTimeOut(keyInfo.getGenerateTime(), SpinContext.KeyExpireTime)) {
                    throw new SimplifiedException(ErrorCode.SECRET_EXPIRED);
                } else {
                    return secretDao.saveKey(keyInfo);
                }
            } catch (Exception e) {
                logger.debug("Extract info from key Error: {}", e);
                throw new SimplifiedException(ErrorCode.SECRET_INVALID);
            }
        } else {
            if (isTimeOut(keyInfo.getGenerateTime(), SpinContext.KeyExpireTime)) {
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
        if (isTimeOut(generateTime, SpinContext.TokenExpireTime)) {
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
            key = RSA.encrypt(RSA_PUBKEY, ecodeStr);
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

    public static void setRsaPubkey(PublicKey rsaPubkey) {
        RSA_PUBKEY = rsaPubkey;
    }

    public static void setRsaPrikey(PrivateKey rsaPrikey) {
        RSA_PRIKEY = rsaPrikey;
    }

    private boolean isTimeOut(Long generateTime, Long expiredIn) {
        return (System.currentTimeMillis() - generateTime) > expiredIn;
    }
}