package org.spin.data.pk;

import java.io.Serializable;

/**
 * 分布式ID默认实现
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2017/5/5</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class DistributedId implements Id, Serializable {

    private static final long serialVersionUID = 6870931236218221183L;

    /**
     * 机器ID 0-9
     */
    private long machine;

    /**
     * 序列号 10-39, 10-19
     */
    private long seq;

    /**
     * 秒级时间 40-59, 毫秒级时间 20-59
     */
    private long time;

    /**
     * 生成方式 60-61 2位，用来区分三种发布模式：嵌入发布模式，中心服务器发布模式，REST发布模式。
     */
    private long genMethod;

    /**
     * 类型 62 1位，用来区分两种ID类型：最大峰值型和最小粒度型。
     */
    private long type;

    /**
     * 版本 63 扩展备用
     */
    private long version;

    public DistributedId(long machine, long seq, long time, long genMethod, long type, long version) {
        this.machine = machine;
        this.seq = seq;
        this.time = time;
        this.genMethod = genMethod;
        this.type = type;
        this.version = version;
    }

    public DistributedId() {
    }

    public long getMachine() {
        return machine;
    }

    public void setMachine(long machine) {
        this.machine = machine;
    }

    public long getSeq() {
        return seq;
    }

    public void setSeq(long seq) {
        this.seq = seq;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
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

    @Override
    public String toString() {

        return "[" +
            "machine=" + machine + "," +
            "seq=" + seq + "," +
            "time=" + time + "," +
            "genMethod=" + genMethod + "," +
            "type=" + type + "," +
            "version=" + version + "]";
    }

}
