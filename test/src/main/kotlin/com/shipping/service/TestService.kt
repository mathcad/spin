package com.shipping.service

import com.shipping.domain.sys.User
import mu.KLogging
import org.spin.data.core.DataSourceContext
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
class TestService {
    companion object : KLogging()

    @Autowired
    private lateinit var repoCtx: RepositoryContext

    @RestfulMethod(auth = false, authRouter = "a")
    fun aaa(pp: Array<String>, a: List<*>?, b: Array<MultipartFile>?): Int {
        //        throw new SimplifiedException("aaa");
        println(a?.size)
        println(b?.size)
        logger.debug { "aaa" }
        return pp.size
    }

    @Transactional
    @RestfulMethod(auth = false, value = "b")
    fun testTransaction() {
        var user = User().apply {
            realName = "二傻子"
            userName = "admin"
            password = "123"
            mobile = "13111111111"
            email = "none@qq.com"
        }
        println(DataSourceContext.getCurrentDataSourceName())
        repoCtx.save(user)
        DataSourceContext.switchDataSource("db2")
        println(DataSourceContext.getCurrentDataSourceName())
        user = user.getDTO(1)
        user.id = null
        repoCtx.save(user)
        DataSourceContext.usePrimaryDataSource()
        //        throw new SimplifiedException("aa");
    }

    @Transactional
    @RestfulMethod(auth = false, value = "c")
    fun testTransaction2() {
        var user = User().apply {
            realName = "二傻子"
            userName = "admin"
            password = "123"
            mobile = "13111111111"
            email = "none@qq.com"
        }
        println(DataSourceContext.getCurrentDataSourceName())
        print(DataSourceContext.getCurrentSchema())
        repoCtx.save(user)
        DataSourceContext.switchSchema("db2")
        print(DataSourceContext.getCurrentSchema())
        user = user.getDTO(1)
        user.id = null
        repoCtx.save(user)
        DataSourceContext.restoreSchema()
        //        throw new SimplifiedException("aa");
    }

    @Transactional
    @RestfulMethod(auth = false, value = "d")
    fun testSTransaction() {
        val user = User().apply {
            realName = "xuweinan"
            userName = "admin"
            password = "123"
            mobile = "13111111111"
            email = "none@qq.com"
        }
        println(DataSourceContext.getCurrentDataSourceName())
        repoCtx.save(user)
        //        throw new SimplifiedException("aa");
        println(DataSourceContext.getCurrentSchema())
        DataSourceContext.switchSchema("dev_db2")
        println(DataSourceContext.getCurrentSchema())
        val u2 = User().apply {
            realName = "mathcat"
            userName = "admin"
            password = "123"
            mobile = "13111112222"
            email = "none@qq.com"
        }
        u2.id = null
        repoCtx.save(u2)
    }
}
