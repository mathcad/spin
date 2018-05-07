package org.spin.core.util.excel;

/**
 * description
 *
 * @author wangy QQ 837195190
 * <p>Created by thinkpad on 2018/5/5.<p/>
 */

import org.spin.core.collection.FixedVector;

/**
 * 单行数据读取
 */
/**
 * 单行数据读取
 */
@FunctionalInterface
public interface RowReader {

    /**
     * 顺序读取行
     */
    void readRow(RowData rowData);
}
