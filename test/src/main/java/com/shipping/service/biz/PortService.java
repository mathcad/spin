package com.shipping.service.biz;

import com.shipping.domain.biz.Port;
import com.shipping.repository.biz.PortRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 港口服务
 *
 * @author X
 * @contact TEL:18900539326, QQ:396616781
 * @create 2017-09-05 下午11:07
 */
@Service
public class PortService {

    @Autowired
    private PortRepository portRepository;

    /**
     * 根据港口名取港口信息
     * @param name 港口名
     * @return 港口
     */
    public Port getByName(String name) {

        return portRepository.findOne("name", name);
    }

    /**
     * 新增港口
     * @param port 港口
     * @return 港口
     */
    public Port save(Port port) {
        return portRepository.save(port);
    }

}
