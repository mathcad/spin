package com.shipping.service

import com.shipping.domain.dto.LoginInfo
import com.shipping.domain.enums.FunctionTypeE
import com.shipping.domain.enums.UserTypeE
import com.shipping.domain.sys.File
import com.shipping.domain.sys.Function
import com.shipping.domain.sys.User
import com.shipping.internal.InfoCache
import org.hibernate.criterion.Restrictions
import org.spin.core.Assert
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
import org.spin.wx.WxHelper
import org.spin.wx.WxTokenManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * 用户服务
 *
 * Created by xuweinan on 2017/4/20.
 *
 * @author xuweinan
 */
@Service
class UserService : Authenticator<User> {

    @Autowired
    private lateinit var repoCtx: RepositoryContext

    @Autowired
    private lateinit var secretManager: SecretManager

    val currentUser: Map<String, Any?>?
        get() {
            val user: User? = repoCtx.get(User::class.java, SessionManager.getCurrentUser().id)
            return if (null == user) null else let {
                val userMap = HashMap<String, Any?>()
                userMap["userId"] = user.id
                userMap["nickName"] = user.nickname
                userMap["headImg"] = user.headImg?.filePath
                userMap["createTime"] = user.createTime
                return userMap
            }
        }

    override fun getSubject(identity: Any): User = repoCtx.getRepo(User::class.java).get(identity.toString().toLong())


    override fun getRolePermissionList(identity: Any): RolePermission {
        // 角色权限
        val id = identity as Long
        return InfoCache.permissionCache[id] ?: let {
            val rp = RolePermission()
            val user: User? = repoCtx.get(User::class.java, id)
            rp.userIdentifier = id
            rp.permissions = user?.permissions?.map { it.code }
            rp.roles = user?.roles?.map { it.code }
            InfoCache.permissionCache[id] = rp
            rp
        }
    }

    override fun authenticate(id: Any, password: String): Boolean {
        if (StringUtils.isEmpty(password))
            return false
        val user: User? = try {
            repoCtx.get(User::class.java, id.toString().toLong())
        } catch (e: NumberFormatException) {
            repoCtx.findOne(CriteriaBuilder.forClass(User::class.java).eq("userName", id.toString()))
        }

        return DigestUtils.sha256Hex(password + user?.salt) == user?.password
    }

    override fun checkAuthorities(id: Any, authRouter: String?): Boolean =
    // 权限验证
        BooleanExt.ofAny(StringUtils.isEmpty(authRouter)).yes { true }.otherwise {
            val apis = getUserFunctions(id as Long)[FunctionTypeE.API]
            apis?.any { a -> authRouter == a.code }
        }

    @Transactional
    fun saveUser(user: User): User = repoCtx.save(user)


    fun getUser(id: Long): User = repoCtx.get(User::class.java, id)


    fun getWxUser(openid: String): User? = repoCtx.findOne(User::class.java, "openid", openid)


    @Transactional
    fun createUserFromWx(openId: String): User =
        WxHelper.getUserInfo(WxTokenManager.getDefaultOAuthToken().token, openId).run {
            FileOperator.saveFileFromUrl(headimgurl).run {
                repoCtx.save(User(
                    openId = openId,
                    nickname = nickname,
                    userType = UserTypeE.普通用户,
                    headImg = repoCtx.save(File(
                        guid = UUID.randomUUID().toString(),
                        originName = originName,
                        fileName = storeName.substring(storeName.lastIndexOf('/') + 1),
                        filePath = storeName,
                        extension = extention,
                        size = size
                    ))
                ))
            }
        }


    /**
     * 常规登录
     */
    fun login(identity: String, password: String): LoginInfo = checkUser(identity, password, null, true)


    /**
     * 注销
     */
    fun logout(key: String) = secretManager.invalidKeyByKeyStr(key)


    /**
     * 密钥登录
     */
    fun keyLogin(key: String): LoginInfo {
        val info: KeyInfo
        try {
            info = secretManager.getKeyInfo(key)
            secretManager.invalidKeyByKeyStr(key)
        } catch (e: Exception) {
            throw SimplifiedException(ErrorCode.SECRET_INVALID)
        }

        val user = repoCtx.get(User::class.java, info.identifier.toLong())
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
        return try {
            WxTokenManager.getDefaultOAuthToken(code)
        } catch (e: Exception) {
            throw SimplifiedException(ErrorCode.INVALID_PARAM, "非法的code")
        }.run out@{
            (getWxUser(openId) ?: createUserFromWx(openId)).run inner@{
                LoginInfo().apply {
                    userId = this@inner.id
                    setUserInfo(this@inner)
                    secretManager.generateKey(this@inner.id!!.toString(), this@inner.openId, "openId").run {
                        setKeyInfo(this, secretManager.keyExpiredIn)
                        setTokenInfo(secretManager.generateToken(key), secretManager.tokenExpiredIn)
                    }
                }
            }
        }
    }

    private fun checkUser(identity: String, secret: String, user: User?, isPassword: Boolean): LoginInfo {
        val usr = user ?: let {
            try {
                repoCtx.get(User::class.java, identity.toLong())
            } catch (ignore: NumberFormatException) {
                Assert.notNull(repoCtx.findOne(User::class.java, "userName", identity), "指定的用户不存在")
            }
        }
        return (if (isPassword) authenticate(identity, secret) else usr?.openId == secret).run {
            if (this) {
                LoginInfo().apply {
                    userId = usr.id
                    userId = usr.id
                    setUserInfo(usr)
                    val keyInfo = secretManager.generateKey(usr.id.toString(), secret, "password")
                    setKeyInfo(keyInfo, secretManager.keyExpiredIn)
                    setTokenInfo(secretManager.generateToken(keyInfo.key), secretManager.tokenExpiredIn)
                }
            } else {
                throw SimplifiedException("登录凭据无效")
            }
        }
    }

    fun userList(): List<User> = repoCtx.list(CriteriaBuilder.forClass(User::class.java).notEq("userType", UserTypeE.微信用户))

    fun getUserFunctions(id: Long): Map<FunctionTypeE, List<Function>> =
        InfoCache.functionCache[id] ?: let {
            val rolePermission = getRolePermissionList(SessionManager.getCurrentUser().id)
            val cb = CriteriaBuilder.forClass(Function::class.java).apply {
                if (rolePermission.permissions.isEmpty()) {
                    isNull("permission")
                } else {
                    or(Restrictions.isNull("permission"),
                        Restrictions.`in`("permission.code", rolePermission.permissions))
                }
            }
            val functions = repoCtx.list(cb)
            val r = functions.groupBy { it.type!! }.map { (k, v) -> k to v.toMutableList() }.toMap().toMutableMap()
            if (r.isEmpty()) {
                r[FunctionTypeE.API] = ArrayList()
                r[FunctionTypeE.MEMU] = ArrayList()
            }
            InfoCache.functionCache[id] = r
            r
        }
}
