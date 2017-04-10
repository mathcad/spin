package org.spin.sys.auth;

import java.util.List;

/**
 * 授权认证信息存储接口
 * Created by xuweinan on 2017/3/2.
 *
 * @author xuweinan
 */
public interface SecretDao {

    /**
     * 通过token查询用户id
     */
    String getIdentifierByToken(String token);

    /**
     * 通过密钥查询用户id
     */
    String getIdentifierByKey(String key);

    /**
     * 通过用户id查询token
     */
    TokenInfo getTokenByIdentifier(String identifier);

    /**
     * 通过token字符串查询token信息
     */
    TokenInfo getTokenInfoByToken(String token);

    /**
     * 通过密钥查询token
     */
    TokenInfo getTokenByKey(String key);

    /**
     * 通过用户id查询密钥
     */
    KeyInfo getKeyByIdentifier(String identifier);

    /**
     * 通过token查询密钥
     */
    KeyInfo getKeyByToken(String token);

    /**
     * 通过key字符串查询密钥信息
     */
    KeyInfo getKeyInfoByKey(String key);

    /**
     * 保存token
     *
     * @param identifier 用户id
     * @param token      token字符串
     */
    TokenInfo saveToken(String identifier, String token);

    /**
     * 保存token
     */
    void saveToken(TokenInfo tokenInfo);

    /**
     * 保存密钥
     *
     * @param identifier   用户id
     * @param key          密钥字符串
     * @param secret       密码
     * @param generateTime 生成时间
     */
    KeyInfo saveKey(String identifier, String key, String secret, Long generateTime);

    /**
     * 保存密钥
     */
    void saveKey(KeyInfo keyInfo);

    /**
     * 根据用户id删除token
     */
    TokenInfo removeToken(String identifier);

    /**
     * 根据用户id删除密钥
     */
    KeyInfo removeKey(String identifier);

    /**
     * 收集过期token
     */
    List<TokenInfo> collectExpiredToken(Long expiredIn);

    /**
     * 收集过期密钥
     */
    List<KeyInfo> collectExpiredKey(Long expiredIn);
}
