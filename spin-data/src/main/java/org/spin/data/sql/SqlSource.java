package org.spin.data.sql;

import java.io.Serializable;

/**
 * 定义SQL语句
 * <p>Created by xuweinan on 2016/9/24.</p>
 *
 * @author xuweinan
 */
public class SqlSource implements Serializable {
    private static final long serialVersionUID = 6127332045883688265L;

    private final String id;
    private final String sql;

    public SqlSource(String id, String sql) {
        this.id = id;
        this.sql = sql;
    }

    public String getSql() {
        return sql;
    }

    public String getId() {
        return id;
    }
}
