package com.shipping.repository.biz

import com.shipping.domain.biz.Port
import org.spin.data.core.ARepository
import org.springframework.stereotype.Repository

/**
 * 港口
 *
 * @author X
 * @contact TEL:18900539326, QQ:396616781
 * @create 2017-09-05 下午11:06
 */
@Repository
class PortRepository : ARepository<Port, Long>()
