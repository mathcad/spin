package org.spin.web.view;

import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.DateFormatConverter;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.spin.sys.Assert;
import org.spin.sys.FileType;
import org.spin.throwable.SimplifiedException;
import org.spin.util.EntityUtils;
import org.spin.util.StringUtils;
import org.springframework.web.servlet.view.AbstractView;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by xuweinan on 2017/2/7.
 *
 * @author xuweinan
 */
public class ModelExcelView extends AbstractView {

    private FileType.Excel fileType;

    /**
     * 一个像素转换为多少宽度
     */
    private static final int PIX_TO_WIDTH = 100 * 50 / 132;

    private static final String DATE_COLUMN_TYPE = "date";
    private static final String NUMBER_COLUMN_TYPE = "number";
    private static final String BOOLEAN_COLUMN_TYPE = "boolean";

    private static final String defaultFileName = "export";

    private ExcelGrid grid = null;
    private List<?> data = null;
    private Map<String, String> dataTypeFormat = null;

    public ModelExcelView(FileType.Excel fileType, ExcelGrid grid, List<?> data) {
        Assert.notNull(fileType, "Excel文件类型不能为空");
        this.fileType = fileType;
        setContentType(this.fileType.getContentType());
        this.grid = grid;
        this.data = data;
    }

    /**
     * Default Constructor.
     * Sets the FileType to XLSX.
     */
    public ModelExcelView(ExcelGrid grid, List<?> data) {
        this(FileType.Excel.XLSX, grid, data);
    }


    @Override
    protected boolean generatesDownloadContent() {
        return true;
    }

    /**
     * Renders the Excel view, given the specified model.
     */
    @Override
    protected final void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception {

        // Create a fresh workbook instance for this render step.
        Workbook workbook = createWorkbook();

        // Delegate to application-provided document code.
        buildExcelDocument(model, workbook, request, response);

        // Set the content type.
        response.setContentType(getContentType());

        // Flush byte array to servlet output stream.
        renderWorkbook(workbook, response);
    }

    protected Workbook createWorkbook() {
        switch (fileType.getExtension()) {
            case ".xls":
                return new HSSFWorkbook();
            case ".xlsx":
                return new XSSFWorkbook();
            default:
                throw new SimplifiedException("");
        }
    }

    /**
     * The actual render step: taking the POI {@link Workbook} and rendering
     * it to the given response.
     *
     * @param workbook the POI Workbook to render
     * @param response current HTTP response
     * @throws IOException when thrown by I/O methods that we're delegating to
     */
    private void renderWorkbook(Workbook workbook, HttpServletResponse response) throws IOException {
        ServletOutputStream out = response.getOutputStream();
        workbook.write(out);
        out.flush();
        out.close();
        workbook.close();
    }

    /**
     * Application-provided subclasses must implement this method to populate
     * the Excel workbook document, given the model.
     *
     * @param model    the model Map
     * @param workbook the Excel workbook to populate
     * @param request  in case we need locale etc. Shouldn't look at attributes.
     * @param response in case we need to set cookies. Shouldn't write to it.
     */
    private void buildExcelDocument(Map<String, Object> model, Workbook workbook, HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (null == workbook)
            return;
        this.generateExcel(workbook, grid, data);
        String fileName = StringUtils.isNotEmpty(grid.getFileName()) ? grid.getFileName() : defaultFileName;
        fileName = StringUtils.urlEncode(fileName.endsWith(fileType.getExtension()) ? fileName : fileName + fileType.getExtension());
        response.setHeader("Content-disposition", "attachment;filename=" + fileName);
    }

