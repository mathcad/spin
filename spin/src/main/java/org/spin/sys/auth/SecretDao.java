package org.spin.sys.auth;

/**
 * 授权认证信息存储接口
 * Created by xuweinan on 2017/3/2.
 *
 * @author xuweinan
 */
public interface SecretDao {

    TokenInfo getTokenByIdentifier(String identifier);

    TokenInfo getTokenInfoByToken(String token);

    void saveTokenInfo(String identifier, String token);

    void clearExpiredInfo(Long expiredIn);
}
