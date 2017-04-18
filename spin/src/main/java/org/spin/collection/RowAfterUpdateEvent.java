package org.spin.collection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventObject;
import java.util.List;

/**
 * Row中的数据更新后的事件
 * <p>Created by xuweinan on 2017/3/20.</p>
 *
 * @author xuweinan
 */
public class RowAfterUpdateEvent extends EventObject {
    private static final long serialVersionUID = -858892121207277827L;

    private List<Integer> updatedCols = new ArrayList<>();

    /**
     * Constructs a prototypical Event.
     *
     * @param source The object on which the Event initially occurred.
     * @throws IllegalArgumentException if source is null.
     */
    public RowAfterUpdateEvent(Object source) {
        super(source);
    }

    public List<Integer> getUpdatedCols() {
        return updatedCols;
    }

    public void setUpdatedCols(List<Integer> updatedCols) {
        this.updatedCols = updatedCols;
    }

    public void setUpdateCols(int... cols) {
        //noinspection unchecked
        this.updatedCols = new ArrayList(Arrays.asList(cols));
    }

    public void addUpdateCols(int col) {
        this.updatedCols.add(col);
    }
}
