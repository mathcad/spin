package org.infrastructure.jpa.sql;

import org.infrastructure.jpa.core.DatabaseType;
import org.infrastructure.jpa.query.QueryParam;
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
        String pagedSql = sql;
        if (null != queryParam.getPageSize())
            pagedSql = "SELECT * FROM (SELECT O.*, ROWNUM RN FROM (" + sql + ") O WHERE ROWNUM > "
                    + (queryParam.getPageIdx() - 1) * queryParam.getPageSize() + ") WHERE RN <= "
                    + ((queryParam.getPageIdx() - 1) * queryParam.getPageSize() + queryParam.getPageSize());
        ss.setTemplate(pagedSql);
        return ss;
    }
}