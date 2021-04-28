package org.spin.datasource.schema;

import java.sql.Connection;

/**
 * 事务上下文的当前jdbc connection提供者
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2021/4/27</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public interface TransactionalSyncConnectionProvider {
    Connection currentConnection();
}
