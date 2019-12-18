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

    private static IdMeta maxPeak = new IdMeta((byte) 10, (byte) 20, (byte) 30, (byte) 2, (byte) 1, (byte) 1);

    private static IdMeta minGranularity = new IdMeta((byte) 10, (byte) 10, (byte) 40, (byte) 2, (byte) 1, (byte) 1);

    public static IdMeta getIdMeta(IdTypeE type) {
        if (IdTypeE.MAX_PEAK == type) {
            return maxPeak;
        } else if (IdTypeE.MIN_GRANULARITY == type) {
            return minGranularity;
        }
        return null;
    }
}
