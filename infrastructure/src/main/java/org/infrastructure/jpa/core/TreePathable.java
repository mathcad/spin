package org.infrastructure.jpa.core;

/**
 * 树路径维护
 * 
 * @author zhou
 * @contact 电话: 18963752887, QQ: 251915460
 * @create 2015年3月29日 下午2:45:37
 * @version V1.0
 */
public interface TreePathable<T, PK> {

	/** 获取主键 */
	PK getTreeNodeId();

	/** 获取父级对象 */
	TreePathable<T, PK> getTreeParent();

	/** 获取父级对象 */
	void setTreeParent(T t);

	/** 获取继承路径 */
	void setTreeIdPath(String idPath);

	/** 获取继承路径 */
	String getTreeIdPath();
}
