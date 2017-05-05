package org.spin.jpa.pk.generator;

import org.junit.Test;
import org.spin.jpa.pk.DistributedId;
import org.spin.jpa.pk.PkProperties;

import static org.junit.Assert.*;

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
