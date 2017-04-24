package org.spin.collection;

import org.junit.Test;
import org.spin.sys.auth.KeyInfo;

import java.security.Key;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Created by Arvin on 2017/3/7.
 */
public class MatrixTest {

    @Test
    public void testInsert() {
        assertTrue(true);
    }

    @Test
    public void tesetSet() {
        KeyInfo keyInfo1 = new KeyInfo("1", "123", "aaa", 0L);
        KeyInfo keyInfo2 = new KeyInfo("2", "456", "bbb", 0L);
        KeyInfo keyInfo3 = new KeyInfo("2", "456", "bbb", 0L);
        Set<KeyInfo> keys = new HashSet<>();
        keys.add(keyInfo1);
//        keys.add(keyInfo2);
        keys.add(keyInfo3);

        KeyInfo keyInfo4 = keys.stream().filter(k -> k.getIdentifier().equals("2")).findFirst().orElse(null);
//        keyInfo4.updateKey("234","ccc", 0L);
        keys.add(keyInfo2);
        assertTrue(true);
    }

}
