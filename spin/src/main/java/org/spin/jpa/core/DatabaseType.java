package org.spin.jpa.core;

import org.spin.jpa.query.QueryParam;
import org.spin.jpa.sql.SQLSource;

/**
 * 描述不同数据库之间的差异，如分页等
 * <p>Created by xuweinan on 2016/8/22.
 *
 * @author xuweinan
 */
public interface DatabaseType {
    SQLSource getPagedSQL(SQLSource sqlSource, QueryParam queryParam);
}
