package com.shipping.controller

import com.shipping.domain.biz.Order
import com.shipping.domain.biz.Port
import com.shipping.service.biz.OrderService
import com.shipping.service.biz.PortService
import org.spin.boot.datasource.annotation.Ds
import org.spin.boot.datasource.filter.OpenSessionInViewFilter
import org.spin.web.RestfulResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 运单
 *
 * @author X
 */
@RestController
@RequestMapping("/api/order")
class OrderController {
    @Autowired
    private lateinit var portService: PortService

    @Autowired
    private lateinit var orderService: OrderService

    @Autowired
    private lateinit var applicationContext: ApplicationContext

    /**
     * 发布运单
     *
     * @return
     */
    @PostMapping(path = ["/add"])
    @Ds(openSession = true)
    fun add(senderName: String?,
            senderMobile: String?, senderPortName: String): RestfulResponse<*> {
        val order = Order()
        order.senderName = senderName
        order.senderMobile = senderMobile
        order.senderPort = getPort(senderPortName)
        return RestfulResponse.ok(orderService.save(order))
    }

    private fun getPort(name: String): Port {
        val oldPort = portService.getByName(name)
        return oldPort ?: portService.save(Port().apply { this.name = name })
    }
}
