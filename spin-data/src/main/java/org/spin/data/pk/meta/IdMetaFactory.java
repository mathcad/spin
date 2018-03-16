package org.spin.data.pk.meta;

public class IdMetaFactory {

    private static IdMeta maxPeak = new IdMeta((byte) 10, (byte) 20, (byte) 30, (byte) 2, (byte) 1, (byte) 1);

    private static IdMeta minGranularity = new IdMeta((byte) 10, (byte) 10, (byte) 40, (byte) 2, (byte) 1, (byte) 1);

    public static IdMeta getIdMeta(IdTypeE type) {
        if (IdTypeE.MAX_PEAK.equals(type)) {
            return maxPeak;
        } else if (IdTypeE.MIN_GRANULARITY.equals(type)) {
            return minGranularity;
        }
        return null;
    }
}
