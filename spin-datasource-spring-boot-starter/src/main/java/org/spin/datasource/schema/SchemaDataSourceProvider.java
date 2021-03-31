package org.spin.datasource.schema;

/**
 * Schema的数据源提供者
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2021/1/28</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public interface SchemaDataSourceProvider {
    String determinDataSource(String schema);
}
