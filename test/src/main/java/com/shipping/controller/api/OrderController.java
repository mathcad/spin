package com.shipping.controller.api;

import com.shipping.domain.biz.Order;
import com.shipping.domain.biz.Port;
import com.shipping.service.biz.OrderService;
import com.shipping.service.biz.PortService;
import org.spin.core.util.DateUtils;
import org.spin.web.RestfulResponse;
import org.spin.web.annotation.RestfulApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * 运单
 *
 * @author X
 * @contact TEL:18900539326, QQ:396616781
 * @create 2017-09-04 下午11:04
 */
@RestController
@RequestMapping("/api/order")
public class OrderController {

    @Autowired
    private PortService portService;

    @Autowired
    private OrderService orderService;

    /**
     * 发布运单
     * @return
     */
    @RestfulApi(auth = false, path = "/add", method = RequestMethod.POST)
    public RestfulResponse add(String planLoadTime, Integer planLoadRange, String planUnloadTime, String senderName,
                               String senderMobile, String senderPortName, String receiverName, String receiverMobile,
                               String receiverPortName, String cargoName, Float cargoQuantity, Float cargoWeight,
                               Double deposit, Double prepay, Double fullpay, Double sumPay) {

        Order order = new Order();

        order.setPlanLoadTime(DateUtils.toLocalDateTime(planLoadTime));
        order.setPlanLoadRange(planLoadRange);
        order.setPlanUnloadTime(DateUtils.toLocalDateTime(planUnloadTime));
        order.setSenderName(senderName);
        order.setSenderMobile(senderMobile);
        order.setSenderPort(getPort(senderPortName));
        order.setReceiverName(receiverName);
        order.setReceiverMobile(receiverMobile);
        order.setReceiverPort(getPort(receiverPortName));
        order.setCargoName(cargoName);
        order.setCargoQuantity(cargoQuantity);
        order.setCargoWeight(cargoWeight);
        order.setDeposit(deposit);
        order.setPrepay(prepay);
        order.setFullpay(fullpay);
        order.setSumPay(sumPay);

        return RestfulResponse.ok(orderService.save(order));
    }

    private Port getPort(String name) {

        Port oldPort = portService.getByName(name);

        if (null != oldPort) {
            return oldPort;
        }

        Port port = new Port();
        port.setName(name);

        return portService.save(port);
    }
}
