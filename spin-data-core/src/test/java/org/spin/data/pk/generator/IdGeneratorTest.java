package org.spin.data.pk.generator;

import org.junit.jupiter.api.Test;
import org.spin.data.pk.DistributedId;
import org.spin.data.pk.IdGeneratorConfig;
import org.spin.data.pk.generator.provider.ExternalMachineIdProvider;
import org.spin.data.pk.generator.provider.PropertyMachineIdProvider;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * <p>Created by xuweinan on 2017/5/5.</p>
 *
 * @author xuweinan
 */
public class IdGeneratorTest {

    @Test
    public void testId() {
        IdGeneratorConfig idGeneratorConfig = new IdGeneratorConfig();
        idGeneratorConfig.setProviderType(PropertyMachineIdProvider.class);
        idGeneratorConfig.setInitParams("machineId=1");
        IdGenerator<Long, DistributedId> idIdGenerator = new DistributedIdGenerator(idGeneratorConfig);

        for (int i = 0; i != 100; ++i) {
            System.out.println(idIdGenerator.genId());
        }
        assertTrue(true);
    }

    @Test
    void testConfig() {
        ExternalMachineIdProvider idProvider = new ExternalMachineIdProvider();

        idProvider.init("");

        System.out.println(idProvider.resolveMachineId());
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
