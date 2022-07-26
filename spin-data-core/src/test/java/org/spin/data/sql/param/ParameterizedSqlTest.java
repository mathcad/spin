package org.spin.data.sql.param;

import org.junit.jupiter.api.Test;
import org.spin.data.sql.SqlSource;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ParameterizedSqlTest {

    @Test
    public void testParse() {
        SqlSource origin = new SqlSource("test", "select * from /*adafdadf*/\n" +
            "sys_user u where u.name = :name and id = ? and mobile = :{mobile} and address = &address and nick = ? and img = ':img :{img} &img' order by create_time desc \\: adf ?? bbb ?| aaa ?& \n" +
            "::");
        long s = System.currentTimeMillis();
        ParameterizedSql parameterizedSql = new ParameterizedSql(origin);
        long e = System.currentTimeMillis();
        System.out.println(e - s);
        System.out.println(parameterizedSql.getActualSql().getSql());
        assertTrue(true);
    }
}
