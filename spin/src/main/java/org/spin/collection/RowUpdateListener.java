package org.spin.collection;

import java.util.EventListener;

/**
 * Row数据变更监听器
 * <p>Created by xuweinan on 2017/3/20.</p>
 *
 * @author xuweinan
 */
public interface RowUpdateListener extends EventListener {
    void beforeUpdate(RowBeforeUpdateEvent event);

    void afterUpdate(RowAfterUpdateEvent event);
}
