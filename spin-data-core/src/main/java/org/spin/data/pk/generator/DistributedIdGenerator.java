package org.spin.data.pk.generator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.util.BeanUtils;
import org.spin.core.util.EnumUtils;
import org.spin.data.pk.DistributedId;
import org.spin.data.pk.IdGeneratorConfig;
import org.spin.data.pk.converter.DistributedIdConverter;
import org.spin.data.pk.converter.IdConverter;
import org.spin.data.pk.generator.provider.MachineIdProvider;
import org.spin.data.pk.meta.IdMeta;
import org.spin.data.pk.meta.IdMetaFactory;
import org.spin.data.pk.meta.IdTypeE;

import java.util.Date;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 分布式ID生成器的默认实现
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2017/5/5</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class DistributedIdGenerator implements IdGenerator<Long, DistributedId> {
    private static final Logger logger = LoggerFactory.getLogger(DistributedIdGenerator.class);
    private static final long EPOCH = 1420041600000L;

    private long sequence = 0;
    private long lastTimestamp = -1;
    private long genMethod = 0;
    private long version = 0;

    private long idType;
    private IdMeta idMeta;
    private final Lock lock = new ReentrantLock();

    private IdConverter<Long, DistributedId> idConverter;
    private MachineIdProvider machineIdProvider;

    private final DistributedId id = new DistributedId();

    public DistributedIdGenerator(IdGeneratorConfig idGeneratorConfig) {
        machineIdProvider = BeanUtils.instantiateClass(idGeneratorConfig.getProviderType());
        machineIdProvider.init(idGeneratorConfig.getInitParams());

        if (machineIdProvider.resolveMachineId() < 0) {
            logger.error("The machine Id is not configured properly so that Vesta Service refuses to start.");
            throw new IllegalStateException("The machine Id is not configured properly so that Vesta Service refuses to start.");
        }

        if (idGeneratorConfig.getIdType() == null) {
            idGeneratorConfig.setIdType(IdTypeE.MAX_PEAK);
        }
        idType = idGeneratorConfig.getIdType().getValue();

        if (idGeneratorConfig.getGenMethod() != null) {
            genMethod = idGeneratorConfig.getGenMethod().getValue();
        }

        if (idGeneratorConfig.getVersion() != -1) {
            version = idGeneratorConfig.getVersion();
        }

        setIdMeta(IdMetaFactory.getIdMeta(idGeneratorConfig.getIdType()));
        setIdConverter(new DistributedIdConverter(idGeneratorConfig.getIdType()));

        id.setGenMethod(genMethod);
        id.setType(idType);
        id.setVersion(version);
    }

    @Override
    public Long genId() {
        Long ret = populateId(id);
        if (logger.isTraceEnabled()) {
            logger.trace(String.format("DistributedId: %s => %d", id, ret));
        }
        return ret;
    }

    @Override
    public DistributedId expId(Long id) {
        return idConverter.convert(id);
    }

    @Override
    public Class<Long> getIdType() {
        return Long.class;
    }

    public long makeId(long time, long seq) {
        return makeId(time, seq, machineIdProvider.resolveMachineId());
    }

    public long makeId(long time, long seq, long machine) {
        return makeId(genMethod, time, seq, machine);
    }

    public long makeId(long genMethod, long time, long seq, long machine) {
        return makeId(idType, genMethod, time, seq, machine);
    }

    public long makeId(long type, long genMethod, long time, long seq, long machine) {
        return makeId(version, type, genMethod, time, seq, machine);
    }

    public long makeId(long version, long type, long genMethod, long time, long seq, long machine) {
        IdTypeE idType = EnumUtils.getEnum(IdTypeE.class, (int) type);

        DistributedId id = new DistributedId(machine, seq, time, genMethod, type, version);
        IdConverter<Long, DistributedId> idConverter = new DistributedIdConverter(idType);

        return idConverter.convert(id);
    }

    public Date transTime(long time) {
        if (idType == IdTypeE.MAX_PEAK.getValue()) {
            return new Date(time * 1000 + EPOCH);
        } else if (idType == IdTypeE.MIN_GRANULARITY.getValue()) {
            return new Date(time + EPOCH);
        }

        return null;
    }

    public void setGenMethod(long genMethod) {
        this.genMethod = genMethod;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public void setIdConverter(IdConverter<Long, DistributedId> idConverter) {
        this.idConverter = idConverter;
    }

    public void setIdMeta(IdMeta idMeta) {
        this.idMeta = idMeta;
    }

    public void setMachineIdProvider(MachineIdProvider machineIdProvider) {
        this.machineIdProvider = machineIdProvider;
    }

    private long genTime() {
        return idType == IdTypeE.MIN_GRANULARITY.getValue() ?
            (System.currentTimeMillis() - EPOCH) : ((System.currentTimeMillis() - EPOCH) / 1000);
    }

    private Long populateId(DistributedId id) {
        lock.lock();
        try {
            long timestamp = this.genTime();
            validateTimestamp(lastTimestamp, timestamp);

            if (timestamp == lastTimestamp) {
                sequence++;
                sequence &= idMeta.getSeqBitsMask();
                if (sequence == 0) {
                    timestamp = this.tillNextTimeUnit(lastTimestamp);
                }
            } else {
                lastTimestamp = timestamp;
                sequence = 0;
            }

            id.setMachine(machineIdProvider.resolveMachineId());
            id.setSeq(sequence);
            id.setTime(timestamp);
            return idConverter.convert(id);
        } finally {
            lock.unlock();
        }
    }

    private void validateTimestamp(long lastTimestamp, long timestamp) {
        if (timestamp < lastTimestamp) {
            if (logger.isErrorEnabled()) {
                logger.error(String.format("Clock moved backwards.  Refusing to generate id for %d %s.", lastTimestamp - timestamp,
                    idType == IdTypeE.MAX_PEAK.getValue() ? "second" : "milisecond"));
            }
            throw new IllegalStateException(String.format("Clock moved backwards.  Refusing to generate id for %d %s.",
                lastTimestamp - timestamp, idType == IdTypeE.MAX_PEAK.getValue() ? "second" : "milisecond"));
        }
    }

    private long tillNextTimeUnit(final long lastTimestamp) {
        if (logger.isInfoEnabled()) {
            logger.info(String.format("Ids are used out during %d in machine %d. Waiting till next %s.",
                lastTimestamp, machineIdProvider.resolveMachineId(), idType == IdTypeE.MAX_PEAK.getValue() ? "second" : "milisecond"));
        }
        long timestamp = this.genTime();
        while (timestamp <= lastTimestamp) {
            timestamp = this.genTime();
        }

        if (logger.isInfoEnabled()) {
            logger.info(String.format("Next %s %d is up.", idType == IdTypeE.MAX_PEAK.getValue() ? "second" : "milisecond", timestamp));
        }
        return timestamp;
    }
}
