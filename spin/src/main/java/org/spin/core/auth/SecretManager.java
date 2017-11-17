package org.spin.core.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.ErrorCode;
import org.spin.core.security.RSA;
import org.spin.core.session.SessionManager;
import org.spin.core.throwable.SimplifiedException;
import org.spin.core.util.DateUtils;
import org.spin.core.util.StringUtils;
import org.spin.data.core.AbstractUser;

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

    /**
     * 分隔符
     */
    private static final String SEPARATOR = "\n";
    private PublicKey rsaPubkey;
    private PrivateKey rsaPrikey;

    /**
     * token过期时间,单位毫秒
     */
    private Long tokenExpiredIn = 7200000L;

    /**
     * key过期时间,单位毫秒
     */
    private Long keyExpiredIn = 1296000000L;

    private SecretDao secretDao;

    public SecretManager(SecretDao secretDao) {
        this.secretDao = secretDao;
    }

    /**
     * 清除过期认证信息
     */
    public void clearTokenKeyCache() {
        logger.info("Clear expired token and key...");
        secretDao.collectExpiredToken(tokenExpiredIn).forEach(t -> SessionManager.removeSession(t.getToken()));
        secretDao.collectExpiredKey(keyExpiredIn).forEach(k -> secretDao.getTokenByKey(k.getKey()).forEach(t -> SessionManager.removeSession(t.getToken())));
        secretDao.clearExpiredToken(tokenExpiredIn);
        secretDao.clearExpiredKey(keyExpiredIn);
        logger.info("Expired token and key was removed successful...");
    }

    /**
     * 获取token信息
     *
     * @param tokenStr token字符串
     * @return token信息
     */
    public TokenInfo getTokenInfo(String tokenStr) {
        TokenInfo tokenInfo = secretDao.getTokenInfoByToken(tokenStr);
        // token不存在
        if (tokenInfo == null)
            throw new SimplifiedException(ErrorCode.TOKEN_INVALID);
        Long generateTime = tokenInfo.getGenerateTime();
        // token过期，移除相关session并抛出异常，否则返回token对应的UserId
        if (DateUtils.isTimeOut(generateTime, tokenExpiredIn)) {
            SessionManager.removeSession(tokenStr);
            throw new SimplifiedException(ErrorCode.TOKEN_EXPIRED);
        } else {
            return tokenInfo;
        }
    }

    /**
     * 向当前线程绑定session
     *
     * @param tokenStr token字符串
     */
    public void bindCurrentSession(String tokenStr) {
        // 解析token
        TokenInfo tokenInfo = getTokenInfo(tokenStr);

        // 设置SessionId
        SessionManager.setCurrentSessionId(tokenStr);

        // 设置CurrentUser
        AbstractUser user = (AbstractUser) SessionManager.getCurrentSession().getAttribute(SessionManager.USER_SESSION_KEY);
        user.setSessionId(tokenStr);
        SessionManager.setCurrentUser(user);
    }

    /**
     * 从密钥中获取用户信息
     */
    public KeyInfo getKeyInfo(String keyStr) {
        KeyInfo keyInfo = secretDao.getKeyInfoByKey(keyStr);
        if (null == keyInfo) {
            try {
                String[] info = RSA.decrypt(rsaPrikey, keyStr).split(SEPARATOR);
                keyInfo = new KeyInfo(info[0], keyStr, info[1], info[2], Long.parseLong(info[3]));
                if (DateUtils.isTimeOut(keyInfo.getGenerateTime(), keyExpiredIn)) {
                    throw new SimplifiedException(ErrorCode.SECRET_EXPIRED);
                } else {
                    return secretDao.saveKey(keyInfo);
                }
            } catch (Exception e) {
                logger.debug("Extract info from key Error: {}", keyStr, e);
                throw new SimplifiedException(ErrorCode.SECRET_INVALID);
            }
        } else {
            if (DateUtils.isTimeOut(keyInfo.getGenerateTime(), keyExpiredIn)) {
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
     * @param tokenStr token
     * @return 正常情况返回userId，否则抛出异常
     */
    public String validateToken(String tokenStr) {
        return getTokenInfo(tokenStr).getIdentifier();
    }

    /**
     * 通过密钥换取token
     *
     * @param keyStr 密钥
     * @return 返回token, 如果key无效，抛出异常
     */
    public TokenInfo generateToken(String keyStr) {
        // 检查key是否合法
        KeyInfo keyInfo = getKeyInfo(keyStr);
        // 生成新token
        String token = UUID.randomUUID().toString();
        return secretDao.saveToken(keyInfo.getIdentifier(), token, keyStr);
    }

    /**
     * 通过密钥换取token，并继承原token的session
     *
     * @param keyStr   密钥
     * @param oldToken 原token
     * @return 返回token, 如果key无效，抛出异常
     */
    public TokenInfo generateToken(String keyStr, String oldToken) {
        // 检查key是否合法
        KeyInfo keyInfo = getKeyInfo(keyStr);
        // 生成新token
        String newToken = UUID.randomUUID().toString();
        SessionManager.extendSession(oldToken, newToken);
        invalidTokenByTokenStr(oldToken);
        return secretDao.saveToken(keyInfo.getIdentifier(), newToken, keyStr);
    }

    /**
     * 生成密钥
     *
     * @param userId     用户id
     * @param secret     密钥
     * @param secretType 密钥类型
     * @return KeyInfo
     */
    public KeyInfo generateKey(String userId, String secret, String secretType) {
        Long generateTime = System.currentTimeMillis();
        String ecodeStr = userId + SEPARATOR + secret + SEPARATOR + secretType + SEPARATOR + generateTime;
        // 生成密钥
        String key;
        try {
            key = RSA.encrypt(rsaPubkey, ecodeStr);
        } catch (Exception ignore) {
            throw new SimplifiedException(ErrorCode.ENCRYPT_FAIL);
        }
        return secretDao.saveKey(userId, key, secret, secretType, generateTime);
    }

    /**
     * 强制使token失效，并清除相关session
     *
     * @param identifier 用户标识
     */
    public void invalidToken(String identifier) {
        secretDao.removeToken(identifier).forEach(t -> SessionManager.removeSession(t.getToken()));
    }

    /**
     * 强制使token失效，并清除相关session
     *
     * @param tokenStr token字符串
     */
    public void invalidTokenByTokenStr(String tokenStr) {
        secretDao.removeTokenByTokenStr(tokenStr);
        SessionManager.removeSession(tokenStr);
    }

    /**
     * 强制使密钥失效
     *
     * @param identifier 用户标识
     */
    public void invalidKey(String identifier) {
        secretDao.removeToken(identifier).forEach(t -> SessionManager.removeSession(t.getToken()));
        secretDao.removeKey(identifier);
    }

    /**
     * 强制使密钥失效
     *
     * @param keyStr 密钥字符串
     */
    public void invalidKeyByKeyStr(String keyStr) {
        secretDao.removeKeyByKey(keyStr);
        secretDao.getTokenByKey(keyStr).forEach(t -> SessionManager.removeSession(t.getToken()));
    }

    /**
     * 当前用户登出
     */
    public void logout() {
        String tokenStr = SessionManager.getCurrentSessionId();
        SessionManager.logout();
        if (StringUtils.isNotEmpty(tokenStr)) {
            TokenInfo tokenInfo = getTokenInfo(tokenStr);
            invalidKey(tokenInfo.getSourceKey());
        }
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
