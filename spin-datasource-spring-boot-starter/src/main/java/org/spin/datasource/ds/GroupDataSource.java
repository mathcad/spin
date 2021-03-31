package org.spin.datasource.ds;

import org.spin.datasource.strategy.DynamicDataSourceStrategy;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 组数据源
 *
 * @author TaoYu
 */
public class GroupDataSource {

    private String groupName;

    private DynamicDataSourceStrategy dynamicDataSourceStrategy;

    private Map<String, DataSource> dataSourceMap = new ConcurrentHashMap<>();

    public GroupDataSource() {
    }

    public GroupDataSource(String groupName, DynamicDataSourceStrategy dynamicDataSourceStrategy) {
        this.groupName = groupName;
        this.dynamicDataSourceStrategy = dynamicDataSourceStrategy;
    }

    /**
     * add a new datasource to this group
     *
     * @param ds         the name of the datasource
     * @param dataSource datasource
     * @return the previous datasource associated with the nameo
     */
    public DataSource addDatasource(String ds, DataSource dataSource) {
        return dataSourceMap.put(ds, dataSource);
    }

    /**
     * @param ds the name of the datasource
     * @return the removed datasource
     */
    public DataSource removeDatasource(String ds) {
        return dataSourceMap.remove(ds);
    }

    public DataSource determineDataSource() {
        return dynamicDataSourceStrategy.determineDataSource(new ArrayList<>(dataSourceMap.values()));
    }

    public int size() {
        return dataSourceMap.size();
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public DynamicDataSourceStrategy getDynamicDataSourceStrategy() {
        return dynamicDataSourceStrategy;
    }

    public void setDynamicDataSourceStrategy(DynamicDataSourceStrategy dynamicDataSourceStrategy) {
        this.dynamicDataSourceStrategy = dynamicDataSourceStrategy;
    }

    public Map<String, DataSource> getDataSourceMap() {
        return dataSourceMap;
    }

    public void setDataSourceMap(Map<String, DataSource> dataSourceMap) {
        this.dataSourceMap = dataSourceMap;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GroupDataSource)) return false;
        GroupDataSource that = (GroupDataSource) o;
        return Objects.equals(groupName, that.groupName) && Objects.equals(dynamicDataSourceStrategy, that.dynamicDataSourceStrategy) && Objects.equals(dataSourceMap, that.dataSourceMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupName, dynamicDataSourceStrategy, dataSourceMap);
    }

    @Override
    public String toString() {
        return "GroupDataSource{" +
            "groupName='" + groupName + '\'' +
            ", dynamicDataSourceStrategy=" + dynamicDataSourceStrategy +
            ", dataSourceMap=" + dataSourceMap +
            '}';
    }
}
