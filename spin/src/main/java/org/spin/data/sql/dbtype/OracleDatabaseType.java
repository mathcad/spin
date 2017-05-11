package org.spin.data.sql.dbtype;

import org.spin.core.Assert;
import org.spin.core.util.StringUtils;
import org.spin.data.core.DatabaseType;
import org.spin.data.query.QueryParam;
import org.spin.data.sql.SQLSource;

/**
 * <p>Created by xuweinan on 2016/9/12.</p>
 *
 * @author xuweinan
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