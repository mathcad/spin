package org.spin.datasource.toolkit;

import org.spin.datasource.CurrentDatasourceInfo;

/**
 * 数据源或schema切换Handler
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2021/3/25</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public interface DynamicDatasourceHandler {

    /**
     * 切换数据源的额外处理逻辑
     *
     * @param current 当前数据源
     * @param target  将要切换的数据源
     * @return 是否允许切换
     */
    boolean handle(CurrentDatasourceInfo current, CurrentDatasourceInfo target);
}
