package org.spin.common.id;

/**
 * snowflake分布式全局唯一id生成类
 * 由 1位符号位 41位时间毫秒序号 5位数据中心 5位机器id 12位流水序号组成
 * 每毫秒 至少可以产生 4000+ 个不重复的id号
 * @author YIJIUE
 */
public class SnowFlake {

    private long workerId;
    private long datacenterId;
    private long sequence = 0L;
    private long timeStamp = 1555227007897L; // 2019年开始的时间单位
    private long workerIdBits = 5L; // 节点ID长度
    private long datacenterIdBits = 5L; // 数据中心ID长度
    private long maxWorkerId = -1L ^ (-1L << workerIdBits); // 最大支持机器节点数0~31，一共32个
    private long maxDatacenterId = -1L ^ (-1L << datacenterIdBits); // 最大支持数据中心节点数0~31，一共32个
    private long sequenceBits = 12L; // 序列号12位
    private long workerIdShift = sequenceBits; // 机器节点左移12位
    private long datacenterIdShift = sequenceBits + workerIdBits; // 数据中心节点左移17位
    private long timestampLeftShift = sequenceBits + workerIdBits + datacenterIdBits; // 时间毫秒数左移22位
    private long sequenceMask = -1L ^ (-1L << sequenceBits); // 4095
    private long lastTimestamp = -1L;

    /**
     * 构造机器节点与数据中心
     */
    public SnowFlake() {
        this(9L, 20L);
    }

    /**
     * 传入工作的机器id与数据中心
     * @param workerId 机器工作id
     * @param datacenterId 数据中心id
     */
    public SnowFlake(long workerId, long datacenterId) {
        if (workerId > maxWorkerId || workerId < 0) {
            throw new IllegalArgumentException(String.format("worker Id can't be greater than %d or less than 0", maxWorkerId));
        }
        if (datacenterId > maxDatacenterId || datacenterId < 0) {
            throw new IllegalArgumentException(String.format("datacenter Id can't be greater than %d or less than 0", maxDatacenterId));
        }
        this.workerId = workerId;
        this.datacenterId = datacenterId;
    }

    /*
     * 获取下一个id
     */
    public long nextId() {
        long timestamp = getCurrentStm(); //获取当前毫秒数
        //如果服务器时间有问题(时钟后退) 报错。
        if (timestamp < lastTimestamp) {
            throw new RuntimeException(String.format(
                    "Clock moved backwards.  Refusing to generate id for %d milliseconds", lastTimestamp - timestamp));
        }
        //如果上次生成时间和当前时间相同,在同一毫秒内
        if (lastTimestamp == timestamp) {
            //sequence自增，因为sequence只有12bit，所以和sequenceMask相与一下，去掉高位
            sequence = (sequence + 1) & sequenceMask;
            //判断是否溢出,也就是每毫秒内超过4095，当为4096时，与sequenceMask相与，sequence就等于0
            if (sequence == 0) {
                timestamp = getNextMill(lastTimestamp); //自旋等待到下一毫秒
            }
        } else {
            sequence = 0L; //如果和上次生成时间不同,重置sequence，就是下一毫秒开始，sequence计数重新从0开始累加
        }
        lastTimestamp = timestamp;
        // 最后按照规则拼出ID。
        // 0        00000000000000000000000000000000000000000  00000            00000       000000000000
        // 符号位   time                                       datacenterId     workerId    sequence
        return ((timestamp - timeStamp) << timestampLeftShift) | (datacenterId << datacenterIdShift)
                | (workerId << workerIdShift) | sequence;
    }

    /**
     * 获取下一时间戳
     * @return
     */
    private long getNextMill(long lastTimestamp) {
        long mill = getCurrentStm();
        while (mill <= lastTimestamp) {
            mill = getCurrentStm();
        }
        return mill;
    }

    /**
     * 获取当前时间戳
     * @return
     */
    private long getCurrentStm() {
        return System.currentTimeMillis();
    }

}
