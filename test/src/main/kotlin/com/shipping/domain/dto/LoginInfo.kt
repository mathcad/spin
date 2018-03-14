package com.shipping.domain.dto


import com.shipping.domain.sys.User
import org.spin.core.auth.KeyInfo
import org.spin.core.auth.TokenInfo
import java.util.*

/**
 * token，密钥DTO
 *
 * Created by xuweinan on 2017/4/26.
 *
 * @author xuweinan
 */
class LoginInfo {
    var userId: Long? = null
    private val tokenInfo = HashMap<String, Any>()
    private val keyInfo = HashMap<String, Any>()
    var userInfo: UserDto? = null
        private set

    fun getTokenInfo(): Map<String, Any> {
        return tokenInfo
    }

    fun setTokenInfo(tokenInfo: TokenInfo, expiredIn: Long?) {
        this.tokenInfo["token"] = tokenInfo.token
        this.tokenInfo["expiredSince"] = Date(tokenInfo.generateTime!! + expiredIn!!)
    }

    fun getKeyInfo(): Map<String, Any> {
        return keyInfo
    }

    fun setKeyInfo(keyInfo: KeyInfo, expiredIn: Long?) {
        this.keyInfo["key"] = keyInfo.key
        this.keyInfo["expiredSince"] = Date(keyInfo.generateTime!! + expiredIn!!)
    }

    fun setUserInfo(user: User) {
        this.userInfo = UserDto(user)
    }
}
