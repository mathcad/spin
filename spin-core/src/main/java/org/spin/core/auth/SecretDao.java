package org.spin.core.auth;

import java.util.List;
import java.util.Set;

/**
 * 授权认证信息存储接口
 * Created by xuweinan on 2017/3/2.
 *
 * @author xuweinan
 */
public interface SecretDao {

    /**
     * 通过token查询用户id
     *
     * @param tokenStr token字符串
     * @return 用户id
     */
    String getIdentifierByToken(String tokenStr);

    /**
     * 通过密钥查询用户id
     *
     * @param keyStr 密钥字符串
     * @return 用户id
     */
    String getIdentifierByKey(String keyStr);

    /**
     * 通过用户id查询token
     *
     * @param identifier 用户id
     * @return token信息
     */
    Set<TokenInfo> getTokenByIdentifier(String identifier);

    /**
     * 通过token字符串查询token信息
     *
     * @param tokenStr token字符串
     * @return token信息
     */
    TokenInfo getTokenInfoByToken(String tokenStr);

    /**
     * 通过密钥查询token
     *
     * @param keyStr 密钥字符串
     * @return token集合
     */
    Set<TokenInfo> getTokenByKey(String keyStr);

    /**
     * 通过用户id查询密钥
     *
     * @param identifier 用户id
     * @return 密钥信息集合
     */
    Set<KeyInfo> getKeyByIdentifier(String identifier);

    /**
     * 通过key字符串查询密钥信息
     *
     * @param keyStr 密钥字符串
     * @return 密钥信息
     */
    KeyInfo getKeyInfoByKey(String keyStr);

    /**
     * 保存token
     *
     * @param identifier 用户id
     * @param tokenStr   token字符串
     * @param sourceKey  生成token的key
     * @return token
     */
    TokenInfo saveToken(String identifier, String tokenStr, String sourceKey);

    /**
     * 保存token
     *
     * @param tokenInfo token
     * @return token
     */
    TokenInfo saveToken(TokenInfo tokenInfo);

    /**
     * 保存密钥
     *
     * @param identifier   用户id
     * @param key          密钥字符串
     * @param secret       用户密钥
     * @param secretType   用户密钥类型
     * @param generateTime 生成时间
     * @return 密钥信息
     */
    KeyInfo saveKey(String identifier, String key, String secret, String secretType, Long generateTime);

    /**
     * 保存密钥
     *
     * @param keyInfo 密钥信息
     * @return 密钥信息
     */
    KeyInfo saveKey(KeyInfo keyInfo);

    /**
     * 根据用户id删除token
     *
     * @param identifier 用户id
     * @return 被删除的token集合
     */
    Set<TokenInfo> removeToken(String identifier);

    /**
     * 根据token字符串删除token
     *
     * @param tokenStr token字符串
     * @return 被删除的tokne
     */
    TokenInfo removeTokenByTokenStr(String tokenStr);

    /**
     * 删除token
     *
     * @param tokenInfo token信息
     * @return 被删除的token
     */
    TokenInfo removeToken(TokenInfo tokenInfo);

    /**
     * 根据用户id删除密钥
     *
     * @param identifier 用户id
     * @return 被删除的密钥集合
     */
    Set<KeyInfo> removeKey(String identifier);

    /**
     * 根据key字符串删除key
     *
     * @param keyStr 密钥字符串
     * @return 被删除的密钥信息
     */
    KeyInfo removeKeyByKey(String keyStr);

    /**
     * 删除key
     *
     * @param keyInfo 密钥信息
     * @return 被删除的密钥信息
     */
    KeyInfo removeKey(KeyInfo keyInfo);

    /**
     * 收集过期token
     *
     * @param expiredIn 过期时间(毫秒)
     * @return 过期的Token列表
     */
    List<TokenInfo> collectExpiredToken(Long expiredIn);

    /**
     * 清除过期的token
     *
     * @param expiredIn 过期时间(毫秒)
     */
    void clearExpiredToken(Long expiredIn);

    /**
     * 收集过期密钥
     *
     * @param expiredIn 过期时间(毫秒)
     * @return 过期的密钥信息列表
     */
    List<KeyInfo> collectExpiredKey(Long expiredIn);

    /**
     * 清除过期的密钥
     *
     * @param expiredIn 过期时间(毫秒)
     */
    void clearExpiredKey(Long expiredIn);
}
