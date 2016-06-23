package org.infrastructure.jpa.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlType;

/**
 * 主从明细数据
 * 
 * @author zhouxiang2
 *
 * @param <PK>
 * @param <T>
 */
@SuppressWarnings("serial")
@XmlType(name = "GridData")
public class GridData<PK, T> implements Serializable{

	/**
	 * 有更新的数据
	 */
	public List<T> modified = new ArrayList<T>();

	/**
	 * 删除的数据
	 */
	public List<PK> removed = new ArrayList<PK>();

//	/**
//	 * 删除的数据
//	 */
//	public List<T> deleted = new ArrayList<T>();

}