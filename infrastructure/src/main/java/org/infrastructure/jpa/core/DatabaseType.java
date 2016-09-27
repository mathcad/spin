package org.infrastructure.jpa.core;

import org.infrastructure.jpa.api.QueryParam;
import org.infrastructure.jpa.sql.SQLSource;

/**
 * 描述不同数据库之间的差异，如分页等
 * <p>Created by xuweinan on 2016/8/22.
 *
 * @author xuweinan
 */
public interface DatabaseType {
    SQLSource getPagedSQL(SQLSource sqlSource, QueryParam queryParam);
}
