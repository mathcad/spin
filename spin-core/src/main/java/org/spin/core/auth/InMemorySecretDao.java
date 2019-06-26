package org.spin.core.auth;

import org.spin.core.ErrorCode;
import org.spin.core.throwable.SimplifiedException;
import org.spin.core.util.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 内存中的认证信息存储
 * Created by xuweinan on 2017/3/2.
 *
 * @author xuweinan
 */
public class InMemorySecretDao implements SecretDao {

    /**
     * identifier, tokenSet
     */
    private final Map<String, Set<TokenInfo>> userTokenCache = new ConcurrentHashMap<>(512);

    /**
     * token -&gt; TokenInfo
     */
    private final Map<String, TokenInfo> tokenCache = new ConcurrentHashMap<>();

    /**
     * identifier, keySet
     */
    private final Map<String, Set<KeyInfo>> userKeyCache = new ConcurrentHashMap<>();

    /**
     * key -&gt; KeyInfo
     */
    private final Map<String, KeyInfo> keyCache = new ConcurrentHashMap<>();
    private final Map<String, String> invalidKeyCache = new ConcurrentHashMap<>();

    @Override
    public String getIdentifierByToken(String tokenStr) {
        TokenInfo tokenInfo = tokenCache.get(StringUtils.trimToEmpty(tokenStr));
        if (null != tokenInfo) {
            return tokenInfo.getIdentifier();
        }
        return null;
    }

    @Override
    public String getIdentifierByKey(String keyStr) {
        checkKeyStr(keyStr);
        KeyInfo keyInfo = keyCache.get(StringUtils.trimToEmpty(keyStr));
        if (null != keyInfo) {
            return keyInfo.getIdentifier();
        }
        return null;
    }

    @Override
    public Set<TokenInfo> getTokenByIdentifier(String identifier) {
        return userTokenCache.get(identifier);
    }

    @Override
    public TokenInfo getTokenInfoByToken(String tokenStr) {
        return tokenCache.get(StringUtils.trimToEmpty(tokenStr));
    }

    @Override
    public Set<TokenInfo> getTokenByKey(String keyStr) {
        String identifier = getIdentifierByKey(keyStr);
        if (StringUtils.isEmpty(identifier)) {
            throw new SimplifiedException(ErrorCode.SECRET_INVALID);
        }
        return userTokenCache.get(identifier).stream().filter(t -> t.getSourceKey().equals(keyStr)).collect(Collectors.toSet());
    }

    @Override
    public Set<KeyInfo> getKeyByIdentifier(String identifier) {
        return userKeyCache.get(identifier);
    }

    @Override
    public KeyInfo getKeyInfoByKey(String keyStr) {
        checkKeyStr(keyStr);
        return keyCache.get(StringUtils.trimToEmpty(keyStr));
    }

    @Override
    public TokenInfo saveToken(String identifier, String tokenStr, String sourceKey) {
        TokenInfo tokenInfo = new TokenInfo(identifier, tokenStr, sourceKey);
        return saveToken(tokenInfo);
    }

    @Override
    public TokenInfo saveToken(TokenInfo tokenInfo) {
        String identifier = tokenInfo.getIdentifier();
        if (userTokenCache.containsKey(identifier)) {
            synchronized (userTokenCache.get(identifier)) {
                userTokenCache.get(identifier).add(tokenInfo);
            }
            tokenCache.put(tokenInfo.getToken(), tokenInfo);
        } else {
            synchronized (userTokenCache) {
                Set<TokenInfo> ts = new HashSet<>();
                ts.add(tokenInfo);
                userTokenCache.put(identifier, ts);
            }
        }
        tokenCache.put(tokenInfo.getToken(), tokenInfo);
        return tokenInfo;
    }

    @Override
    public KeyInfo saveKey(String identifier, String key, String secret, String secretType, Long generateTime) {
        KeyInfo keyInfo = new KeyInfo(identifier, key, secret, secretType, generateTime);
        return saveKey(keyInfo);
    }

    @Override
    public KeyInfo saveKey(KeyInfo keyInfo) {
        String identifier = keyInfo.getIdentifier();
        if (userKeyCache.containsKey(identifier)) {
            synchronized (userKeyCache.get(identifier)) {
                userKeyCache.get(identifier).add(keyInfo);
            }
        } else {
            synchronized (userKeyCache.get(identifier)) {
                Set<KeyInfo> ks = new HashSet<>();
                ks.add(keyInfo);
                userKeyCache.put(identifier, ks);
            }
        }
        keyCache.put(keyInfo.getKey(), keyInfo);
        return keyInfo;
    }

