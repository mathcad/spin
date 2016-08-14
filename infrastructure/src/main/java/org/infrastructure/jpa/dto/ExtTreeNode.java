package org.infrastructure.jpa.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.infrastructure.util.ObjectUtils;

/**
 * Extjs树节点
 */
public class ExtTreeNode extends LinkedHashMap<String, Object> implements Serializable{
	private static final long serialVersionUID = 7207994684117757183L;
	
	public static final String ID_KEY = "id";
    public static final String TEXT_KEY = "text";
    public static final String PARENT_ID_KEY = "parentId";
    public static final String ICON_CLS_KEY = "iconCls";
    public static final String LEAF_KEY = "leaf";
    public static final String EXPENDED_KEY = "expanded";
    public static final String CHILDREN_KEY = "children";
    public static final String MTYPE_KEY = "mxtype";
    
    public ExtTreeNode(){
        super();
    }
    
    public ExtTreeNode(String id,String parentId, String text, String iconCls,boolean leaf){
    	this(id,parentId,text,iconCls,leaf,"");
    }

    /**
     * 构造函数
     *
     */
    public ExtTreeNode(String id,String parentId, String text, String iconCls,boolean leaf,String mxtype) {
        super();
        this.put(ID_KEY, id);
        this.put(PARENT_ID_KEY, parentId);
        this.put(TEXT_KEY, text);
        this.put(ICON_CLS_KEY, iconCls);
        this.put(LEAF_KEY, leaf);
        this.put(MTYPE_KEY, mxtype);
        if(!leaf){
            this.setChildren(new ArrayList<ExtTreeNode>());
        }
    }

    /**
     * 构造函数
     *
     */
	public ExtTreeNode(String id, String parentId, String text, String iconCls, boolean leaf, HashMap<String, String> map) {
		super();
		this.put(ID_KEY, id);
		this.put(PARENT_ID_KEY, parentId);
		this.put(TEXT_KEY, text);
		this.put(ICON_CLS_KEY, iconCls);
		this.put(LEAF_KEY, leaf);
		Set<String> set = map.keySet();
		for (String s : set) {
			this.put(s, map.get(s));
		}
	}

    /**
     * 得到Id
     *
     * @return
     */
	public String getId() {
		return (String) this.get(ID_KEY);
	}

    /**
     * 得到ParentId
     *
     * @return
     */
	public String getParentId() {
		return (String) this.get(PARENT_ID_KEY);
	}

	public Integer getSort() {
		return (Integer) this.get("sort");
	}

    /**
     * 子节点
     *
     * @param children
     */
    public void setChildren(List<ExtTreeNode> children) {
        this.put(CHILDREN_KEY, children);
    }
    
    /**
     * 子节点
     *
     * @param children
     */
	@SuppressWarnings("unchecked")
	public List<ExtTreeNode> getChildren() {
		return (List<ExtTreeNode>) this.get(CHILDREN_KEY);
	}

    /**
     * 添加子节点
     *
     * @param child
     * @param autoExpanded
     */
	@SuppressWarnings("unchecked")
	public void addChild(ExtTreeNode child, boolean autoExpanded) {
		if (this.getChildren() == null) {
			this.setChildren(new ArrayList<ExtTreeNode>());
		}
		ExtTree.insertBySort(ObjectUtils.convert(List.class, this.get(CHILDREN_KEY)), child);
		this.put(LEAF_KEY, false);

		if (autoExpanded && this.get(EXPENDED_KEY) == null)
			this.put(EXPENDED_KEY, true);
	}

	public void setSort(Integer sort) {
		this.put("sort", sort);
	}

    /**
     * 从根节点开始，遍历整个树
     * 
     * @param filter 条件
    * @version 1.0
     */
	public void find(ExtTreeNode node, ExtTreeNodeFilter filter) {
		// 先序遍历
		if (filter.find(node)) {
			filter.result = node;
			return;
		}

		if (node.getChildren() != null) {
			for (ExtTreeNode c : node.getChildren()) {
				find(c, filter);
			}
		}
	}
}