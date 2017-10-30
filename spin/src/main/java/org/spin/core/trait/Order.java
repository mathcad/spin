package org.spin.core.trait;

/**
 * 排序号
 * <p>Created by xuweinan on 2017/10/27.</p>
 *
 * @author xuweinan
 */
public interface Order {

    default int getOrder() {
        return Integer.MAX_VALUE;
    }
}
