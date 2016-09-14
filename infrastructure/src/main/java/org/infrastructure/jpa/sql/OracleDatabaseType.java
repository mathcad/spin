package org.infrastructure.jpa.sql;

import org.infrastructure.jpa.core.DatabaseType;

/**
 * Created by Arvin on 2016/9/12.
 */
public class OracleDatabaseType implements DatabaseType {
    @Override
    public SQLSource getPagedSQL(SQLSource sqlSource, int start, int limit) {
        SQLSource ss = new SQLSource();
        ss.setId(sqlSource.getId());
        String pagedSql = "SELECT * FROM (SELECT D.*, ROWNUM RN FROM (" + sqlSource.getTemplate() + ") D WHERE ROWNUM > " + start + ") WHERE RN <= " + (start + limit);
        ss.setTemplate(pagedSql);
        return ss;
    }
}