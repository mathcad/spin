package org.spin.data.pk.meta;

/**
 * ID结构定义工厂
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2017/5/5</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class IdMetaFactory {

    /**
     * 最大峰值型
     */
    private static final IdMeta MAX_PEAK = new IdMeta((byte) 10, (byte) 20, (byte) 30, (byte) 2, (byte) 1, (byte) 1);

    /**
     * 最小粒度型
     */
    private static final IdMeta MIN_GRANULARITY = new IdMeta((byte) 10, (byte) 10, (byte) 40, (byte) 2, (byte) 1, (byte) 1);

    public static IdMeta getIdMeta(IdTypeE type) {
        if (IdTypeE.MAX_PEAK == type) {
            return MAX_PEAK;
        } else if (IdTypeE.MIN_GRANULARITY == type) {
            return MIN_GRANULARITY;
        }
        return null;
    }
}
