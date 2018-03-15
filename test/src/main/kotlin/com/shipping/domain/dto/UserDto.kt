package com.shipping.domain.dto

import com.shipping.domain.enums.UserTypeE
import com.shipping.domain.sys.Organization
import com.shipping.domain.sys.User
import java.io.Serializable
import java.time.LocalDateTime
import java.util.*

/**
 *
 * Created by xuweinan on 2017/9/13.
 *
 * @author xuweinan
 */
class UserDto(
    var id: String? = null,

    var userName: String? = null,

    var nickname: String? = null,

    var realName: String? = null,

    var headImg: String? = null,

    var email: String? = null,

    var mobile: String? = null,

    var userType: UserTypeE? = null,

    var about: String? = null,

    var organ: Organization? = null,

    var createTime: LocalDateTime? = null
) : Serializable {
    constructor(user: User) : this(
        id = user.id!!.toString(),
        userName = user.userName,
        nickname = user.nickname,
        realName = user.realName,
        headImg = if (Objects.isNull(user.headImg)) null else user.headImg?.filePath,
        email = user.email,
        mobile = user.mobile,
        userType = user.userType,
        about = user.about,
        organ = user.organ,
        createTime = user.createTime
    )

    companion object {
        private const val serialVersionUID = -4926348221616077329L
    }
}
