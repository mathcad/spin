package org.spin.data.pk.generator.provider;

import org.spin.core.Assert;
import org.spin.core.throwable.SpinException;

/**
 * 基于配置的机器ID提供者
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2017/5/5</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class PropertyMachineIdProvider implements MachineIdProvider {

    private long machineId;

    @Override
    public void init(String initParams) {
        String[] strings = initParams.split("=");
        if (strings.length != 2) {
            throw new SpinException(this.getClass().getName() + "[Illegal init params]: " + initParams);
        }

        if (!strings[0].equals("machineId")) {
            throw new SpinException(this.getClass().getName() + "[init params does not contains machineId]: " + initParams);
        }
        try {
            machineId = Assert.inclusiveBetween(0, 1023, Long.parseLong(strings[1]),
                this.getClass().getName() + "[machineId must less then 10 bit(0-1023)]: " + initParams);
        } catch (NumberFormatException ignore) {
            throw new SpinException(this.getClass().getName() + "[machineId isn't a number]: " + initParams);
        }

    }

    @Override
    public long getMachineId() {
        return machineId;
    }
}
