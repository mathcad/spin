package org.spin.jpa.lin;

import javax.persistence.criteria.CriteriaDelete;

/**
 * 语言集成删除
 */
public interface Lind<D> extends Lin<Lind<D>, CriteriaDelete<?>> {

    /**
     * 批量删除
     *
     * @return 删除条数
     */
    int delete();
}
