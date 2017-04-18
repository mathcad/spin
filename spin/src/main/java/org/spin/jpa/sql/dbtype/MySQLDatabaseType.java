package org.spin.jpa.sql.dbtype;

import org.spin.jpa.core.DatabaseType;
import org.spin.jpa.query.QueryParam;
import org.spin.jpa.sql.SQLSource;
import org.spin.sys.Assert;
import org.spin.util.StringUtils;

/**
 * <p>Created by xuweinan on 2016/9/12.</p>
 *
 * @author xuweinan
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
