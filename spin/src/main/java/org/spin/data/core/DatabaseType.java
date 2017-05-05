package org.spin.data.core;

import org.spin.data.query.QueryParam;
import org.spin.data.sql.SQLSource;

/**
 * 描述不同数据库之间的差异，如分页等
 * <p>Created by xuweinan on 2016/8/22.</p>
 *
 * @author xuweinan
 */
public interface DatabaseType {
    SQLSource getPagedSQL(SQLSource sqlSource, QueryParam queryParam);
}
