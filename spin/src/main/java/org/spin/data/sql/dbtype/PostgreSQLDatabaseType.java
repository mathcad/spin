package org.spin.data.sql.dbtype;

import org.spin.core.Assert;
import org.spin.core.util.StringUtils;
import org.spin.data.core.DatabaseType;
import org.spin.data.core.PageRequest;
import org.spin.data.sql.SQLSource;

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
    public SQLSource getPagedSQL(SQLSource sqlSource, PageRequest pageRequest) {
        Assert.notNull(pageRequest, "Page request must be a NON-NULL value");
        SQLSource ss = new SQLSource();
        ss.setId(sqlSource.getId());
        String order = pageRequest.parseOrder("out_alias");
        String pagedSql = String.format("SELECT * FROM (%s) AS out_alias %s LIMIT %d OFFSET %d",
            sqlSource.getTemplate(),
            StringUtils.isBlank(order) ? "" : order,
            pageRequest.getPageSize(),
            pageRequest.getOffset());
        ss.setTemplate(pagedSql);
        return ss;
    }
}
