package org.spin.jpa.pk;

/**
 * <p>Created by xuweinan on 2017/5/5.</p>
 *
 * @author xuweinan
 */
public class PkProperties {
    // PROPERTY 或 IP_CONFIGURABLE
    private String providerType;

    // machineId与ips配合providerType，2选一
    private long machineId = -1;
    private String ips;

    private long genMethod = -1;
    private long type = -1;
    private long version = -1;

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
