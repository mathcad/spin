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
public class PostgreSQLDatabaseType implements DatabaseType {

    @Override
    public String getProductName() {
        return "PostgreSQL";
    }

    @Override
    public SqlSource getPagedSQL(SqlSource sqlSource, PageRequest pageRequest) {
        Assert.notNull(pageRequest, "Page request must be a NON-NULL value");
        String order = pageRequest.parseOrder("out_alias");
        String pagedSql = String.format("SELECT * FROM (%s) AS out_alias %s LIMIT %d OFFSET %d",
            sqlSource.getSql(),
            StringUtils.isBlank(order) ? "" : order,
            pageRequest.getSize(),
            pageRequest.getOffset());
        return new SqlSource(sqlSource.getId(), pagedSql);
    }
}
