package org.spin.collection;

import java.util.EventListener;

/**
 * Created by xuweinan on 2017/3/20.
 *
 * @author xuweinan
 */
public interface RowUpdateListener extends EventListener {
    void beforeUpdate(RowBeforeUpdateEvent event);

    void afterUpdate(RowAfterUpdateEvent event);
}
