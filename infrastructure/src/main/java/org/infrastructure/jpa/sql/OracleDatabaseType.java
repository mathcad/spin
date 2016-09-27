package org.infrastructure.jpa.sql;

import org.infrastructure.jpa.api.QueryParam;
import org.infrastructure.jpa.core.DatabaseType;
import org.infrastructure.sys.Assert;
import org.infrastructure.util.StringUtils;

/**
 * Created by Arvin on 2016/9/12.
 */
public class OracleDatabaseType implements DatabaseType {
    @Override
    public SQLSource getPagedSQL(SQLSource sqlSource, QueryParam queryParam) {
        Assert.notNull(queryParam, "Page request must be a NON-NULL value");
        SQLSource ss = new SQLSource();
        ss.setId(sqlSource.getId());
        String sql = sqlSource.getTemplate();
        String order = queryParam.parseOrder("D");
        if (StringUtils.isNotEmpty(order))
            sql = "SELECT * FROM (" + sqlSource.getTemplate() + ") D " + order;
        String pagedSql = "SELECT * FROM (SELECT O.*, ROWNUM RN FROM (" + sql + ") O WHERE ROWNUM > " + queryParam.getStart() + ") WHERE RN <= " + (queryParam.getStart() + queryParam.getLimit());
        ss.setTemplate(pagedSql);
        return ss;
    }
}