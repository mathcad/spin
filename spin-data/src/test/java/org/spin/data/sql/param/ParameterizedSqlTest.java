package org.spin.data.sql.param;

import org.junit.jupiter.api.Test;
import org.spin.data.sql.SqlSource;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ParameterizedSqlTest {

    @Test
    public void testParse() {
        SqlSource origin = new SqlSource("test", "select * from /*adafdadf*/\n" +
            "sys_user u where u.name = :name and mobile = :{mobile} and address = &address and nick = ? and img = ':img :{img} &img' order by create_time desc \\: adf ?? bbb ?| aaa ?& \n" +
            "::");
        ParameterizedSql parameterizedSql = new ParameterizedSql(origin);
        System.out.println(parameterizedSql.getActualSql().getSql());
        assertTrue(true);
    }
}