    @Override
    public Set<TokenInfo> removeToken(String identifier) {
        Set<TokenInfo> remove = userTokenCache.remove(identifier);
        remove.forEach(it -> tokenCache.remove(it.getToken()));
        return remove;
    }

    @Override
    public TokenInfo removeTokenByTokenStr(String tokenStr) {
        TokenInfo tokenInfo = getTokenInfoByToken(StringUtils.trimToEmpty(tokenStr));
        return removeToken(tokenInfo);
    }

    @Override
    public TokenInfo removeToken(TokenInfo tokenInfo) {
        if (null != tokenInfo) {
            synchronized (userTokenCache.get(tokenInfo.getIdentifier())) {
                userTokenCache.get(tokenInfo.getIdentifier()).remove(tokenInfo);
            }
            tokenCache.remove(tokenInfo.getToken());
        }
        return tokenInfo;
    }

    @Override
    public Set<KeyInfo> removeKey(String identifier) {
        removeToken(identifier);
        Set<KeyInfo> invalidKeys = userKeyCache.remove(identifier);
        synchronized (keyCache) {
            for (KeyInfo k : invalidKeys) {
                invalidKeyCache.put(k.getKey(), k.getKey());
                keyCache.remove(k.getKey());
            }
        }
        return invalidKeys;
    }

    @Override
    public KeyInfo removeKeyByKey(String keyStr) {
        KeyInfo keyInfo = getKeyInfoByKey(keyStr);
        if (null == keyInfo) {
            throw new SimplifiedException(ErrorCode.SECRET_INVALID);
        }
        return removeKey(keyInfo);
    }

    @Override
    public KeyInfo removeKey(KeyInfo keyInfo) {
        invalidKeyCache.put(keyInfo.getKey(), keyInfo.getKey());
        synchronized (userKeyCache.get(keyInfo.getIdentifier())) {
            userKeyCache.get(keyInfo.getIdentifier()).remove(keyInfo);
        }
        keyCache.remove(keyInfo.getKey());

        // 收集并移除由失效的key生成的token
        Set<TokenInfo> invalidTokens = userTokenCache.get(keyInfo.getIdentifier()).stream()
            .filter(it -> it.getSourceKey().equals(keyInfo.getKey()))
            .collect(Collectors.toSet());
        synchronized (userTokenCache.get(keyInfo.getIdentifier())) {
            invalidTokens.forEach(t -> {
                    userTokenCache.get(t.getIdentifier()).remove(t);
                    tokenCache.remove(t.getToken());
                }
            );
        }
        return keyInfo;
    }

    @Override
    public List<TokenInfo> collectExpiredToken(Long expiredIn) {
        return tokenCache.values().stream().filter(t -> isTimeOut(t.getGenerateTime(), expiredIn)).collect(Collectors.toList());
    }

    @Override
    public void clearExpiredToken(Long expriedIn) {
        List<TokenInfo> expired = collectExpiredToken(expriedIn);
        synchronized (userTokenCache) {
            expired.forEach(t -> {
                userTokenCache.get(t.getIdentifier()).remove(t);
                tokenCache.remove(t.getToken());
            });
        }
    }

    @Override
    public List<KeyInfo> collectExpiredKey(Long expiredIn) {
        return keyCache.values().stream().filter(t -> isTimeOut(t.getGenerateTime(), expiredIn)).collect(Collectors.toList());
    }

    @Override
    public void clearExpiredKey(Long expriedIn) {
        List<KeyInfo> expired = collectExpiredKey(expriedIn);
        synchronized (userKeyCache) {
            expired.forEach(k -> {
                userKeyCache.get(k.getIdentifier()).remove(k);
                keyCache.remove(k.getKey());
            });
        }
    }

    private void checkKeyStr(String keyStr) {
        if (invalidKeyCache.containsKey(keyStr)) {
            throw new SimplifiedException(ErrorCode.SECRET_INVALID);
        }
    }

    private boolean isTimeOut(Long generateTime, Long expiredIn) {
        return (System.currentTimeMillis() - generateTime) > expiredIn;
    }
}
