package org.spin.data.pk.generator;

import org.junit.jupiter.api.Test;
import org.spin.data.pk.DistributedId;
import org.spin.data.pk.PkProperties;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * <p>Created by xuweinan on 2017/5/5.</p>
 *
 * @author xuweinan
 */
public class IdGeneratorTest {

    @Test
    public void testId() {
        PkProperties pkProperties = new PkProperties();
        pkProperties.setGenMethod(0);
        pkProperties.setMachineId(1);
        pkProperties.setType(0);
        pkProperties.setProviderType("PROPERTY");
        pkProperties.setVersion(0);
        IdGenerator<Long, DistributedId> idIdGenerator = new DistributedIdGenerator(pkProperties);

        for (int i = 0; i != 100; ++i) {
            System.out.println(idIdGenerator.genId());
        }
        assertTrue(true);
    }


    @Test
    public void bitTest() {
        long offset = 10;
        System.out.println(-1 ^ -1 << offset);
        System.out.println(-1 ^ (-1 << offset));
        System.out.println(~(-1 << offset));
        System.out.println(~-1 << offset);
        assertTrue(true);
    }
}
