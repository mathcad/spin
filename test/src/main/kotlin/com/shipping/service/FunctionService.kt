package com.shipping.service

import com.shipping.domain.enums.OrganizationTypeE
import com.shipping.domain.sys.Function
import org.slf4j.LoggerFactory
import org.spin.data.extend.RepositoryContext
import org.spin.web.annotation.RestfulMethod
import org.spin.web.annotation.RestfulService
import org.springframework.beans.factory.annotation.Autowired
import java.util.*
import javax.transaction.Transactional

/**
 *
 * Created by xuweinan on 2017/9/4.
 *
 * @author xuweinan
 */
@RestfulService
open class FunctionService {

    @Autowired
    private lateinit var repoCtx: RepositoryContext

    @RestfulMethod
    @Transactional
    open fun add(func: Function) {
        if (Objects.nonNull(func.parent)) {
            OrganizationTypeE.GROUP
            repoCtx.get(Function::class.java, func.parent!!.id)
            repoCtx.save(func, true)
            repoCtx.doReturningWork("SELECT * FROM sys_function") {
            }
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(FunctionService::class.java)
    }
}
