package org.infrastructure.jpa.sql;

import org.infrastructure.jpa.core.DatabaseType;
import org.infrastructure.jpa.query.QueryParam;
import org.infrastructure.sys.Assert;
import org.infrastructure.util.StringUtils;

/**
 * Created by xuweinan on 2016/9/12.
 */
public class MySQLDatabaseType implements DatabaseType {

    @Override
    public SQLSource getPagedSQL(SQLSource sqlSource, QueryParam queryParam) {
        Assert.notNull(queryParam, "Page request must be a NON-NULL value");
        SQLSource ss = new SQLSource();
        ss.setId(sqlSource.getId());
        String order = queryParam.parseOrder("out_alias");
        String pagedSql = "SELECT * FROM (" + sqlSource.getTemplate() + ") as out_alias " + (StringUtils.isBlank(order) ? "" : order);
        if (null != queryParam.getPageSize())
            pagedSql += " LIMIT " + (queryParam.getPageIdx() - 1) * queryParam.getPageSize() + ", " + queryParam.getPageSize();
        ss.setTemplate(pagedSql);
        return ss;
    }
}