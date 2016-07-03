package org.infrastructure.jpa.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.infrastructure.sys.MatcherUtils;
import org.infrastructure.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extjs的Tree结构数据
 */
public class ExtTree implements Serializable {
	private static final long serialVersionUID = 1295533782528279520L;
	private transient static final Logger logger = LoggerFactory.getLogger(ExtTree.class);

	public static void insertBySort(List<ExtTreeNode> children, ExtTreeNode newChild) {
		int idx = 0;
		for (ExtTreeNode c : children) {
			if (newChild.getSort() != null && newChild.getSort() < c.getSort())
				break;
			idx++;
		}
		children.add(idx, newChild);
	}
	
	public boolean autoExpanded = true;

	/**
	 * id -> Node 用于遍历树
	 */
	private LinkedHashMap<String, ExtTreeNode> nodesMap = new LinkedHashMap<String, ExtTreeNode>();

	/**
	 * 根节点-ToJson使用
	 */
	private List<ExtTreeNode> roots = new ArrayList<ExtTreeNode>();

	/**
	 * 返回跟节点
	 * 
	 * @return
	 */
	public List<ExtTreeNode> getRoots() {
		return roots;
	}

	/**
	 * 返回节点
	 * 
	 * @return
	 */
	public ExtTreeNode getNode(String id) {
		return nodesMap.get(id);
	}

	/**
	 * 添加节点数据到树，前提条件，父节点，一定先出现
	 *
	 * @param nodelist
	 * @param nodeMapper
	 * @throws Exception
	 */

	public <T> void addEntities(List<T> nodelist, NodeMapper<T> nodeMapper) throws Exception {
		// addToMap
		List<ExtTreeNode> newNodeList = new ArrayList<ExtTreeNode>();
		for (T nodeMap : nodelist) {
			ExtTreeNode node = nodeMapper.convertToNode(nodeMap);
			if (node != null) {
				nodesMap.put(node.getId(), node);
				newNodeList.add(node);
			}
		}

		// find roots
		for (ExtTreeNode node : newNodeList) {
			// 父节点存入
			if (nodesMap.containsKey(node.getParentId()) == false) {
				try {
					insertBySort(roots, node);
				} catch (Exception ex) {
					logger.error("");
					ex.printStackTrace();
				}
			} else {
				nodesMap.get(node.getParentId()).addChild(node, this.autoExpanded);
			}
		}
	}

	/**
	 * 遍历树，查找节点
	 * 
	 * @param prop
	 * @param value
	 * @return
	 */
	public ExtTreeNode findFrom(ExtTreeNode rootNode, String prop, String value) {
		result = null;
		if (result == null)
			find(rootNode, prop, value);
		return result;
	}

	/**
	 * 遍历树，查找节点
	 * 
	 * @param prop
	 * @param value
	 * @return
	 */
	public ExtTreeNode find(String prop, String value) {
		result = null;
		for (ExtTreeNode node : this.getRoots()) {
			if (result == null)
				find(node, prop, value);
		}
		return result;
	}

	/**
	 * 遍历树，查找节点
	 *
	 * @param prop 属性
	 * @param pattern 正则表达式
	 * @return
	 */
	public ExtTreeNode findWithPattern(String prop,String pattern){
		result =null;
		for(ExtTreeNode node : this.getRoots()){
			if(result == null)
				findWithPatternStr(node,prop,pattern);
		}
		return result;
	}

	void findWithPatternStr(ExtTreeNode root,String prop,String pattern){

		Object propNode = root.get(prop);

		if(propNode !=null && StringUtils.isNotEmpty(pattern) && MatcherUtils.isMatch(pattern, root.get(prop).toString())){
			result = root;
			return;
		}

		if(root.getChildren() !=null){
			for(ExtTreeNode node : root.getChildren()){
				findWithPatternStr(node,prop,pattern);
			}
		}
	}

	ExtTreeNode result = null;

	void find(ExtTreeNode root, String prop, String value) {

		if (value.equals(root.get(prop))) {
			result = root;
			return;
		}

		if (root.getChildren() != null) {
			for (ExtTreeNode node : root.getChildren()) {
				find(node, prop, value);
			}
		}
	}

	/**
	 * 从根节点开始，遍历整个树
	 * 
	 * @param node null代表从跟开始遍历
	 * @param visior 遍历处理
	 * @version 1.0
	 */
	public void visit(ExtTreeNode node, ExtTreeNodeVisit visitor) {
		if (node == null) {
			for (ExtTreeNode c : this.getRoots()) {
				visit(c, visitor);
			}
			return;
		}
		// 先序遍历
		visitor.visit(node);
		if (node.getChildren() != null) {
			for (ExtTreeNode c : node.getChildren()) {
				visit(c, visitor);
			}
		}

	}
}
