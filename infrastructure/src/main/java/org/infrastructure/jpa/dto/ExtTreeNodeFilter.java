package org.infrastructure.jpa.dto;

public abstract class ExtTreeNodeFilter {
	public ExtTreeNode result;
	public abstract boolean find(ExtTreeNode n);
	

    /**
     * 从根节点开始，遍历整个树
     * 
     * @param filter 条件
    * @version 1.0
     */
    public static void find(ExtTreeNode node,ExtTreeNodeFilter filter){
    	//先序遍历
    	if(filter.find(node)){
    		filter.result=node;
    		return;
    	}
    	
    	if(node.getChildren() !=null){
	    	for(ExtTreeNode c : node.getChildren()){
	    		find(c,filter);
	    	}
    	}
    }
}
