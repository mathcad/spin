package org.infrastructure.jpa.sql;

import org.infrastructure.jpa.api.QueryParam;
import org.infrastructure.jpa.core.DatabaseType;
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
        String order = queryParam.parseOrder("d");
        String pagedSql = "SELECT * FROM (" + sqlSource.getTemplate() + ") as d " + (StringUtils.isBlank(order) ? "" : order) + " LIMIT " + queryParam.getStart() + ", " + queryParam.getLimit();
        ss.setTemplate(pagedSql);
        return ss;
    }
}