package com.shipping.service

import com.shipping.domain.enums.OrganizationTypeE
import com.shipping.domain.sys.Function
import com.shipping.repository.sys.FunctionRepository
import org.slf4j.LoggerFactory
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
    private lateinit var functionDao: FunctionRepository

    @RestfulMethod
    @Transactional
    open fun add(func: Function) {
        if (Objects.nonNull(func.parent)) {
            OrganizationTypeE.GROUP
            val parent = functionDao.get(func.parent!!.id)
            functionDao.save(func, true)
            functionDao.doWork { connection ->
                val stmt = connection.createStatement()
                val rs = stmt.executeQuery("SELECT * FROM sys_function")
            }
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(FunctionService::class.java)
    }
}
