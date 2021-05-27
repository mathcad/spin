package org.spin.data.pk.generator.provider;

import org.spin.core.util.NumericUtils;
import org.spin.core.util.StringUtils;

/**
 * 机器ID提供者
 * <p>机器ID由10bit表示，最多可以有1024个机器id(0-1023)</p>
 * <p>Created by xuweinan on 2017/5/5</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public interface MachineIdProvider {

    long DEFAULT_MACHINE_ID = NumericUtils.toLong(StringUtils.isEmpty(System.getenv("MACHINE_ID")) ? System.getProperty("MACHINE_ID") : System.getenv("MACHINE_ID"), -1);

    /**
     * 初始化
     *
     * @param initParams 初始化参数
     */
    void init(String initParams);

    /**
     * 获取机器ID (0-1023)
     *
     * @return 机器ID
     */
    long getMachineId();

    default long resolveMachineId() {
        if (DEFAULT_MACHINE_ID < 0) {
            return getMachineId();
        }
        return DEFAULT_MACHINE_ID;
    }
}
