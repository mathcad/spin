package com.shipping.domain.dto;


import com.shipping.domain.sys.User;
import org.spin.core.SpinContext;
import org.spin.core.auth.KeyInfo;
import org.spin.core.auth.TokenInfo;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * token，密钥DTO
 * <p>Created by xuweinan on 2017/4/26.</p>
 *
 * @author xuweinan
 */
public class LoginInfo {
    private Long userId;
    private Map<String, Object> tokenInfo = new HashMap<>();
    private Map<String, Object> keyInfo = new HashMap<>();
    private UserDto userInfo;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Map<String, Object> getTokenInfo() {
        return tokenInfo;
    }

    public void setTokenInfo(TokenInfo tokenInfo, Long expiredIn) {
        this.tokenInfo.put("token", tokenInfo.getToken());
        this.tokenInfo.put("expiredSince", new Date(tokenInfo.getGenerateTime() + expiredIn));
    }

    public Map<String, Object> getKeyInfo() {
        return keyInfo;
    }

    public void setKeyInfo(KeyInfo keyInfo, Long expiredIn) {
        this.keyInfo.put("key", keyInfo.getKey());
        this.keyInfo.put("expiredSince", new Date(keyInfo.getGenerateTime() + expiredIn));
    }

    public UserDto getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(User user) {
        this.userInfo = new UserDto(user);
    }
}
