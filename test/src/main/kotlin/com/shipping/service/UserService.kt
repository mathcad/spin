package com.shipping.service

import com.shipping.domain.dto.LoginInfo
import com.shipping.domain.enums.FunctionTypeE
import com.shipping.domain.enums.UserTypeE
import com.shipping.domain.sys.File
import com.shipping.domain.sys.Function
import com.shipping.domain.sys.User
import com.shipping.internal.InfoCache
import com.shipping.repository.sys.FileRepository
import com.shipping.repository.sys.FunctionRepository
import com.shipping.repository.sys.UserRepository
import org.hibernate.criterion.Restrictions
import org.spin.core.ErrorCode
import org.spin.core.auth.Authenticator
import org.spin.core.auth.KeyInfo
import org.spin.core.auth.RolePermission
import org.spin.core.auth.SecretManager
import org.spin.core.session.SessionManager
import org.spin.core.throwable.SimplifiedException
import org.spin.core.util.BooleanExt
import org.spin.core.util.DigestUtils
import org.spin.core.util.StringUtils
import org.spin.data.extend.RepositoryContext
import org.spin.data.query.CriteriaBuilder
import org.spin.web.FileOperator
import org.spin.wx.AccessToken
import org.spin.wx.WxHelper
import org.spin.wx.WxTokenManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*
import java.util.stream.Collectors

/**
 * 用户服务
 *
 * Created by xuweinan on 2017/4/20.
 *
 * @author xuweinan
 */
@Service
open class UserService : Authenticator<User> {

    @Autowired
    private val userDao: UserRepository? = null

    @Autowired
    private val fileDao: FileRepository? = null

    @Autowired
    private val functionDao: FunctionRepository? = null

    @Autowired
    private val secretManager: SecretManager? = null

    @Autowired
    private val repositoryContext: RepositoryContext? = null

    val currentUser: Map<String, Any?>
        get() {
            val user = userDao!!.get(SessionManager.getCurrentUser().id)
            val userMap = HashMap<String, Any?>()
            userMap.put("userId", user.id)
            userMap.put("nickName", user.nickname)
            userMap.put("headImg", if (null == user.headImg) null else user.headImg!!.filePath)
            userMap.put("createTime", user.createTime)
            return userMap
        }

    override fun getSubject(identity: Any): User {
        return repositoryContext!!.getRepo(User::class.java).get(java.lang.Long.parseLong(identity.toString()))
    }

    override fun getRolePermissionList(identity: Any): RolePermission {
        // 角色权限
        val id = identity as Long
        return InfoCache.permissionCache[id] ?: let {
            val rp = RolePermission()
            val user: User? = userDao!!.get(id)
            rp.userIdentifier = id
            rp.permissions = user?.permissions?.map { it.code }
            rp.roles = user?.roles?.map { it.code }
            InfoCache.permissionCache.put(id, rp)
            rp
        }
    }

    override fun authenticate(id: Any?, password: String): Boolean {
        if (null == id || StringUtils.isEmpty(password))
            return false
        val user: User? = try {
            userDao!!.get(java.lang.Long.parseLong(id.toString()))
        } catch (e: NumberFormatException) {
            userDao!!.findOne(CriteriaBuilder.newInstance().eq("userName", id.toString()))
        }

        return user != null && !StringUtils.isEmpty(user.password) && DigestUtils.sha256Hex(password + user.salt) == user.password
    }

    override fun checkAuthorities(id: Any, authRouter: String?): Boolean {
        // 权限验证
        return BooleanExt.of(StringUtils.isEmpty(authRouter), Boolean::class.javaPrimitiveType).yes { true }.otherwise {
            val apis = getUserFunctions(id as Long)[FunctionTypeE.API]
            apis?.any { a -> authRouter == a.code }
        }
    }

    @Transactional
    open fun saveUser(user: User): User {
        return userDao!!.save(user)
    }

    fun getUser(id: Long?): User {
        return userDao!!.get(id)
    }

    fun getWxUser(openid: String): User {
        return userDao!!.findOne("openid", openid)
    }

    @Transactional
    open fun createUserFromWx(openId: String): User {
        val accessToken = WxTokenManager.getDefaultOAuthToken()
        val wxUserInfo = WxHelper.getUserInfo(accessToken.token, openId)
        val user = User()
        user.openId = openId
        user.nickname = wxUserInfo.nickname
        user.userType = UserTypeE.普通用户

        val rs = FileOperator.saveFileFromUrl(wxUserInfo.headimgurl)
        var file = File()
        file.guid = UUID.randomUUID().toString()
        file.originName = rs.originName
        file.fileName = rs.storeName.substring(rs.storeName.lastIndexOf('/') + 1)
        file.filePath = rs.storeName
        file.extension = rs.extention
        file.size = rs.size
        file = fileDao!!.save(file)

        user.headImg = file
        return userDao!!.save(user)
    }

