package org.spin.data.extend.dialect;

import org.hibernate.dialect.MySQL55Dialect;
import org.hibernate.dialect.function.SQLFunction;
import org.hibernate.dialect.function.StaticPrecisionFspTimestampFunction;

import java.sql.Types;

/**
 * MySQL5.7方言
 * <p>
 * 屏蔽了外键生成
 * </p>
 * <p>Created by xuweinan on 2018/1/10.</p>
 *
 * @author xuweinan
 */
public class MySQL57WithoutPkDialect extends MySQL55Dialect {
    public MySQL57WithoutPkDialect() {
        super();

        registerColumnType(Types.TIMESTAMP, "datetime(6)");
        registerColumnType(Types.JAVA_OBJECT, "json");
        final SQLFunction currentTimestampFunction = new StaticPrecisionFspTimestampFunction("now", 6);

        registerFunction("now", currentTimestampFunction);
        registerFunction("current_timestamp", currentTimestampFunction);
        registerFunction("localtime", currentTimestampFunction);
        registerFunction("localtimestamp", currentTimestampFunction);

        registerFunction("sysdate", new StaticPrecisionFspTimestampFunction("sysdate", 6));
    }

    @Override
    public boolean supportsRowValueConstructorSyntaxInInList() {
        return true;
    }

    @Override
    public String getAddForeignKeyConstraintString(String constraintName, String[] foreignKey, String referencedTable, String[] primaryKey, boolean referencesPrimaryKey) {
        return String.format(" alter %s set default null", foreignKey[0]);
    }
}
