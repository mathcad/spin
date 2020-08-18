package org.spin.data.pk.generator.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.util.NetUtils;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 基于IP的机器ID提供者
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2017/5/5</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class IpConfigurableMachineIdProvider implements MachineIdProvider {
    private static final Logger logger = LoggerFactory.getLogger(IpConfigurableMachineIdProvider.class);

    private long machineId;

    private final Map<String, Long> ipsMap = new HashMap<>();

    @Override
    public void init(String initParams) {
        setIps(initParams);

        String ip = NetUtils.getLocalhost().getHostAddress();

        if (StringUtils.isEmpty(ip)) {
            String msg = "Fail to get host IP address. Stop to initialize the IpConfigurableMachineIdProvider provider.";

            logger.error(msg);
            throw new IllegalStateException(msg);
        }

        if (!ipsMap.containsKey(ip)) {
            String msg = String.format("Fail to configure Id for host IP address %s. Stop to initialize the IpConfigurableMachineIdProvider provider.", ip);

            logger.error(msg);
            throw new IllegalStateException(msg);
        }

        machineId = ipsMap.get(ip);

        logger.info("IpConfigurableMachineIdProvider.init ip {} id {}", ip, machineId);
    }

    @Override
    public long getMachineId() {
        return machineId;
    }

    public void setIps(String ips) {
        logger.debug("IpConfigurableMachineIdProvider ips {}", ips);
        if (!StringUtils.isEmpty(ips)) {
            String[] ipArray = ips.split(",");

            for (int i = 0; i < ipArray.length; i++) {
                ipsMap.put(ipArray[i], (long) i);
            }
        }
    }
}
