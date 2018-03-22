package org.spin.data.core;

import org.spin.data.sql.SQLSource;

/**
 * 描述不同数据库之间的差异，如分页等
 * <p>Created by xuweinan on 2016/8/22.</p>
 *
 * @author xuweinan
 */
public interface DatabaseType {

    /**
     * 数据库产品名称
     *
     * @return 产品名称
     */
    String getProductName();

    /**
     * 获得分页的sql语句
     *
     * @param sqlSource   sql
     * @param pageRequest 分页参数
     * @return 包含分页信息的sql
     */
    SQLSource getPagedSQL(SQLSource sqlSource, PageRequest pageRequest);
}
