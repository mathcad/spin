package com.shipping.service

import com.shipping.domain.sys.User
import com.shipping.repository.sys.UserRepository
import org.spin.core.throwable.SimplifiedException
import org.spin.web.annotation.RestfulMethod
import org.spin.web.annotation.RestfulService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Required
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

import java.beans.Transient

/**
 *
 * Created by xuweinan on 2017/9/17.
 *
 * @author xuweinan
 */
@RestfulService("Test")
open class TestService {

    @Autowired
    private val userDao: UserRepository? = null

    @RestfulMethod(auth = false, authRouter = "a")
    fun aaa(pp: Array<String>, a: List<*>, b: Array<MultipartFile>): Int {
        //        throw new SimplifiedException("aaa");
        return pp.size
    }

    @Transactional
    @RestfulMethod(auth = false, value = "b")
    open fun testTransaction() {
        var user = User()
        user.realName = "二傻子"
        user.userName = "admin"
        user.password = "123"
        user.mobile = "13111111111"
        user.email = "none@qq.com"
        println(userDao!!.currentDataSourceName)
        userDao.save(user)
        userDao.switchDataSource("db2")
        println(userDao.currentDataSourceName)
        user = user.getDTO(1)
        user.id = null
        userDao.save(user)
        userDao.switchDataSource("db1")
        //        throw new SimplifiedException("aa");
    }

    @Transactional
    @RestfulMethod(auth = false, value = "c")
    open fun testSTransaction() {
        val user = User()
        user.realName = "二傻子"
        user.userName = "admin"
        user.password = "123"
        user.mobile = "13111111111"
        user.email = "none@qq.com"
        println(userDao!!.currentDataSourceName)
        userDao.save(user)
        //        throw new SimplifiedException("aa");
    }
}
