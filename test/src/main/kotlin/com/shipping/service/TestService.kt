package com.shipping.service

import com.shipping.domain.sys.User
import org.spin.data.extend.RepositoryContext
import org.spin.web.annotation.RestfulMethod
import org.spin.web.annotation.RestfulService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

/**
 *
 * Created by xuweinan on 2017/9/17.
 *
 * @author xuweinan
 */
@RestfulService("Test")
open class TestService {

    @Autowired
    private lateinit var repoCtx: RepositoryContext

    @RestfulMethod(auth = false, authRouter = "a")
    fun aaa(pp: Array<String>, a: List<*>?, b: Array<MultipartFile>?): Int {
        //        throw new SimplifiedException("aaa");
        println(a?.size)
        println(b?.size)
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
        println(repoCtx.currentDataSourceName)
        repoCtx.save(user)
        repoCtx.switchDataSource("db2")
        println(repoCtx.currentDataSourceName)
        user = user.getDTO(1)
        user.id = null
        repoCtx.save(user)
        repoCtx.switchDataSource("db1")
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
        println(repoCtx.currentDataSourceName)
        repoCtx.save(user)
        //        throw new SimplifiedException("aa");
    }
}
