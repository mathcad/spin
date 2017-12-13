package com.shipping.service

import com.shipping.domain.sys.Function
import com.shipping.repository.sys.FunctionRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import java.sql.ResultSet
import java.sql.Statement
import java.util.Objects

/**
 *
 * Created by xuweinan on 2017/9/4.
 *
 * @author xuweinan
 */
@Service
class FunctionService {

    @Autowired
    private val functionDao: FunctionRepository? = null

    fun add(func: Function) {
        if (Objects.nonNull(func.parent)) {
            val parent = functionDao!!.get(func.parent!!.id)
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
