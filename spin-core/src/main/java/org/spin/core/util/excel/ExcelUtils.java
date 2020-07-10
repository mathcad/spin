package org.spin.core.util.excel;

import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.DateFormatConverter;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.spin.core.ErrorCode;
import org.spin.core.function.FinalConsumer;
import org.spin.core.function.serializable.ExceptionalSupplier;
import org.spin.core.io.BytesCombinedInputStream;
import org.spin.core.throwable.SpinException;
import org.spin.core.util.BeanUtils;
import org.spin.core.util.CollectionUtils;
import org.spin.core.util.DateUtils;
import org.spin.core.util.MapUtils;
import org.spin.core.util.StringUtils;
import org.spin.core.util.Util;
import org.spin.core.util.file.FileType;
import org.spin.core.util.file.FileTypeUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Excel工具类(基于SaxReader与事件驱动，支持大文件)
 *
 * @author wangy QQ 837195190
 * <p>Created by thinkpad on 2018/5/4.</p>
 */
public final class ExcelUtils extends Util {

    /*******************************************************************************
     EXCEL的行高度和列宽度单位是不一样的

     1. EXCEL列高度的单位是磅, Apache POI的行高度单位是缇(twip)：
     1英寸 = 72磅 = 25.4毫米 = 1440缇
     1磅 = 0.353毫米 = 20缇

     POI中的行高 ＝ Excel的行高度*20
     Excel的行高度 = POI中的行高/20

     2. EXCEL列宽度的单位是字符个数
     在excel2003以上的版本中，可以建立一个空白的xls文档并将第一列宽度拉到10。然后在A1单元格中输入1234567890可以看到单元格正好可以容纳这十个字符。
     一个字符的宽度是通过测量1234567890这十个字符在默认字体(通常是宋体11号字，视版本可能不同)下的平均宽度得到的。
     只要记住一个字符的宽度是8像素 (一个字符的宽度取决于Excel文件中的第一个字体大小，并不一定就是8像素)就可以了。
     一个单元格实际的像素宽度还要在 （字符个数 * 默认字符的宽度）的基础上前后各加2个像素的空白边。另外字符之间会叠加一个像素，在计算时也要减去（一个字符的边距不一定只是2像素，和字体有关系）

     像素 = 2个像素空白 + (字符个数 * 默认字符的宽度) + 2个像素空白 - (字符个数 - 1)
     整理一下，公式变成：
     像素 = 5 + (字符个数 * (默认字符的宽度 - 1))
     代入默认字符宽度8：
     像素 = 5 + (字符个数 * 7)
     POI中的字符宽度算法是：
     double 宽度 = (字符个数 * (字符宽度 - 1) + 5) / (字符宽度 - 1) * 256;
     然后再四舍五入成整数。
     *********************************************************************************/

    public static final String DATE_COLUMN_TYPE = "date";
    public static final String NUMBER_COLUMN_TYPE = "number";
    public static final String BOOLEAN_COLUMN_TYPE = "boolean";

    private static final int TRAIT_LEN = 16;

    /**
     * 像素转换为多少宽度
     */
    private static final int PIX_TO_WIDTH = 100 * 50 / 132;

    /**
     * 缇/磅
     */
    private static final int POUNDS_PER_TWIP = 20;

    private static final int WID_PER_CHAR = 256;

    private static final Map<String, String> DEFAULT_DATA_TYPE_FORMAT = new HashMap<>();

    private static final ThreadLocal<Map<String, String>> DATA_TYPE_FORMAT = new ThreadLocal<>();

    static {
        String dateFormat = DateFormatConverter.convert(Locale.SIMPLIFIED_CHINESE, "yyyy-MM-dd HH:mm:ss");
        DEFAULT_DATA_TYPE_FORMAT.put(DATE_COLUMN_TYPE, dateFormat);
    }

    private ExcelUtils() {
    }

    public static void readWorkBook(InputStream is, FinalConsumer<ExcelRow> rowReader) {
        byte[] trait = new byte[TRAIT_LEN];
        int read;
        FileType fileType;
        BytesCombinedInputStream bcis;
        try {
            bcis = new BytesCombinedInputStream(is, TRAIT_LEN);
            read = bcis.readCombinedBytes(trait);
            if (read < TRAIT_LEN) {
                throw new SpinException(ErrorCode.IO_FAIL, "输入流中不包含有效内容");
            }
            fileType = FileTypeUtils.detectFileType(trait);
            ExcelReader reader;
            if (Objects.isNull(fileType)) {
                throw new SpinException(ErrorCode.IO_FAIL, "不支持的文件类型");
            } else if (fileType.equals(FileType.Document.XLS)) {
                reader = new ExcelXlsReader();
            } else if (fileType.equals(FileType.Document.XLSX)) {
                reader = new ExcelXlsxReader();
            } else {
                throw new SpinException(ErrorCode.IO_FAIL, "不支持的文件类型");
            }
            reader.process(bcis, rowReader);
        } catch (IOException e) {
            throw new SpinException(ErrorCode.IO_FAIL, "输入流读取失败", e);
        }
    }

