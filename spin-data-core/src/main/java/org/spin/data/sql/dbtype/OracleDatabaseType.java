package org.spin.data.sql.dbtype;

import org.spin.core.Assert;
import org.spin.core.util.StringUtils;
import org.spin.data.core.DatabaseType;
import org.spin.data.core.PageRequest;
import org.spin.data.sql.SqlSource;

/**
 * <p>Created by xuweinan on 2016/9/12.</p>
 *
 * @author xuweinan
 */
public class OracleDatabaseType implements DatabaseType {
    @Override
    public String getProductName() {
        return "Oracle";
    }

    @Override
    public SqlSource getPagedSQL(SqlSource sqlSource, PageRequest pageRequest) {
        Assert.notNull(pageRequest, "Page request must be a NON-NULL value");
        String sql = sqlSource.getSql();
        String order = pageRequest.parseOrder("D");
        if (StringUtils.isNotEmpty(order))
            sql = String.format("SELECT * FROM (%s) D %s", sqlSource.getSql(), order);
        String pagedSql;
        pagedSql = String.format("SELECT * FROM (SELECT O.*, ROWNUM RN FROM (%s) O WHERE ROWNUM > %d) WHERE RN <= %d",
            sql,
            pageRequest.getOffset(),
            pageRequest.getOffset() + pageRequest.getPageSize());
        return new SqlSource(sqlSource.getId(), pagedSql);
    }
}
