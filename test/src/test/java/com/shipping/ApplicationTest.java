package com.shipping;

import com.alibaba.druid.filter.config.ConfigTools;
import org.junit.jupiter.api.Test;
import org.spin.core.security.AES;
import org.spin.core.util.CollectionUtils;
import org.spin.core.util.DigestUtils;

import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * <p>Created by xuweinan on 2017/4/20.</p>
 *
 * @author xuweinan
 */
public class ApplicationTest {

    @Test
    public void testDbPassword() {
        System.out.println(AES.newInstance().withKey("c4b2a7d36f9a2e61").encrypt("1q2w3e4r"));
        assertTrue(true);
    }

    @Test
    public void testPassword() throws Exception {
//        System.out.println(DigestUtils.sha256Hex("123" + "xP8F4vjKSYQladtp"));
        ConfigTools.main(CollectionUtils.ofArray("admin"));
        assertTrue(true);
    }
}
