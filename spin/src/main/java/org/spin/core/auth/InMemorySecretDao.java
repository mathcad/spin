package org.spin.core.auth;

import org.spin.core.ErrorCode;
import org.spin.core.throwable.SimplifiedException;
import org.spin.core.util.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    private final Map<String, Set<TokenInfo>> tokenCache = new ConcurrentHashMap<>();
    private final Map<String, Set<KeyInfo>> keyCache = new ConcurrentHashMap<>();
    private final Map<String, String> invalidKeyCache = new ConcurrentHashMap<>();

    @Override
    public String getIdentifierByToken(String tokenStr) {
        return tokenCache.values().stream().filter(Objects::nonNull)
            .flatMap(ts -> ts.stream().filter(t -> t.getToken().equals(tokenStr)).map(TokenInfo::getIdentifier))
            .findFirst()
            .orElse(null);
    }

    @Override
    public String getIdentifierByKey(String keyStr) {
        checkKeyStr(keyStr);
        return keyCache.values().stream()
            .flatMap(ks -> ks.stream().filter(k -> k.getKey().equals(keyStr)).map(KeyInfo::getIdentifier))
            .findFirst()
            .orElse(null);
    }

    @Override
    public Set<TokenInfo> getTokenByIdentifier(String identifier) {
        return tokenCache.get(identifier);
    }

    @Override
    public TokenInfo getTokenInfoByToken(String tokenStr) {
        return tokenCache.values().stream()
            .flatMap(ts -> ts.stream().filter(t -> t.getToken().equals(tokenStr)))
            .findFirst()
            .orElse(null);
    }

    @Override
    public Set<TokenInfo> getTokenByKey(String keyStr) {
        String identifier = getIdentifierByKey(keyStr);
        if (StringUtils.isEmpty(identifier)) {
            throw new SimplifiedException(ErrorCode.SECRET_INVALID);
        }
        return tokenCache.get(identifier).stream().filter(t -> t.getSourceKey().equals(keyStr)).collect(Collectors.toSet());
    }

    @Override
    public Set<KeyInfo> getKeyByIdentifier(String identifier) {
        return keyCache.get(identifier);
    }

    @Override
    public KeyInfo getKeyInfoByKey(String keyStr) {
        checkKeyStr(keyStr);
        return keyCache.values().stream()
            .flatMap(ks -> ks.stream().filter(k -> k.getKey().equals(keyStr)))
            .findFirst()
            .orElse(null);
    }

    @Override
    public TokenInfo saveToken(String identifier, String tokenStr, String sourceKey) {
        TokenInfo tokenInfo = new TokenInfo(identifier, tokenStr, sourceKey);
        if (tokenCache.containsKey(identifier)) {
            tokenCache.get(identifier).add(tokenInfo);
        } else {
            Set<TokenInfo> ts = new HashSet<>();
            ts.add(tokenInfo);
            tokenCache.put(identifier, ts);
        }
        return tokenInfo;
    }

    @Override
    public TokenInfo saveToken(TokenInfo tokenInfo) {
        String identifier = tokenInfo.getIdentifier();
        if (tokenCache.containsKey(identifier)) {
            synchronized (tokenCache.get(identifier)) {
                tokenCache.get(identifier).add(tokenInfo);
            }
        } else {
            Set<TokenInfo> ts = new HashSet<>();
            ts.add(tokenInfo);
            tokenCache.put(identifier, ts);
        }
        return tokenInfo;
    }

    @Override
    public KeyInfo saveKey(String identifier, String key, String secret, Long generateTime) {
        KeyInfo keyInfo = new KeyInfo(identifier, key, secret, generateTime);
        if (keyCache.containsKey(identifier)) {
            keyCache.get(identifier).add(keyInfo);
        } else {
            Set<KeyInfo> ks = new HashSet<>();
            ks.add(keyInfo);
            keyCache.put(identifier, ks);
        }
        return keyInfo;
    }

    @Override
    public KeyInfo saveKey(KeyInfo keyInfo) {
        String identifier = keyInfo.getIdentifier();
        if (keyCache.containsKey(identifier)) {
            keyCache.get(identifier).add(keyInfo);
        } else {
            Set<KeyInfo> ks = new HashSet<>();
            ks.add(keyInfo);
            keyCache.put(identifier, ks);
        }
        return keyInfo;
    }

    @Override
    public Set<TokenInfo> removeToken(String identifier) {
        return tokenCache.remove(identifier);
    }

    @Override
    public TokenInfo removeTokenByTokenStr(String tokenStr) {
        TokenInfo tokenInfo = getTokenInfoByToken(tokenStr);
        if (null == tokenInfo) {
            throw new SimplifiedException(ErrorCode.TOKEN_INVALID);
        }
        synchronized (tokenCache.get(tokenInfo.getIdentifier())) {
            tokenCache.get(tokenInfo.getIdentifier()).remove(tokenInfo);
        }
        return tokenInfo;
    }

    @Override
    public TokenInfo removeToken(TokenInfo tokenInfo) {
        synchronized (tokenCache.get(tokenInfo.getIdentifier())) {
            tokenCache.get(tokenInfo.getIdentifier()).remove(tokenInfo);
        }
        return tokenInfo;
    }

    @Override
    public Set<KeyInfo> removeKey(String identifier) {
        removeToken(identifier);
        Set<KeyInfo> invalidKeys = keyCache.remove(identifier);
        for (KeyInfo k : invalidKeys) {
            invalidKeyCache.put(k.getKey(), k.getKey());
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
        synchronized (keyCache.get(keyInfo.getIdentifier())) {
            keyCache.get(keyInfo.getIdentifier()).remove(keyInfo);
        }

        // 收集并移除由失效的key生成的token
        Set<TokenInfo> invalidTokens = tokenCache.values().stream()
            .flatMap(t -> t.stream().filter(it -> it.getSourceKey().equals(keyInfo.getKey())))
            .collect(Collectors.toSet());
        invalidTokens.forEach(t -> {
            synchronized (tokenCache.get(t.getIdentifier())) {
                tokenCache.get(t.getIdentifier()).remove(t);
            }
        });
        return keyInfo;
    }

    @Override
    public List<TokenInfo> collectExpiredToken(Long expiredIn) {
        return tokenCache.values().stream().flatMap(ts -> ts.stream().filter(t -> isTimeOut(t.getGenerateTime(), expiredIn))).collect(Collectors.toList());
    }

    @Override
    public void clearExpiredToken(Long expriedIn) {
        List<TokenInfo> expired = collectExpiredToken(expriedIn);
        synchronized (tokenCache) {
            expired.forEach(t -> tokenCache.get(t.getIdentifier()).remove(t));
        }
    }

    @Override
    public List<KeyInfo> collectExpiredKey(Long expiredIn) {
        return keyCache.values().stream().flatMap(ks -> ks.stream().filter(t -> isTimeOut(t.getGenerateTime(), expiredIn))).collect(Collectors.toList());
    }

    @Override
    public void clearExpiredKey(Long expriedIn) {
        List<KeyInfo> expired = collectExpiredKey(expriedIn);
        synchronized (keyCache) {
            expired.forEach(k -> keyCache.get(k.getIdentifier()).remove(k));
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
