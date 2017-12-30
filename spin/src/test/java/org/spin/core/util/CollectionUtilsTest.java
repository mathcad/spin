package org.spin.core.util;


import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * <p>Created by xuweinan on 2017/12/28.</p>
 *
 * @author xuweinan
 */
public class CollectionUtilsTest {
    protected static List<String> people = new ArrayList<>();

    static {
        people.add("Mary");
        people.add("Mary");
        people.add("Mary");
        people.add("Bob");
        people.add("Ted");
        people.add("Jake");
        people.add("Barry");
        people.add("Terry");
        people.add("Harry");
        people.add("John");
        people.add("Smith");
        people.add("Spike");
        people.add("Dolly");
        people.add("Spot");
        people.add("Snake");
        people.add("Bird");
        people.add("Turtle");
        people.add("Hamster");
    }

    @Test
    public void selectWith() {
        List<String> mary = CollectionUtils.selectWith(people, String::equals, "Mary");
        assertTrue(mary.size() == 3);
        mary = CollectionUtils.select(people, i -> i.equals("Mary"));
        assertTrue(mary.size() == 3);
        mary = CollectionUtils.rejectWith(people, String::equals, "Mary");
        assertTrue(mary.size() == 15);
        mary = CollectionUtils.reject(people, i -> i.equals("Mary"));
        assertTrue(mary.size() == 15);
        String bird = CollectionUtils.detectWith(people, String::equals, "Bird");
        assertEquals("Bird", bird);
    }
}