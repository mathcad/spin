package com.shipping.repository.biz

import com.shipping.domain.biz.Order
import org.spin.data.core.ARepository
import org.springframework.stereotype.Repository

/**
 * 运单
 *
 * @author X
 * @contact TEL:18900539326, QQ:396616781
 * @create 2017-09-04 下午11:04
 */
@Repository
class OrderRepository : ARepository<Order, Long>()
