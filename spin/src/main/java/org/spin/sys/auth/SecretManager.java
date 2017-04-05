package org.spin.sys.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.security.RSA;
import org.spin.sys.EnvCache;
import org.spin.sys.ErrorCode;
import org.spin.throwable.SimplifiedException;
import org.spin.util.RandomStringUtils;
import org.spin.util.StringUtils;

import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * Created by xuweinan on 2017/3/27.
 *
 * @author xuweinan
 */
public class SecretManager {
    private static final Logger logger = LoggerFactory.getLogger(SecretManager.class);

    private SecretDao secretDao;

    /**
     * 分隔符
     */
    private static final String SEPARATOR = "~~~";

    private static PublicKey RSA_PUBKEY;
    private static PrivateKey RSA_PRIKEY;

    public void clearTokenKeyCache() {
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
            } catch (Exception e) {
                logger.debug("Extract info from key Error: {}", e);
                throw new SimplifiedException(ErrorCode.SECRET_INVALID);
            }
        }
        return keyInfo;
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
        if (isTimeOut(generateTime, EnvCache.TokenExpireTime)) {
            throw new SimplifiedException(ErrorCode.TOKEN_EXPIRED);
        } else {
            return tokenInfo.getIdentifier();
        }
    }

    /**
     * 通过密钥换取token
     *
     * @return 返回token, 如果key无效，抛出异常
     */
    public TokenInfo generateToken(String key, boolean forceNew) {
        String id = secretDao.getIdentifierByKey(key);
        // 检查key是否合法
        if (StringUtils.isEmpty(id)) {
            throw new SimplifiedException(ErrorCode.SECRET_INVALID);
        }

        TokenInfo tokenInfo = secretDao.getTokenByIdentifier(id);
        // 如果需要强制更新token，token不存在或者已过期，重新生成token并存储
        if (forceNew || null == tokenInfo || isTimeOut(tokenInfo.getGenerateTime(), EnvCache.TokenExpireTime)) {
            String token = RandomStringUtils.randomAlphanumeric(32);
            tokenInfo = secretDao.saveToken(id, token);
        }
        return tokenInfo;
    }

    public KeyInfo generateKey(String userId, String pwd, boolean forceNew) {
        KeyInfo keyInfo = secretDao.getKeyByIdentifier(userId);

        // 如果需要强制更新key，key不存在或者已过期，重新生成key并存储
        if (forceNew || null == keyInfo || isTimeOut(keyInfo.getGenerateTime(), EnvCache.KeyExpireTime)) {
            Long generateTime = System.currentTimeMillis();
            String ecodeStr = userId + SEPARATOR + pwd + SEPARATOR + generateTime;
            // 生成密钥
            String key;
            try {
                key = RSA.encrypt(RSA_PUBKEY, ecodeStr);
            } catch (Exception ignore) {
                throw new SimplifiedException(ErrorCode.ENCRYPT_FAIL);
            }

            keyInfo = secretDao.saveKey(userId, key, pwd, generateTime);
        }
        return keyInfo;
    }

    private boolean isTimeOut(Long generateTime, Long expiredIn) {
        return (System.currentTimeMillis() - generateTime) > expiredIn;
    }
}
