package org.spin.sys;

import org.spin.security.RSA;
import org.spin.util.RandomStringUtils;
import org.spin.util.StringUtils;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Token与密钥管理类
 *
 * @author X
 */
public class TokenKeyManager {
    private static final Map<String, TokenInfo> TOKEN_INFO_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, String> USERID_TOKEN_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, String> KEY_USERID_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, String> USERID_KEY_CACHE = new ConcurrentHashMap<>();

    private static PublicKey RSA_PUBKEY;
    private static PrivateKey RSA_PRIKEY;

    /**
     * 分隔符
     */
    private static final String SEPARATOR = "~~~";

    public static void clearTokenKeyCache() {
        if (TOKEN_INFO_CACHE.isEmpty()) {
            return;
        }
        TOKEN_INFO_CACHE.entrySet().stream()
                .filter(i -> (System.currentTimeMillis() - i.getValue().getGenerateTime()) > EnvCache.TokenExpireTime)
                .forEach(i -> {
                    USERID_TOKEN_CACHE.remove(i.getValue().getUserId());
                    TOKEN_INFO_CACHE.remove(i.getKey());
                });
    }

    /**
     * 从密钥中获取用户信息
     */
    public static String[] getKeyInfo(String key) throws Exception {
        return RSA.decrypt(RSA_PRIKEY, key).split(SEPARATOR);
    }

    /**
     * 判断token是否有效
     *
     * @param token token
     * @return null 无效 userId 正常
     */
    public static Object validateToken(String token) {
        if (StringUtils.isEmpty(token))
            return null;
        TokenInfo tokenInfo = TOKEN_INFO_CACHE.get(token);
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
    public static String generateToken(String key) {
        String userId = KEY_USERID_CACHE.get(key);
        if (userId != null) {
            String oldToken = USERID_TOKEN_CACHE.get(userId);
            String token = RandomStringUtils.randomAlphanumeric(32);
            TokenInfo info = new TokenInfo(userId);
            TOKEN_INFO_CACHE.put(token, info);
            USERID_TOKEN_CACHE.put(userId, token);
            if (StringUtils.isNotBlank(oldToken))
                TOKEN_INFO_CACHE.remove(oldToken);
            return token;
        }
        return null;
    }

    public static String generateKey(String userId, String pwd) {
        String ecodeStr = userId + SEPARATOR + pwd + SEPARATOR + System.currentTimeMillis();

        // 生成密钥
        String key = "";
        try {
            key = RSA.encrypt(RSA_PUBKEY, ecodeStr);
        } catch (Exception ignore) {
        }
        // 清理密钥缓存
        if (USERID_KEY_CACHE.containsKey(userId)) {
            KEY_USERID_CACHE.remove(USERID_KEY_CACHE.get(userId));
            USERID_KEY_CACHE.remove(userId);
        }
        // 添加密钥缓存
        USERID_KEY_CACHE.put(userId, key);
        KEY_USERID_CACHE.put(key, userId);
        return key;
    }

    private static boolean isTimeOut(Long generateTime) {
        return (System.currentTimeMillis() - generateTime) > EnvCache.TokenExpireTime;
    }
}