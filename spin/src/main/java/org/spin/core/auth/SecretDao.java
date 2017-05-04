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
     */
    String getIdentifierByToken(String tokenStr);

    /**
     * 通过密钥查询用户id
     */
    String getIdentifierByKey(String keyStr);

    /**
     * 通过用户id查询token
     */
    Set<TokenInfo> getTokenByIdentifier(String identifier);

    /**
     * 通过token字符串查询token信息
     */
    TokenInfo getTokenInfoByToken(String tokenStr);

    /**
     * 通过密钥查询token
     */
    Set<TokenInfo> getTokenByKey(String keyStr);

    /**
     * 通过用户id查询密钥
     */
    Set<KeyInfo> getKeyByIdentifier(String identifier);

    /**
     * 通过key字符串查询密钥信息
     */
    KeyInfo getKeyInfoByKey(String keyStr);

    /**
     * 保存token
     *
     * @param identifier 用户id
     * @param tokenStr   token字符串
     * @param sourceKey  生成token的key
     */
    TokenInfo saveToken(String identifier, String tokenStr, String sourceKey);

    /**
     * 保存token
     */
    TokenInfo saveToken(TokenInfo tokenInfo);

    /**
     * 保存密钥
     *
     * @param identifier   用户id
     * @param keyStr       密钥字符串
     * @param secret       密码
     * @param generateTime 生成时间
     */
    KeyInfo saveKey(String identifier, String keyStr, String secret, Long generateTime);

    /**
     * 保存密钥
     */
    KeyInfo saveKey(KeyInfo keyInfo);

    /**
     * 根据用户id删除token
     */
    Set<TokenInfo> removeToken(String identifier);

    /**
     * 根据token字符串删除token
     */
    TokenInfo removeTokenByTokenStr(String tokenStr);

    /**
     * 删除token
     */
    TokenInfo removeToken(TokenInfo tokenInfo);

    /**
     * 根据用户id删除密钥
     */
    Set<KeyInfo> removeKey(String identifier);

    /**
     * 根据key字符串删除key
     */
    KeyInfo removeKeyByKey(String keyStr);

    /**
     * 删除key
     */
    KeyInfo removeKey(KeyInfo keyInfo);

    /**
     * 收集过期token
     */
    List<TokenInfo> collectExpiredToken(Long expiredIn);

    /**
     * 清除过期的token
     */
    void clearExpiredToken(Long expriedIn);

    /**
     * 收集过期密钥
     */
    List<KeyInfo> collectExpiredKey(Long expiredIn);

    /**
     * 清除过期的密钥
     */
    void clearExpiredKey(Long expriedIn);
}
