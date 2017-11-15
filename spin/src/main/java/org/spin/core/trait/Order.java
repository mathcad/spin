package org.spin.core.trait;

/**
 * 排序号
 * <p>Created by xuweinan on 2017/10/27.</p>
 *
 * @author xuweinan
 */
public interface Order {

    /**
     * 排序号，低排序号具有更高的优先级
     *
     * @return 排序号
     */
    default int getOrder() {
        return Integer.MAX_VALUE;
    }
}
