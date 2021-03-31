package org.spin.datasource;

import org.spin.core.Assert;
import org.spin.core.util.StringUtils;

/**
 * 当前数据源信息
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2021/1/28</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class CurrentDatasourceInfo {
    private String datasource;
    private String catalog;

    public CurrentDatasourceInfo(String ds) {
        String[] split = Assert.notEmpty(ds, "数据源信息不能为空").split("@");
        datasource = Assert.notNull(StringUtils.trimToNull(split[0]), "数据源名称不能为空");
        if (split.length > 1) {
            catalog = datasource;
            datasource = StringUtils.trimToNull(split[1]);
        }
    }

    public CurrentDatasourceInfo(String datasource, String catalog) {
        this.datasource = Assert.notNull(StringUtils.trimToNull(datasource), "数据源名称不能为空");
        this.catalog = StringUtils.trimToNull(catalog);
    }

    public String getDatasource() {
        return datasource;
    }

    public void setDatasource(String datasource) {
        this.datasource = datasource;
    }

    public String getCatalog() {
        return catalog;
    }

    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }

    @Override
    public String toString() {
        return StringUtils.isNotEmpty(catalog) ? (catalog + "@" + datasource) : datasource;
    }
}
