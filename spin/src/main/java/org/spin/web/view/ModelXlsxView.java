package org.spin.web.view;


import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.DateFormatConverter;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.spin.throwable.SimplifiedException;
import org.spin.util.EntityUtils;
import org.springframework.web.servlet.view.document.AbstractXlsxView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


/**
 * Created by Arvin on 2017/2/7.
 */
public class ModelXlsxView extends AbstractXlsxView {

    /**
     * 一个像素转换为多少宽度
     */
    static final float PIX_TO_WIDTH = 100 * 50 / 132;

    static final String DATE_COLUMN_XTYPE = "datecolumn";
    static final String LINK_COLUMN_XTYPE = "linkcolumn";
    static final String NUMBER_COLUMN_XTYPE = "numbercolumn ";
    static final String BOOLEAN_COLUMN_XTYPE = "booleancolumn";
    static final String COLUMN_XTYPE = "gridcolumn";

    /**
     * 返回默认文件名
     */
    private static String getDefaultExportFileName() {
        return "export.xls";
    }

    /**
     * 构造函数需要的配置
     */
    ExcelGrid grid = null;

    /**
     * 填充数据所需要列表数据
     */
    List data = null;

    /**
     * 通过ExtjsGrid的Column配置导出
     *
     * @param grid grid配置（列）
     * @param data 查询数据
     */
    public ModelXlsxView(ExcelGrid grid, List data) {
        this.grid = grid;
        this.data = data;
    }

    @Override
    protected void buildExcelDocument(Map<String, Object> model, Workbook workbook, HttpServletRequest request, HttpServletResponse response) throws Exception {
        workbook = null == workbook ? this.createWorkbook(model, request) : workbook;
        this.generateExcel(workbook, grid, data);
        String fileName = StringUtils.isNotEmpty(grid.fileName) ? grid.fileName : getDefaultExportFileName();
        fileName = encodeFilename(fileName.endsWith(".xls") ? fileName : fileName + ".xls");
        response.setContentType(this.getContentType());
        response.setHeader("Content-disposition", "attachment;filename=" + fileName);

        OutputStream ouputStream = response.getOutputStream();
        workbook.write(ouputStream);
        ouputStream.flush();
        ouputStream.close();
    }

