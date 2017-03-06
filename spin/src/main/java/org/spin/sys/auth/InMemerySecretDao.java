package org.spin.sys.auth;

import org.apache.commons.collections4.map.MultiKeyMap;

/**
 * 内存中的认证信息存储
 * Created by xuweinan on 2017/3/2.
 *
 * @xuweinan
 */
public class InMemerySecretDao implements SecretDao {
    private final MultiKeyMap<String, TokenInfo> tokenCache = new MultiKeyMap<>();

    @Override
    public TokenInfo getTokenByIdentifier(String identifier) {
        return tokenCache.get(identifier);
    }

    @Override
    public TokenInfo getTokenInfoByToken(String token) {
        return tokenCache.get(token);
    }

    @Override
    public void saveTokenInfo(String identifier, String token) {
        TokenInfo tokenInfo = tokenCache.remove(identifier);
        if (null == tokenInfo)
            tokenInfo = new TokenInfo(identifier, token);
        else if (tokenInfo.getToken().equals(token))
            return;
        else
            tokenInfo.setToken(token);
        synchronized (this.tokenCache) {
            this.tokenCache.put(identifier, token, tokenInfo);
        }
    }

    @Override
    public synchronized void clearExpiredInfo(Long expiredIn) {
        this.tokenCache.entrySet().stream().filter(e -> this.isTimeOut(e.getValue().getGenerateTime(), expiredIn))
                .forEach(e -> this.tokenCache.remove(e.getKey().getKey(0)));
    }

    private boolean isTimeOut(Long generateTime, Long expiredIn) {
        return (System.currentTimeMillis() - generateTime) > expiredIn;
    }
}
