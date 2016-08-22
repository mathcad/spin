package org.infrastructure.jpa.core;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlType;

/**
 * 分页数据存放（列表使用)
 * @param <T>
 */
@XmlType(name = "Page")
public class Page<T> implements Serializable {
	private static final long serialVersionUID = -1433098389717460681L;
	public List<T> data;
	public Long total;

	public Page() {
	}

	public Page(List<T> data, Long total) {
		this.data = data;
		this.total = total;
	}
}