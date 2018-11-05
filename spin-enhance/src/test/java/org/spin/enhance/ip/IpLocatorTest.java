package org.spin.enhance.ip;

import org.junit.jupiter.api.Test;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2018/7/2.</p>
 *
 * @author xuweinan
 */
class IpLocatorTest {

    @Test
    public void test() throws DbMakerConfigException {
        MemoryIpLocator locator = new MemoryIpLocator();
        DataBlock dataBlock = locator.search("61.191.211.19");
        System.out.println(dataBlock.getRegion());
    }

}
