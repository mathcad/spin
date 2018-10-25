package com.shipping.service

import com.shipping.domain.enums.OrganizationTypeE
import com.shipping.domain.sys.Function
import mu.KLogging
import org.spin.data.extend.RepositoryContext
import org.spin.data.rs.MapRowMapper
import org.spin.web.annotation.RestfulMethod
import org.spin.web.annotation.RestfulService
import org.springframework.beans.factory.annotation.Autowired
import javax.transaction.Transactional

/**
 *
 * Created by xuweinan on 2017/9/4.
 *
 * @author xuweinan
 */
@RestfulService
class FunctionService {
    companion object : KLogging()

    @Autowired
    private lateinit var repoCtx: RepositoryContext

    @RestfulMethod
    @Transactional
    fun add(func: Function) {
        func.parent?.let {
            OrganizationTypeE.GROUP
            repoCtx.get(Function::class.java, func.parent!!.id)
            repoCtx.save(func, true)
            val list: List<Map<String, Any?>> = repoCtx.doReturningWork("SELECT * FROM sys_function") {
                MapRowMapper().extractData(it)
            }
            list
        }
    }
}
