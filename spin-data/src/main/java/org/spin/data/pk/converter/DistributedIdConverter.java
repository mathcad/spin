package org.spin.data.pk.converter;

import org.spin.data.pk.DistributedId;
import org.spin.data.pk.meta.IdMeta;
import org.spin.data.pk.meta.IdMetaFactory;
import org.spin.data.pk.meta.IdTypeE;

public class DistributedIdConverter implements IdConverter<Long, DistributedId> {

    private IdTypeE idType;

    public DistributedIdConverter() {
    }

    public DistributedIdConverter(IdTypeE idType) {
        this.idType = idType;
    }

    public Long convert(DistributedId id) {
        return doConvert(id, IdMetaFactory.getIdMeta(idType));
    }

    protected Long doConvert(DistributedId id, IdMeta idMeta) {
        long ret = 0;

        ret |= id.getMachine();

        ret |= id.getSeq() << idMeta.getSeqBitsStartPos();

        ret |= id.getTime() << idMeta.getTimeBitsStartPos();

        ret |= id.getGenMethod() << idMeta.getGenMethodBitsStartPos();

        ret |= id.getType() << idMeta.getTypeBitsStartPos();

        ret |= id.getVersion() << idMeta.getVersionBitsStartPos();

        return ret;
    }

    public DistributedId convert(Long id) {
        return doConvert(id, IdMetaFactory.getIdMeta(idType));
    }

    protected DistributedId doConvert(long id, IdMeta idMeta) {
        DistributedId ret = new DistributedId();

        ret.setMachine(id & idMeta.getMachineBitsMask());

        ret.setSeq((id >>> idMeta.getSeqBitsStartPos()) & idMeta.getSeqBitsMask());

        ret.setTime((id >>> idMeta.getTimeBitsStartPos()) & idMeta.getTimeBitsMask());

        ret.setGenMethod((id >>> idMeta.getGenMethodBitsStartPos()) & idMeta.getGenMethodBitsMask());

        ret.setType((id >>> idMeta.getTypeBitsStartPos()) & idMeta.getTypeBitsMask());

        ret.setVersion((id >>> idMeta.getVersionBitsStartPos()) & idMeta.getVersionBitsMask());

        return ret;
    }

    public IdTypeE getIdType() {
        return idType;
    }

    public void setIdType(IdTypeE idType) {
        this.idType = idType;
    }
}
