package org.spin.sys.auth;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.spin.sys.ErrorCode;
import org.spin.throwable.SimplifiedException;
import org.spin.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 内存中的认证信息存储
 * Created by xuweinan on 2017/3/2.
 *
 * @author xuweinan
 */
public class InMemerySecretDao implements SecretDao {
    //    private final MultiKeyMap<String, TokenInfo> tokenCache = new MultiKeyMap<>();
    private final BidiMap<String, TokenInfo> tokenCache = new DualHashBidiMap<>();
    private final BidiMap<String, KeyInfo> keyCache = new DualHashBidiMap<>();


    @Override
    public String getIdentifierByToken(String token) {
        return tokenCache.getKey(token);
    }

    @Override
    public String getIdentifierByKey(String key) {
        return keyCache.getKey(key);
    }

    @Override
    public TokenInfo getTokenByIdentifier(String identifier) {
        return tokenCache.get(identifier);
    }

    @Override
    public TokenInfo getTokenInfoByToken(String token) {
        return tokenCache.get(tokenCache.getKey(token));
    }

    @Override
    public TokenInfo getTokenByKey(String key) {
        String identifier = keyCache.getKey(key);
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
    public KeyInfo getKeyByToken(String token) {
        String identifier = tokenCache.getKey(token);
        if (StringUtils.isEmpty(identifier)) {
            throw new SimplifiedException(ErrorCode.TOKEN_INVALID);
        }
        return keyCache.get(identifier);
    }

    @Override
    public KeyInfo getKeyInfoByKey(String key) {
        return keyCache.get(keyCache.getKey(key));
    }

    @Override
    public TokenInfo saveToken(String identifier, String token) {
        TokenInfo tokenInfo = new TokenInfo(identifier, token);
        synchronized (this.tokenCache) {
            this.tokenCache.put(identifier, tokenInfo);
        }
        return tokenInfo;
    }

    @Override
    public void saveToken(TokenInfo tokenInfo) {
        synchronized (this.tokenCache) {
            this.tokenCache.put(tokenInfo.getIdentifier(), tokenInfo);
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
    public TokenInfo removeToken(String identifier) {
        return tokenCache.remove(identifier);
    }

    @Override
    public KeyInfo removeKey(String identifier) {
        return keyCache.remove(identifier);
    }

    @Override
    public List<TokenInfo> collectExpiredToken(Long expiredIn) {
        return tokenCache.values().stream().filter(e -> isTimeOut(e.getGenerateTime(), expiredIn)).collect(Collectors.toList());
    }

    @Override
    public List<KeyInfo> collectExpiredKey(Long expiredIn) {
        return keyCache.values().stream().filter(k -> isTimeOut(k.getGenerateTime(), expiredIn)).collect(Collectors.toList());
    }

    private boolean isTimeOut(Long generateTime, Long expiredIn) {
        return (System.currentTimeMillis() - generateTime) > expiredIn;
    }
}
