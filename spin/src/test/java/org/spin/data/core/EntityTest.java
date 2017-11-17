package org.spin.data.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * <p>Created by xuweinan on 2017/11/17.</p>
 *
 * @author xuweinan
 */
public class EntityTest {

    @Test
    public void testRef() {
        File f = IEntity.ref(File.class, 1L);
        Dict d = IEntity.ref(Dict.class, "aa");
        f = new File().ref(2L);
        assertTrue(true);
    }
}