    public static void readWorkBook(String fileName, FinalConsumer<ExcelRow> rowReader) {
        readWorkBook(new File(fileName), rowReader);
    }

    public static void readWorkBook(File file, FinalConsumer<ExcelRow> rowReader) {
        try (InputStream is = new FileInputStream(file)) {
            readWorkBook(is, rowReader);
        } catch (FileNotFoundException e) {
            throw new SpinException(ErrorCode.IO_FAIL, "读取的文件不存在", e);
        } catch (IOException e) {
            throw new SpinException(ErrorCode.IO_FAIL, "读取文件失败", e);
        }
    }

    /**
     * 根据指定的文件类型与数据，生成Excel并写出到输出流，写出完成后，并不会关闭流
     *
     * @param fileType     文件类型
     * @param excelModel   ExceModel
     * @param outputStream 输出流
     */
    public static void generateWorkBook(FileType fileType, ExcelModel excelModel, OutputStream outputStream) {
        try (Workbook workbook = generateWorkbook(fileType, excelModel)) {
            workbook.write(outputStream);
            outputStream.flush();
        } catch (IOException e) {
            throw new SpinException(ErrorCode.IO_FAIL, "Excel写出workbook失败", e);
        }
    }

    /**
     * 根据指定的文件类型与数据，生成Excel并写出到指定提供者提供的输出流输出，输出完成后，将关闭该流
     *
     * @param fileType             文件类型
     * @param excelModel           ExceModel
     * @param outputStreamSupplier 输出流提供者
     */
    public static void generateWorkBook(FileType fileType, ExcelModel excelModel, ExceptionalSupplier<OutputStream, IOException> outputStreamSupplier) {
        try (Workbook workbook = generateWorkbook(fileType, excelModel); OutputStream outputStream = outputStreamSupplier.get()) {
            workbook.write(outputStream);
            outputStream.flush();
        } catch (IOException e) {
            throw new SpinException(ErrorCode.IO_FAIL, "Excel写出workbook失败", e);
        }
    }


    /**
     * 根据指定的文件类型与数据，生成Excel
     *
     * @param fileType   文件类型
     * @param excelModel ExceModel
     * @return workbook
     */
    public static Workbook generateWorkbook(FileType fileType, ExcelModel excelModel) {
        ExcelGrid grid = excelModel.getGrid();
        Map<String, Iterable<?>> data = excelModel.getData();
        Workbook workbook = createWorkbook(fileType);

        for (ExcelSheet excelSheet : grid.getSheets()) {
            Sheet sheet = workbook.createSheet(excelSheet.getSheetName());
            sheet.createFreezePane(1, 1);

            // 列头的样式
            CellStyle columnHeadStyle = getHeaderCellStyle(workbook);

            try {
                // 创建第一行
                Row row0 = sheet.createRow(0);
                // 设置行高
                row0.setHeight((short) 285); // 14.25磅

                // 初始化列头和数据列单元格样式
                Map<String, CellStyle> columnStyleMap = new HashMap<>();

                for (int i = 0; i < excelSheet.getColumns().size(); i++) {
                    GridColumn col = excelSheet.getColumns().get(i);
                    if (excelSheet.getExcludeColumns().contains(col.getHeader())) {
                        continue;
                    }
                    if (col.getWidth() != null) {
                        sheet.setColumnWidth(i, (col.getWidth() * PIX_TO_WIDTH));
                    } else {
                        sheet.setColumnWidth(i, 100 * PIX_TO_WIDTH);
                    }

                    Cell cell = row0.createCell(i);
                    cell.setCellStyle(columnHeadStyle);
                    if (workbook instanceof HSSFWorkbook) {
                        cell.setCellValue(new HSSFRichTextString(col.getHeader()));
                    } else if (workbook instanceof XSSFWorkbook) {
                        cell.setCellValue(new XSSFRichTextString(col.getHeader()));
                    }

                    columnStyleMap.put(col.getDataIndex(), getDataCellStyle(workbook, col.getDataType()));
                }

                // 填充数据内容
                Iterable<?> sheetData = data.get(sheet.getSheetName());
                if (!CollectionUtils.isEmpty(sheetData)) {
                    int i = 1;
                    for (Object robj : sheetData) {
                        Row row = sheet.createRow(i);// 除去头部
                        row0.setHeight((short) 285); // 14.25磅
                        Cell cell;
                        // 当行赋值
                        for (int c = 0; c < excelSheet.getColumns().size(); c++) {
                            GridColumn col = excelSheet.getColumns().get(c);
                            if (excelSheet.getExcludeColumns().contains(col.getHeader())) {
                                continue;
                            }
                            cell = row.createCell(c);

                            setDataCellValue(robj, cell, col);
                            if (col.getWidth() == null) {
                                int columnWidth = 10;

                                int length = cell.getStringCellValue().getBytes().length;
                                if (columnWidth < length) {
                                    columnWidth = length;
                                }
                                int columnWidthFinal = columnWidth + 2;
                                if (columnWidthFinal > 255) {
                                    columnWidthFinal = 255;
                                }
                                sheet.setColumnWidth(c, columnWidthFinal * WID_PER_CHAR);
                            }
                            cell.setCellStyle(columnStyleMap.get(col.getDataIndex()));
                        }
                        ++i;
                    }
                }

            } catch (Exception e) {
                throw new SpinException("生成Excel文件[" + grid.getFileName() + "]出错", e);
            }
        }

        return workbook;
    }

