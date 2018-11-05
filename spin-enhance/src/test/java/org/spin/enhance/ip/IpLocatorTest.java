package org.spin.enhance.ip;

import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2018/7/2.</p>
 *
 * @author xuweinan
 */
class IpLocatorTest {

    @Test
    public void test() throws DbMakerConfigException, IOException {
        MemoryIpLocator locator = new MemoryIpLocator();
        DataBlock dataBlock = locator.memorySearch("61.191.211.19");
        System.out.println(dataBlock.getRegion());
    }

}
