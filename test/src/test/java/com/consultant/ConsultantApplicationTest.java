package com.consultant;

import org.junit.Test;
import org.spin.security.AES;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.security.InvalidKeyException;

import static org.junit.Assert.*;

/**
 * <p>Created by xuweinan on 2017/4/20.</p>
 *
 * @author xuweinan
 */
public class ConsultantApplicationTest {

    @Test
    public void testDbPassword() throws BadPaddingException, InvalidKeyException, IllegalBlockSizeException {
        System.out.println(AES.encrypt("c4b2a7d36f9a2e61", "admin"));
        assertTrue(true);
    }
}
