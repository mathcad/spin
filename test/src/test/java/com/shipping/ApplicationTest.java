package com.shipping;

import org.junit.jupiter.api.Test;
import org.spin.core.security.AES;
import org.spin.core.util.DigestUtils;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.security.InvalidKeyException;

import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * <p>Created by xuweinan on 2017/4/20.</p>
 *
 * @author xuweinan
 */
public class ApplicationTest {

    @Test
    public void testDbPassword() {
        System.out.println(AES.encrypt("c4b2a7d36f9a2e61", "1q2w3e4r"));
        assertTrue(true);
    }

    @Test
    public void testPassword() {
        System.out.println(DigestUtils.sha256Hex("123" + "xP8F4vjKSYQladtp"));
        assertTrue(true);
    }
}
