package org.spin.boot.properties;

import org.spin.data.pk.PkProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * <p>Created by xuweinan on 2017/9/16.</p>
 *
 * @author xuweinan
 */
@ConfigurationProperties(prefix = "spin.data.pk")
public class IdGenProperties extends PkProperties {
    private String providerType = "PROPERTY";
    private long machineId = 1;
    private String ips = null;
    private long genMethod = 0;
    private long type = 0;
    private long version = 0;

    public String getProviderType() {
        return providerType;
    }

    public void setProviderType(String providerType) {
        this.providerType = providerType;
    }

    public long getMachineId() {
        return machineId;
    }

    public void setMachineId(long machineId) {
        this.machineId = machineId;
    }

    public String getIps() {
        return ips;
    }

    public void setIps(String ips) {
        this.ips = ips;
    }

    public long getGenMethod() {
        return genMethod;
    }

    public void setGenMethod(long genMethod) {
        this.genMethod = genMethod;
    }

    public long getType() {
        return type;
    }

    public void setType(long type) {
        this.type = type;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }
}
