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
public class SQLServerDatabaseType implements DatabaseType {

    @Override
    public String getProductName() {
        return "SQL Server";
    }

    @Override
    public SqlSource getPagedSQL(SqlSource sqlSource, PageRequest pageRequest) {
        // TODO: 未完成
        Assert.notNull(pageRequest, "Page request must be a NON-NULL value");
        String orderedSql = sqlSource.getSql();
        String order = pageRequest.parseOrder("I_");
//        String tmp = "SELECT D.* FROM (SELECT TOP 60 row_number() OVER ({order}) rwn, I.* FROM (sql) I) D WHERE D.rwn > {}";
        if (StringUtils.isNotEmpty(order)) {
            orderedSql = "SELECT D_.* FROM (SELECT TOP "
                + pageRequest.getOffset()
                + " row_number() OVER (" + order + ") rwn, I_.* FROM ("
                + orderedSql + ") I_) D_ WHERE D_.rwn > "
                + pageRequest.getOffset();
        }
        String pagedSql = orderedSql;
        pagedSql = "SELECT * FROM (SELECT O.*, ROWNUM RN FROM (" + orderedSql + ") O WHERE ROWNUM > "
            + pageRequest.getOffset() + ") WHERE RN <= "
            + (pageRequest.getOffset() + pageRequest.getPageSize());
        return new SqlSource(sqlSource.getId(), pagedSql);
    }
}
