package org.spin.jpa.dialect;

import org.hibernate.dialect.MySQL55Dialect;

/**
 * MySQL5.5方言
 * <p>屏蔽了外键生成</p>
 * <p>Created by xuweinan on 2018/1/10.</p>
 *
 * @author xuweinan
 */
public class MySQL55WithoutFkDialect extends MySQL55Dialect {
    @Override
    public String getAddForeignKeyConstraintString(String constraintName, String[] foreignKey, String referencedTable, String[] primaryKey, boolean referencesPrimaryKey) {
        return String.format(" alter %s set default null", foreignKey[0]);
    }
}
