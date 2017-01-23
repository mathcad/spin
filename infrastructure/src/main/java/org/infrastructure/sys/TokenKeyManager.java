package org.infrastructure.sys;

import org.infrastructure.security.RSA;
import org.infrastructure.util.RandomStringUtils;
import org.infrastructure.util.StringUtils;
import org.springframework.stereotype.Component;

/**
 * Token与密钥管理类
 *
 * @author X
 */
@Component
public class TokenKeyManager {

    /**
     * 分隔符
     */
    private static final String SEPARATOR = "@";

    public static void clearTokenKeyCache() {
        if (EnvCache.TOKEN_INFO_CACHE.isEmpty()) {
            return;
        }
        EnvCache.TOKEN_INFO_CACHE.entrySet().stream()
                .filter(i -> (System.currentTimeMillis() - i.getValue().getGenerateTime()) > EnvCache.TokenExpireTime)
                .forEach(i -> {
                    EnvCache.USERID_TOKEN_CACHE.remove(i.getValue().getUserId());
                    EnvCache.TOKEN_INFO_CACHE.remove(i.getKey());
                });
    }

    /**
     * 从密钥中获取用户信息
     */
    public static String[] getKeyInfo(String key) throws Exception {
        return RSA.decrypt(EnvCache.RSA_PRIKEY, key).split(SEPARATOR);
    }

    /**
     * 判断token是否有效
     *
     * @param token token
     * @return null 无效 userId 正常
     */
    public Object validateToken(String token) {
        if (StringUtils.isEmpty(token))
            return null;
        TokenInfo tokenInfo = EnvCache.TOKEN_INFO_CACHE.get(token);
        // token不存在，返回-1
        if (tokenInfo == null)
            return null;
        Long generateTime = tokenInfo.getGenerateTime();
        // token过期，返回-1，否则返回token对应的UserId
        if (isTimeOut(generateTime)) {
            return null;
        } else {
            return tokenInfo.getUserId();
        }
    }

    /**
     * 通过密钥换取token
     *
     * @return 返回token, 如果key无效，返回null
     */
    public String generateToken(String key) {
        Long userId = EnvCache.KEY_USERID_CACHE.get(key);
        if (userId != null) {
            String oldToken = EnvCache.USERID_TOKEN_CACHE.get(userId);
            String token = RandomStringUtils.randomAlphanumeric(32);
            TokenInfo info = new TokenInfo(userId);
            EnvCache.TOKEN_INFO_CACHE.put(token, info);
            EnvCache.USERID_TOKEN_CACHE.put(userId, token);
            if (StringUtils.isNotBlank(oldToken))
                EnvCache.TOKEN_INFO_CACHE.remove(oldToken);
            return token;
        }
        return null;
    }

    public String generateKey(Long userId, String pwd) {
        String ecodeStr = userId + SEPARATOR + pwd + SEPARATOR + System.currentTimeMillis();

        // 生成密钥
        String key = "";
        try {
            key = RSA.encrypt(EnvCache.RSA_PUBKEY, ecodeStr);
        } catch (Exception ignore) {
        }
        // 清理密钥缓存
        if (EnvCache.USERID_KEY_CACHE.containsKey(userId)) {
            EnvCache.KEY_USERID_CACHE.remove(EnvCache.USERID_KEY_CACHE.get(userId));
            EnvCache.USERID_KEY_CACHE.remove(userId);
        }
        // 添加密钥缓存
        EnvCache.USERID_KEY_CACHE.put(userId, key);
        EnvCache.KEY_USERID_CACHE.put(key, userId);
        return key;
    }

    private boolean isTimeOut(Long generateTime) {
        return (System.currentTimeMillis() - generateTime) > EnvCache.TokenExpireTime;
    }
}