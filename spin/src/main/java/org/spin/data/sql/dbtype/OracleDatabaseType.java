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
public class OracleDatabaseType implements DatabaseType {
    @Override
    public String getProductName() {
        return "Oracle";
    }

    @Override
    public SQLSource getPagedSQL(SQLSource sqlSource, PageRequest pageRequest) {
        Assert.notNull(pageRequest, "Page request must be a NON-NULL value");
        SQLSource ss = new SQLSource();
        ss.setId(sqlSource.getId());
        String sql = sqlSource.getTemplate();
        String order = pageRequest.parseOrder("D");
        if (StringUtils.isNotEmpty(order))
            sql = "SELECT * FROM (" + sqlSource.getTemplate() + ") D " + order;
        String pagedSql = sql;
        pagedSql = "SELECT * FROM (SELECT O.*, ROWNUM RN FROM (" + sql + ") O WHERE ROWNUM > "
            + pageRequest.getOffset() + ") WHERE RN <= "
            + (pageRequest.getOffset() + pageRequest.getPageSize());
        ss.setTemplate(pagedSql);
        return ss;
    }
}