    /**
     * 设置数据类型格式
     *
     * @param dataType 数据类型
     * @param format   格式
     */
    public static void setDataTypeFormat(String dataType, String format) {
        if (null == DATA_TYPE_FORMAT.get()) {
            DATA_TYPE_FORMAT.set(MapUtils.ofMap(dataType, format));
        } else {
            DATA_TYPE_FORMAT.get().put(dataType, format);
        }
    }

    private static Workbook createWorkbook(FileType fileType) {
        switch (fileType.getFormat()) {
            case "XLS":
                return new HSSFWorkbook();
            case "XLSX":
                return new XSSFWorkbook();
            default:
                throw new SpinException("不支持的文件类型: " + fileType.getFormat());
        }
    }

    /**
     * 生成列头样式
     */
    private static CellStyle getHeaderCellStyle(Workbook workbook) {
        Font columnHeadFont = workbook.createFont();
        columnHeadFont.setFontName("宋体");
        columnHeadFont.setFontHeightInPoints((short) 10);
        columnHeadFont.setBold(true);

        CellStyle columnHeadStyle = workbook.createCellStyle();
        columnHeadStyle.setFont(columnHeadFont);
        columnHeadStyle.setAlignment(HorizontalAlignment.CENTER);
        columnHeadStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        columnHeadStyle.setLocked(true);
        columnHeadStyle.setWrapText(false);
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
    private static CellStyle getDataCellStyle(Workbook workbook, String dataType) {
        Font font = workbook.createFont();
        CreationHelper createHelper = workbook.getCreationHelper();
        font.setFontName("宋体");
        font.setFontHeightInPoints((short) 10);

        // 普通单元格样式
        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.TOP);
        style.setWrapText(false);

        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);

        style.setTopBorderColor((short) 8);
        style.setRightBorderColor((short) 8);
        style.setBottomBorderColor((short) 8);
        style.setLeftBorderColor((short) 8);

        style.setFillForegroundColor((short) 9);

        String format = getColumnDataTypeFormat(dataType);
        if (StringUtils.isNotEmpty(format)) {
            style.setDataFormat(createHelper.createDataFormat().getFormat(format));
        }

        return style;
    }

    private static void setDataCellValue(Object rdata, Cell cell, GridColumn col) {
        Object o;
        if (rdata instanceof Map) {
            o = ((Map) rdata).get(col.getDataIndex());
        } else {
            o = BeanUtils.getFieldValue(rdata, col.getDataIndex());
        }
        if (o == null)
            return;

        Object t = null;
        if (o instanceof Enum) {
            t = ((Enum) o).name();
        } else if (o instanceof TemporalAccessor) {
            try {
                t = DateUtils.toDate((TemporalAccessor) o);
            } catch (Exception ignore) {
                // ignore
            }
        }
        if (null != t) {
            o = t;
        }

        String dataType = StringUtils.trimToEmpty(col.getDataType());
        switch (dataType) {
            case DATE_COLUMN_TYPE:
                if (o instanceof Date) {
                    cell.setCellValue((Date) o);
                } else {
                    try {
                        cell.setCellValue(DateUtils.toDate(o.toString()));
                    } catch (Exception e) {
                        cell.setCellValue(o.toString());
                    }
                }
                break;
            case BOOLEAN_COLUMN_TYPE:
                if (o instanceof Boolean) {
                    if (Boolean.TRUE.equals(o)) {
                        cell.setCellValue("是");
                    } else {
                        cell.setCellValue("否");
                    }
                } else {
                    if (o.toString().equalsIgnoreCase("false") || o.toString().equals("0")) {
                        cell.setCellValue("否");
                    } else {
                        cell.setCellValue("是");
                    }
                }
                break;
            default:
                if (o instanceof Boolean) {
                    if (Boolean.TRUE.equals(o)) {
                        cell.setCellValue("是");
                    } else {
                        cell.setCellValue("否");
                    }
                } else if (o instanceof Date) {
                    cell.setCellValue(DateUtils.formatDateForSecond((Date) o));
                } else {
                    cell.setCellValue(o.toString());
                }
                break;
        }
    }

    private static String getColumnDataTypeFormat(String dataType) {
        if (StringUtils.isEmpty(dataType)) {
            return null;
        }
        String format = BeanUtils.getFieldValue(DATA_TYPE_FORMAT.get(), dataType);
        return StringUtils.isEmpty(format) ? DEFAULT_DATA_TYPE_FORMAT.get(dataType) : format;
    }
}


