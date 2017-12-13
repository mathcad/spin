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
class UserDto(user: User) : Serializable {

    var id: String? = null

    var userName: String? = null

    var nickname: String? = null

    var realName: String? = null

    var headImg: String? = null

    var email: String? = null

    var mobile: String? = null

    var userType: UserTypeE? = null

    var about: String? = null

    var organ: Organization? = null

    var createTime: LocalDateTime? = null

    init {
        this.id = user.id!!.toString()
        this.userName = user.userName
        this.nickname = user.nickname
        this.realName = user.realName
        this.headImg = if (Objects.isNull(user.headImg)) null else user.headImg?.filePath
        this.email = user.email
        this.mobile = user.mobile
        this.userType = user.userType
        this.about = user.about
        this.organ = user.organ
        this.createTime = user.createTime
    }

    companion object {
        private const val serialVersionUID = -4926348221616077329L
    }
}
