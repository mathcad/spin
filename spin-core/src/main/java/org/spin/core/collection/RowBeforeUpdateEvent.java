package org.spin.core.collection;

import org.spin.core.util.CollectionUtils;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

/**
 * Row中的数据更新前的事件
 * <p>Created by xuweinan on 2017/4/10.</p>
 *
 * @author xuweinan
 */
public class RowBeforeUpdateEvent extends EventObject {
    private static final long serialVersionUID = -1053476441392484983L;

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

    public void setUpdateCols(Integer... cols) {
        this.updateCols = CollectionUtils.ofArrayList(cols);
    }

    public void addUpdateCols(int col) {
        this.updateCols.add(col);
    }
}