    /**
     * 设置下载文件中文件的名称
     */
    public static String encodeFilename(String filename) {
        /*
         * 获取客户端浏览器和操作系统信息
         * 在IE浏览器中得到的是：User-Agent=Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; Maxthon; Alexa Toolbar)
         * 在Firefox中得到的是：User-Agent=Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.7.10) Gecko/20050717 Firefox/1.0.6
         */
        try {
            return URLEncoder.encode(filename, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return filename;
    }

    /*
     * grid的xtype对应到单元格赋值
     */
    Map<String, String> gridColumnXtypeFormat = null;

    public Map<String, String> getGridColumnXtypeFormat() {
        if (gridColumnXtypeFormat == null)
            gridColumnXtypeFormat = new HashMap<>();
        String dateFormat = DateFormatConverter.convert(Locale.SIMPLIFIED_CHINESE, "yyyy-MM-dd HH:mm:ss");
        gridColumnXtypeFormat.put(DATE_COLUMN_XTYPE, dateFormat);// "yyyy-MM-dd HH:mm:ss");
        return gridColumnXtypeFormat;
    }

    private int toColumnWidth(int pixWidth) {
        Float colWdt = pixWidth * PIX_TO_WIDTH;
        return colWdt.intValue();
    }


    /**
     * /**
     * 通过参数和数据生成Excel文件
     */
    @SuppressWarnings("unchecked")
    protected Workbook generateExcel(Workbook workbook, ExcelGrid grid, List data) throws Exception {
        Sheet sheet = workbook.createSheet();// 创建一个Excel的Sheet
        sheet.createFreezePane(1, 1);// 冻结header

        // 列头的样式
        CellStyle columnHeadStyle = getColumnHeaderStyle(workbook);

        try {
            // 创建第一行
            Row row0 = sheet.createRow(0);
            // 设置行高
            row0.setHeight((short) 500); // 50pix高度

            // 初始化列头和数据列单元格样式
            Map<String, CellStyle> columnStyleMap = new HashMap<>();

            for (int i = 0; i < grid.columns.size(); i++) {
                ExcelGrid.GridColumn col = grid.columns.get(i);
                if (col.width != null) {
                    sheet.setColumnWidth(i, toColumnWidth(col.width));// 70pix宽度
                } else
                    sheet.setColumnWidth(i, toColumnWidth(100));

                Cell cell = row0.createCell(i);
                cell.setCellStyle(columnHeadStyle);
                cell.setCellValue(new XSSFRichTextString(col.header));

                columnStyleMap.put(col.dataIndex, getDataCellStyle(workbook, col.xtype));
            }

            // 填充数据内容
            for (int i = 0; i < data.size(); i++) {
                Object robj = data.get(i);
                Row row = sheet.createRow(i + 1);// 除去头部
                Cell cell;
                // 当行赋值
                for (int c = 0; c < grid.columns.size(); c++) {
                    ExcelGrid.GridColumn col = grid.columns.get(c);
                    cell = row.createCell(c);

                    setDataCellValue(robj, cell, col, workbook);

                    cell.setCellStyle(columnStyleMap.get(col.dataIndex));
                }
            }

            return workbook;

        } catch (Exception e) {
            throw new SimplifiedException("导出Excel文件[" + grid.fileName + "]出错", e);
        }
    }

    /**
     * 设置列值 （日期类型赋值date，默认类型String
     *
     * @param rdata 汗数据
     * @param cell  单元格
     * @param col   列
     */
    private void setDataCellValue(Object rdata, Cell cell, ExcelGrid.GridColumn col, Workbook workbook) {
        Object o;

        if (rdata instanceof Map) {
            o = ((Map) rdata).get(col.dataIndex);
        } else {
            o = EntityUtils.getFieldValue(rdata, col.dataIndex);
        }

        if (o == null)
            return;

        if (DATE_COLUMN_XTYPE.equals(col.xtype)) {
            if (o instanceof Date) {
                cell.setCellValue((Date) o);
            }
        } else {
            String value = o.toString();
            cell.setCellValue(value);
        }
    }

    /**
     * 获取每个数据内容单元格的样式
     */
    private CellStyle getDataCellStyle(Workbook workbook, String gridColumnXtype) {
        Font font = workbook.createFont();
        CreationHelper createHelper = workbook.getCreationHelper();

        font.setFontName("宋体");
        font.setFontHeightInPoints((short) 10);
        // 普通单元格样式
        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.LEFT);// 左右居中
        style.setVerticalAlignment(VerticalAlignment.TOP);// 上下居中
        style.setWrapText(true);
        style.setLeftBorderColor(HSSFColor.BLACK.index);
        style.setBorderLeft(BorderStyle.THIN);
        style.setRightBorderColor(HSSFColor.BLACK.index);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN); // 设置单元格的边框为粗体
        style.setBottomBorderColor(HSSFColor.BLACK.index); // 设置单元格的边框颜色．
        style.setFillForegroundColor(HSSFColor.WHITE.index);// 设置单元格的背景颜色．

        String format = getGridColumnXtypeFormat().get(gridColumnXtype);
        if (StringUtils.isNotEmpty(format)) {// "m/d/yy h:mm"
            style.setDataFormat(createHelper.createDataFormat().getFormat(format));
        }

        return style;
    }

    /**
     * 生成列头样式
     */
    private CellStyle getColumnHeaderStyle(Workbook workbook) {
        Font columnHeadFont = workbook.createFont();
        columnHeadFont.setFontName("宋体");
        columnHeadFont.setFontHeightInPoints((short) 10);
        columnHeadFont.setBold(true);

        CellStyle columnHeadStyle = workbook.createCellStyle();
        columnHeadStyle.setFont(columnHeadFont);
        columnHeadStyle.setAlignment(HorizontalAlignment.CENTER);// 左右居中
        columnHeadStyle.setVerticalAlignment(VerticalAlignment.CENTER);// 上下居中
        columnHeadStyle.setLocked(true);
        columnHeadStyle.setWrapText(true);
        columnHeadStyle.setLeftBorderColor(HSSFColor.BLACK.index);// 左边框的颜色
        columnHeadStyle.setBorderLeft(BorderStyle.THIN);// 边框的大小
        columnHeadStyle.setRightBorderColor(HSSFColor.BLACK.index);// 右边框的颜色
        columnHeadStyle.setBorderRight(BorderStyle.THIN);// 边框的大小
        columnHeadStyle.setBorderBottom(BorderStyle.THIN); // 设置单元格的边框为粗体
        columnHeadStyle.setBottomBorderColor(HSSFColor.BLACK.index); // 设置单元格的边框颜色
        columnHeadStyle.setFillForegroundColor(HSSFColor.WHITE.index); // 设置单元格的背景颜色（单元格的样式会覆盖列或行的样式）
        return columnHeadStyle;
    }
}