    /**
     * 常规登录
     */
    fun login(identity: String, password: String): LoginInfo {
        return checkUser(identity, password, null, true)
    }

    /**
     * 注销
     */
    fun logout(key: String) {
        secretManager!!.invalidKeyByKeyStr(key)
    }

    /**
     * 密钥登录
     */
    fun keyLogin(key: String): LoginInfo {
        val info: KeyInfo
        try {
            info = secretManager!!.getKeyInfo(key)
            secretManager.invalidKeyByKeyStr(key)
        } catch (e: Exception) {
            throw SimplifiedException(ErrorCode.SECRET_INVALID)
        }

        val user = userDao!!.get(java.lang.Long.parseLong(info.identifier))
        if (null != user) {
            val loginInfo: LoginInfo
            if (StringUtils.isNotEmpty(user.password)) {
                loginInfo = checkUser(info.identifier, info.secret, user, true)
                SessionManager.extendSession(DigestUtils.md5Hex(key), DigestUtils.md5Hex(loginInfo.getKeyInfo()["key"].toString()))
                return loginInfo
            } else if (StringUtils.isNotEmpty(user.openId)) {
                loginInfo = checkUser(info.identifier, info.secret, user, false)
                SessionManager.extendSession(DigestUtils.md5Hex(key), DigestUtils.md5Hex(loginInfo.getKeyInfo()["key"].toString()))
                return loginInfo
            }
        }
        throw SimplifiedException(ErrorCode.SECRET_INVALID)
    }

    /**
     * 微信授权登录
     */
    fun wxLogin(code: String): LoginInfo {
        val accessToken: AccessToken
        try {
            accessToken = WxTokenManager.getDefaultOAuthToken(code)
        } catch (e: Exception) {
            throw SimplifiedException(ErrorCode.INVALID_PARAM, "非法的code")
        }

        var user: User? = getWxUser(accessToken.openId)
        if (null == user) {
            // 创建新用户并关联微信
            user = createUserFromWx(accessToken.openId)
        }
        val dto = LoginInfo()
        dto.userId = user.id
        dto.setUserInfo(user)
        val keyInfo = secretManager!!.generateKey(user.id!!.toString(), user.openId, "openId")
        dto.setKeyInfo(keyInfo, secretManager.keyExpiredIn)
        dto.setTokenInfo(secretManager.generateToken(keyInfo.key), secretManager.tokenExpiredIn)
        return dto
    }

    private fun checkUser(identity: String, secret: String, user: User?, isPassword: Boolean): LoginInfo {
        val usr = user ?: let {
            try {
                userDao!!.get(identity.toLong())
            } catch (ignore: NumberFormatException) {
                userDao!!.findOne(CriteriaBuilder.newInstance().eq("userName", identity))
            }
        }
        val authenticated: Boolean
        authenticated = if (isPassword) authenticate(identity, secret) else usr!!.openId == secret
        if (authenticated) {
            val dto = LoginInfo()
            dto.userId = usr.id
            dto.setUserInfo(usr)
            val keyInfo = secretManager!!.generateKey(usr.id.toString(), secret, "password")
            dto.setKeyInfo(keyInfo, secretManager.keyExpiredIn)
            dto.setTokenInfo(secretManager.generateToken(keyInfo.key), secretManager.tokenExpiredIn)
            return dto
        } else {
            throw SimplifiedException("登录凭据无效")
        }
    }

    fun userList(): List<User> {
        return userDao!!.list(CriteriaBuilder.newInstance().notEq("userType", UserTypeE.微信用户))
    }

    fun getUserFunctions(id: Long): Map<FunctionTypeE, List<Function>> {
        return InfoCache.functionCache[id] ?: let {
            val rolePermission = getRolePermissionList(SessionManager.getCurrentUser().id)
            val cb = CriteriaBuilder.newInstance()
            if (rolePermission.permissions.isEmpty()) {
                cb.isNull("permission")
            } else {
                cb.or(Restrictions.isNull("permission"),
                    Restrictions.`in`("permission.code", rolePermission.permissions))
            }
            val functions = functionDao!!.list(cb)
            val r = functions.groupBy { it.type!! }.map { (k, v) -> k to v.toMutableList() }.toMap().toMutableMap()
            if (r.isEmpty()) {
                r.put(FunctionTypeE.API, ArrayList())
                r.put(FunctionTypeE.MEMU, ArrayList())
            }
            InfoCache.functionCache.put(id, r)
            r
        }
    }
}
