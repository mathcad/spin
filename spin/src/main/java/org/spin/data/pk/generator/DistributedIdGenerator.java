package org.spin.data.pk.generator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.util.EnumUtils;
import org.spin.data.pk.DistributedId;
import org.spin.data.pk.PkProperties;
import org.spin.data.pk.converter.DistributedIdConverter;
import org.spin.data.pk.converter.IdConverter;
import org.spin.data.pk.generator.provider.IpConfigurableMachineIdProvider;
import org.spin.data.pk.generator.provider.MachineIdProvider;
import org.spin.data.pk.generator.provider.PropertyMachineIdProvider;
import org.spin.data.pk.meta.IdMeta;
import org.spin.data.pk.meta.IdMetaFactory;
import org.spin.data.pk.meta.IdTypeE;

import java.util.Date;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DistributedIdGenerator implements IdGenerator<Long, DistributedId> {
    private static final Logger logger = LoggerFactory.getLogger(DistributedIdGenerator.class);
    private static final long EPOCH = 1420041600000L;

    private long machineId = -1;
    private long sequence = 0;
    private long lastTimestamp = -1;
    private long genMethod = 0;
    private long type = 0;
    private long version = 0;

    private IdTypeE idType;
    private IdMeta idMeta;
    private Lock lock = new ReentrantLock();

    private IdConverter<Long, DistributedId> idConverter;
    private MachineIdProvider machineIdProvider;

    public DistributedIdGenerator(PkProperties pkProperties) {
        MachineIdProvider machineIdProvider = null;
        if ("PROPERTY".equals(pkProperties.getProviderType())) {
            machineIdProvider = new PropertyMachineIdProvider();
            ((PropertyMachineIdProvider) machineIdProvider).setMachineId(pkProperties.getMachineId());
        } else if ("IP_CONFIGURABLE".equals(pkProperties.getProviderType())) {
            machineIdProvider = new IpConfigurableMachineIdProvider(pkProperties.getIps());
        }

        if (machineIdProvider == null) {
            throw new IllegalArgumentException("The type of DistributedId service is mandatory.");
        }

        if (pkProperties.getType() != -1)
            idType = EnumUtils.getEnum(IdTypeE.class, (int) pkProperties.getType());
        else
            idType = IdTypeE.MAX_PEAK;
        this.machineIdProvider = machineIdProvider;
        if (pkProperties.getGenMethod() != -1)
            genMethod = pkProperties.getGenMethod();
        if (pkProperties.getVersion() != -1)
            version = pkProperties.getVersion();
        init();
    }

    private void init() {
        this.machineId = machineIdProvider.getMachineId();

        if (machineId < 0) {
            logger.error("The machine Id is not configured properly so that Vesta Service refuses to start.");
            throw new IllegalStateException("The machine Id is not configured properly so that Vesta Service refuses to start.");
        }

        setIdMeta(IdMetaFactory.getIdMeta(idType));
        setType(idType.value());
        setIdConverter(new DistributedIdConverter(idType));
    }

    public Long genId() {
        DistributedId id = new DistributedId();
        populateId(id);
        id.setMachine(machineId);
        id.setGenMethod(genMethod);
        id.setType(type);
        id.setVersion(version);

        long ret = idConverter.convert(id);
        // Use trace because it cause low performance
        if (logger.isTraceEnabled())
            logger.trace(String.format("DistributedId: %s => %d", id, ret));
        return ret;
    }

    public DistributedId expId(Long id) {
        return idConverter.convert(id);
    }

    public long makeId(long time, long seq) {
        return makeId(time, seq, machineId);
    }

    public long makeId(long time, long seq, long machine) {
        return makeId(genMethod, time, seq, machine);
    }

    public long makeId(long genMethod, long time, long seq, long machine) {
        return makeId(type, genMethod, time, seq, machine);
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
        if (idType == IdTypeE.MAX_PEAK) {
            return new Date(time * 1000 + EPOCH);
        } else if (idType == IdTypeE.MIN_GRANULARITY) {
            return new Date(time + EPOCH);
        }

        return null;
    }

    public void setMachineId(long machineId) {
        this.machineId = machineId;
    }

    public void setGenMethod(long genMethod) {
        this.genMethod = genMethod;
    }

    public void setType(long type) {
        this.type = type;
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
        if (idType == IdTypeE.MAX_PEAK)
            return (System.currentTimeMillis() - EPOCH) / 1000;
        else if (idType == IdTypeE.MIN_GRANULARITY)
            return (System.currentTimeMillis() - EPOCH);

        return (System.currentTimeMillis() - EPOCH) / 1000;
    }

    private void populateId(DistributedId id) {
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

            id.setSeq(sequence);
            id.setTime(timestamp);

        } finally {
            lock.unlock();
        }
    }

    private void validateTimestamp(long lastTimestamp, long timestamp) {
        if (timestamp < lastTimestamp) {
            if (logger.isErrorEnabled()) {
                logger.error(String.format("Clock moved backwards.  Refusing to generate id for %d %s.", lastTimestamp - timestamp,
                    idType == IdTypeE.MAX_PEAK ? "second" : "milisecond"));
            }
            throw new IllegalStateException(String.format("Clock moved backwards.  Refusing to generate id for %d %s.",
                lastTimestamp - timestamp, idType == IdTypeE.MAX_PEAK ? "second" : "milisecond"));
        }
    }

    private long tillNextTimeUnit(final long lastTimestamp) {
        if (logger.isInfoEnabled()) {
            logger.info(String.format("Ids are used out during %d in machine %d. Waiting till next %s.", lastTimestamp, machineId, idType == IdTypeE.MAX_PEAK ? "second" : "milisecond"));
        }
        long timestamp = this.genTime();
        while (timestamp <= lastTimestamp) {
            timestamp = this.genTime();
        }

        if (logger.isInfoEnabled()) {
            logger.info(String.format("Next %s %d is up.", idType == IdTypeE.MAX_PEAK ? "second" : "milisecond", timestamp));
        }
        return timestamp;
    }
}