    /**
     * /**
     * 通过参数和数据生成Excel文件
     */
    private Workbook generateExcel(Workbook workbook, ExcelGrid grid, List<?> data) throws Exception {
        Sheet sheet = workbook.createSheet();
        sheet.createFreezePane(1, 1);

        // 列头的样式
        CellStyle columnHeadStyle = getHeaderCellStyle(workbook);

        try {
            // 创建第一行
            Row row0 = sheet.createRow(0);
            // 设置行高
            row0.setHeight((short) 500); // 50pix高度

            // 初始化列头和数据列单元格样式
            Map<String, CellStyle> columnStyleMap = new HashMap<>();

            for (int i = 0; i < grid.getColumns().size(); i++) {
                GridColumn col = grid.getColumns().get(i);
                if (col.getWidth() != null) {
                    sheet.setColumnWidth(i, col.getWidth() * PIX_TO_WIDTH);// 70pix宽度
                } else
                    sheet.setColumnWidth(i, 100 * PIX_TO_WIDTH);

                Cell cell = row0.createCell(i);
                cell.setCellStyle(columnHeadStyle);
                if (FileType.Excel.XLSX.equals(fileType))
                    cell.setCellValue(new XSSFRichTextString(col.getHeader()));
                else if (FileType.Excel.XLSX.equals(fileType))
                    cell.setCellValue(new HSSFRichTextString(col.getHeader()));

                columnStyleMap.put(col.getDataIndex(), getDataCellStyle(workbook, col.getDataType()));
            }

            // 填充数据内容
            for (int i = 0; i < data.size(); i++) {
                Object robj = data.get(i);
                Row row = sheet.createRow(i + 1);// 除去头部
                Cell cell;
                // 当行赋值
                for (int c = 0; c < grid.getColumns().size(); c++) {
                    GridColumn col = grid.getColumns().get(c);
                    cell = row.createCell(c);

                    setDataCellValue(robj, cell, col);

                    cell.setCellStyle(columnStyleMap.get(col.getDataIndex()));
                }
            }

            return workbook;

        } catch (Exception e) {
            throw new SimplifiedException("导出Excel文件[" + grid.getFileName() + "]出错", e);
        }
    }

    /**
     * 生成列头样式
     */
    private CellStyle getHeaderCellStyle(Workbook workbook) {
        Font columnHeadFont = workbook.createFont();
        columnHeadFont.setFontName("宋体");
        columnHeadFont.setFontHeightInPoints((short) 10);
        columnHeadFont.setBold(true);

        CellStyle columnHeadStyle = workbook.createCellStyle();
        columnHeadStyle.setFont(columnHeadFont);
        columnHeadStyle.setAlignment(HorizontalAlignment.CENTER);
        columnHeadStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        columnHeadStyle.setLocked(true);
        columnHeadStyle.setWrapText(true);
        columnHeadStyle.setBorderTop(BorderStyle.THIN);
        columnHeadStyle.setBorderRight(BorderStyle.THIN);
        columnHeadStyle.setBorderBottom(BorderStyle.THIN);
        columnHeadStyle.setBorderLeft(BorderStyle.THIN);

        columnHeadStyle.setTopBorderColor((short) 8);
        columnHeadStyle.setRightBorderColor((short) 8);
        columnHeadStyle.setBottomBorderColor((short) 8);
        columnHeadStyle.setLeftBorderColor((short) 8);

        columnHeadStyle.setFillForegroundColor((short) 9);
        return columnHeadStyle;
    }

    /**
     * 获取每个数据内容单元格的样式
     */
    private CellStyle getDataCellStyle(Workbook workbook, String dataType) {
        Font font = workbook.createFont();
        CreationHelper createHelper = workbook.getCreationHelper();
        font.setFontName("宋体");
        font.setFontHeightInPoints((short) 10);

        // 普通单元格样式
        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.TOP);
        style.setWrapText(true);

        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);

        style.setTopBorderColor((short) 8);
        style.setRightBorderColor((short) 8);
        style.setBottomBorderColor((short) 8);
        style.setLeftBorderColor((short) 8);

        style.setFillForegroundColor((short) 9);

        String format = getColumnDataTypeFormat().get(dataType);
        if (StringUtils.isNotEmpty(format)) {
            style.setDataFormat(createHelper.createDataFormat().getFormat(format));
        }

        return style;
    }

    private void setDataCellValue(Object rdata, Cell cell, GridColumn col) {
        Object o;
        if (rdata instanceof Map)
            o = ((Map) rdata).get(col.getDataIndex());
        else
            o = EntityUtils.getFieldValue(rdata, col.getDataIndex());
        if (o == null)
            return;

        switch (col.getDataType()) {
            case DATE_COLUMN_TYPE:
                if (o instanceof Date)
                    cell.setCellValue((Date) o);
                break;
            default:
                cell.setCellValue(o.toString());
                break;
        }
    }

    private Map<String, String> getColumnDataTypeFormat() {
        if (dataTypeFormat == null)
            dataTypeFormat = new HashMap<>();
        String dateFormat = DateFormatConverter.convert(Locale.SIMPLIFIED_CHINESE, "yyyy-MM-dd HH:mm:ss");
        dataTypeFormat.put(DATE_COLUMN_TYPE, dateFormat);
        return dataTypeFormat;
    }

    public FileType.Excel getFileType() {
        return fileType;
    }
}