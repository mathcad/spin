package org.infrastructure.jpa.query;

import org.hibernate.criterion.DetachedCriteria;
import org.springframework.data.domain.PageRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * 离线查询条件封装
 * Created by xuweinan on 2016/12/14.
 *
 * @author xuweinan
 */
public class DetachedCriteriaBag {
    private Class<?> enCls;
    private DetachedCriteria deCriteria;
    private Map<String, String> aliasMap = new HashMap<>();
    private PageRequest pageRequest;

    public Class<?> getEnCls() {
        return enCls;
    }

    public void setEnCls(Class<?> enCls) {
        this.enCls = enCls;
        this.deCriteria = DetachedCriteria.forClass(enCls);
    }

    public DetachedCriteria getDeCriteria() {
        return deCriteria;
    }

    public Map<String, String> getAliasMap() {
        return aliasMap;
    }

    public void setAliasMap(Map<String, String> aliasMap) {
        this.aliasMap = aliasMap;
    }

    public PageRequest getPageRequest() {
        return pageRequest;
    }

    public void setPageRequest(PageRequest pageRequest) {
        this.pageRequest = pageRequest;
    }
}
