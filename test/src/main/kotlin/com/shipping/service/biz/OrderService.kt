package com.shipping.service.biz

import com.shipping.domain.biz.Order
import org.spin.data.extend.RepositoryContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import javax.transaction.Transactional

/**
 * 运单
 *
 * @author X
 * @contact TEL:18900539326, QQ:396616781
 * @create 2017-09-04 下午11:05
 */
@Service
class OrderService {

    @Autowired
    private lateinit var repoCtx: RepositoryContext

    /**
     * 新增运单
     * @param order 运单
     * @return 运单
     */
    @Transactional
    fun save(order: Order): Order {
        return repoCtx.save(order)
    }

}
