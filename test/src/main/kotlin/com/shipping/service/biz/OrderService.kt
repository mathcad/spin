package com.shipping.service.biz

import com.shipping.domain.biz.Order
import com.shipping.repository.biz.OrderRepository
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
open class OrderService {

    @Autowired
    private lateinit var orderRepository: OrderRepository

    /**
     * 新增运单
     * @param order 运单
     * @return 运单
     */
    @Transactional
    open fun save(order: Order): Order {
        return orderRepository.save(order)
    }

}
