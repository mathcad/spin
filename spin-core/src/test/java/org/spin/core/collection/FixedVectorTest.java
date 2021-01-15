package org.spin.core.collection;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2021/1/5</p>
 *
 * @author xuweinan
 * @version 1.0
 */
class FixedVectorTest {

    @Test
    void testVector() {
        FixedVector<Integer> vector = new FixedVector<>(8);
        vector.push(1);
        vector.push(2);
        vector.push(3);
        vector.addLast(3);
        vector.add(1, 4);
        vector.add(4, 5);
        vector.push(6);
        vector.remove(4);
        vector.remove(2);
        System.out.println(vector.indexOf(3));
        System.out.println(vector.lastIndexOf(3));
        System.out.println("-----");
        System.out.println(Arrays.toString(vector.toArray()));

        System.out.println(vector.pop());
        System.out.println(vector.pop());
        System.out.println(vector.pop());
        System.out.println(vector.pop());
        System.out.println(vector.pop());

        vector = new FixedVector<>(5, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        System.out.println(vector.pop());
        System.out.println(vector.pop());
        System.out.println(vector.pop());
        System.out.println(vector.pop());
        System.out.println(vector.pop());
    }

}
