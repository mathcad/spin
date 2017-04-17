package org.spin.sys.auth;

import org.spin.sys.ErrorCode;
import org.spin.throwable.SimplifiedException;
import org.spin.util.StringUtils;

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
public class InMemerySecretDao implements SecretDao {
    private final Map<String, Set<TokenInfo>> tokenCache = new ConcurrentHashMap<>();
    private final Map<String, KeyInfo> keyCache = new ConcurrentHashMap<>();

    @Override
    public String getIdentifierByToken(String tokenStr) {
        return tokenCache.values().stream().filter(Objects::nonNull)
            .flatMap(ts -> ts.stream().filter(t -> t.getToken().equals(tokenStr)).map(TokenInfo::getIdentifier))
            .findFirst()
            .orElse(null);
    }

    @Override
    public String getIdentifierByKey(String keyStr) {
        return keyCache.values().stream()
            .filter(k -> k.getKey().equals(keyStr))
            .map(KeyInfo::getIdentifier)
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
        return tokenCache.get(identifier);
    }

    @Override
    public KeyInfo getKeyByIdentifier(String identifier) {
        return keyCache.get(identifier);
    }

    @Override
    public KeyInfo getKeyByToken(String tokenStr) {
        String identifier = getIdentifierByToken(tokenStr);
        if (StringUtils.isEmpty(identifier)) {
            throw new SimplifiedException(ErrorCode.TOKEN_INVALID);
        }
        return keyCache.get(identifier);
    }

    @Override
    public KeyInfo getKeyInfoByKey(String keyStr) {
        return keyCache.values().stream()
            .filter(k -> k.getKey().equals(keyStr))
            .findFirst()
            .orElse(null);
    }

    @Override
    public TokenInfo saveToken(String identifier, String token) {
        TokenInfo tokenInfo = new TokenInfo(identifier, token);
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
    public void saveToken(TokenInfo tokenInfo) {
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
    }

    @Override
    public KeyInfo saveKey(String identifier, String key, String secret, Long generateTime) {
        KeyInfo keyInfo = new KeyInfo(identifier, key, secret, generateTime);
        synchronized (this.keyCache) {
            this.keyCache.put(identifier, keyInfo);
        }
        return keyInfo;
    }

    @Override
    public void saveKey(KeyInfo keyInfo) {
        synchronized (this.keyCache) {
            this.keyCache.put(keyInfo.getIdentifier(), keyInfo);
        }
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
    public KeyInfo removeKey(String identifier) {
        return keyCache.remove(identifier);
    }

    @Override
    public KeyInfo removeKeyByKey(String keyStr) {
        String identifier = getIdentifierByKey(keyStr);
        if (StringUtils.isEmpty(identifier)) {
            throw new SimplifiedException(ErrorCode.SECRET_INVALID);
        }
        return keyCache.remove(identifier);
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
        return keyCache.values().stream().filter(k -> isTimeOut(k.getGenerateTime(), expiredIn)).collect(Collectors.toList());
    }

    @Override
    public void clearExpiredKey(Long expriedIn) {
        List<KeyInfo> expired = collectExpiredKey(expriedIn);
        expired.forEach(k -> keyCache.remove(k.getIdentifier()));
    }

    private boolean isTimeOut(Long generateTime, Long expiredIn) {
        return (System.currentTimeMillis() - generateTime) > expiredIn;
    }
}
