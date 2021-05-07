package org.spin.jpa;

import java.util.ArrayList;
import java.util.List;

/**
 * 联合条件
 */
public class Junction {
    private final List<Object> predicates = new ArrayList<>();

    public void add(Object predicate) {
        this.predicates.add(predicate);
    }

    public List<Object> getPredicates() {
        return this.predicates;
    }
}
