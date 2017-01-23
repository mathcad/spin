package org.infrastructure.jpa.sql.loader;

/**
 * 基于jar\war\ear包的SQL装载器
 * Created by xuweinan on 2016/10/15.
 *
 * @author xuweinan
 */
public abstract class ArchiveSQLLoader extends GenericSqlLoader {
    @Override
    public boolean isModified(String id) {
        return false;
    }
}