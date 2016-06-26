package org.infrastructure.jpa.core;

/**
 * 树路径维护
 *
 * @author xuweinan
 * @version V1.0
 */
public interface TreePathable<T, PK> {

    /**
     * 获取主键
     */
    PK getTreeNodeId();

    /**
     * 获取父级对象
     */
    TreePathable<T, PK> getTreeParent();

    /**
     * 获取父级对象
     */
    void setTreeParent(T t);

    /**
     * 获取继承路径
     */
    void setTreeIdPath(String idPath);

    /**
     * 获取继承路径
     */
    String getTreeIdPath();
}
