package org.infrastructure.jpa.dto;

/**
 * 节点转换
 *
 */
public interface NodeMapper<T> {
    public ExtTreeNode convertToNode(T nodeData) throws Exception;
}
