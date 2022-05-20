package org.spin.core.juc;

import org.junit.jupiter.api.Test;
import org.openjdk.jol.info.ClassLayout;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2021/6/22</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class ObjectTest {
    private final A aa = new A();

    @Test
    void testObjLayout() {
        synchronized (aa) {
            System.out.println(Long.toHexString(Thread.currentThread().getId()));
            System.out.println(aa.hashCode());
            System.out.println(ClassLayout.parseInstance(aa).toPrintable());
        }
        A a = new A();
        System.out.println(a.hashCode());
        System.out.println(ClassLayout.parseInstance(a).toPrintable());
//        String[] s = {"1","2","3","4","5","6","7","8"};
//        System.out.println(ClassLayout.parseInstance(a).toPrintable());
    }
}

class A {
    private boolean field1;
    private boolean field2;
    private short field3;

    public boolean isField1() {
        return field1;
    }

    public void setField1(boolean field1) {
        this.field1 = field1;
    }

    public boolean isField2() {
        return field2;
    }

    public void setField2(boolean field2) {
        this.field2 = field2;
    }

    @Override
    public int hashCode() {
        return 0x11111111;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}
