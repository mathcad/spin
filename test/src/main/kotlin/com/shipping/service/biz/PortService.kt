package com.shipping.service.biz

import com.shipping.domain.biz.Port
import org.spin.data.extend.RepositoryContext
import org.spin.data.query.CriteriaBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 港口服务
 *
 * @author X
 * @contact TEL:18900539326, QQ:396616781
 * @create 2017-09-05 下午11:07
 */
@Service
class PortService {

    @Autowired
    private lateinit var repoCtx: RepositoryContext

    /**
     * 根据港口名取港口信息
     * @param name 港口名
     * @return 港口
     */
    fun getByName(name: String): Port = repoCtx.findOne(Port::class.java, "name", name)

    /**
     * 新增港口
     * @param port 港口
     * @return 港口
     */
    @Transactional
    fun save(port: Port): Port {
        CriteriaBuilder.forClass(Port::class.java).createAlias(Port::name, "portName")
        val property1 = Port::name
        return repoCtx.save(port)
    }

}
