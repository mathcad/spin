package org.spin.collection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventObject;
import java.util.List;

/**
 * Created by xuweinan on 2017/4/10.
 */
public class RowBeforeUpdateEvent extends EventObject {

    private List<Integer> updateCols = new ArrayList<>();

    /**
     * Constructs a prototypical Event.
     *
     * @param source The object on which the Event initially occurred.
     * @throws IllegalArgumentException if source is null.
     */
    public RowBeforeUpdateEvent(Object source) {
        super(source);
    }

    public List<Integer> getUpdateCols() {
        return updateCols;
    }

    public void setUpdateCols(List<Integer> updatedCols) {
        this.updateCols = updatedCols;
    }

    public void setUpdateCols(int... cols) {
        //noinspection unchecked
        this.updateCols = new ArrayList(Arrays.asList(cols));
    }

    public void addUpdateCols(int col) {
        this.updateCols.add(col);
    }
}
