package org.spin.data.pk.generator.provider;

/**
 * 机器ID提供者
 * <p>机器ID由10bit表示，最多可以有1024个机器id(0-1023)</p>
 * <p>Created by xuweinan on 2017/5/5</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public interface MachineIdProvider {

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
}
