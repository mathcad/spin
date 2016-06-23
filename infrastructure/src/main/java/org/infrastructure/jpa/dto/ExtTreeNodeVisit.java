package org.infrastructure.jpa.dto;

/**   
 * 查找遍历
 *  
 * @author zhou
 * @contact 电话: 18963752887, QQ: 251915460
 * @create 2015年4月2日 下午5:29:31 
 * @version V1.0   
 */
public interface ExtTreeNodeVisit {
	/**
	 * 遍历处理
	 * @param node
	* @version 1.0
	 */
	void visit(ExtTreeNode node);
}
