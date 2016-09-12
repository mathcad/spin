package org.infrastructure.jpa.sql;

import org.infrastructure.jpa.core.DatabaseType;

/**
 * Created by xuweinan on 2016/9/12.
 */
public class MySQLDatabaseType implements DatabaseType {

    @Override
    public SQLSource getPagedSQL(SQLSource sqlSource, int start, int limit) {
        SQLSource ss = new SQLSource();
        ss.setId(sqlSource.getId());
        String pagedSql = "SELECT * FROM (" + sqlSource.getTemplate() + ") as d " + " LIMIT " + start + ", " + limit;
        ss.setTemplate(pagedSql);
        return ss;
    }
}