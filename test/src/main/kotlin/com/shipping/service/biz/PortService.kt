package com.shipping.service.biz

import com.shipping.domain.biz.Port
import com.shipping.repository.biz.PortRepository
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
open class PortService {

    @Autowired
    private lateinit var portRepository: PortRepository

    /**
     * 根据港口名取港口信息
     * @param name 港口名
     * @return 港口
     */
    fun getByName(name: String): Port {

        return portRepository.findOne("name", name)
    }

    /**
     * 新增港口
     * @param port 港口
     * @return 港口
     */
    @Transactional
    open fun save(port: Port): Port {
        return portRepository.save(port)
    }

}
