package org.infrastructure.web.view;

import java.util.ArrayList;
import java.util.List;


/**
 * 映射到ExtjsGrid的 Excel导出格式
 */
public class ExcelExtGrid {

	public String fileId;

	public String fileName;

	/**
	 * 增加 Grid column
	 * 
	 * @param header
	 * @param width
	 * @param dataIndex
	 */
	public void addGridColumn(String header, Integer width, String dataIndex, String xtype) {
		GridColumn col = new GridColumn();
		col.header = header;
		col.width = width;
		col.dataIndex = dataIndex;
		col.xtype = xtype;
		this.columns.add(col);
	}

	/**
	 * 表列布局
	 */
	public List<GridColumn> columns = new ArrayList<GridColumn>();
	
	/**
	 * extjs grid的属性映射
	 */
	public static class GridColumn {
		/**
		 * 列头
		 */
		public String header;

		/**
		 * 列宽度
		 */
		public Integer width;

		/**
		 * 访问值
		 */
		public String dataIndex;

		/**
		 * 数据类型
		 */
		public String xtype;
	}
}


