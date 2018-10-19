package org.spin.core.util;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.spin.core.ErrorCode;
import org.spin.core.function.FinalConsumer;
import org.spin.core.io.BytesCombinedInputStream;
import org.spin.core.throwable.SimplifiedException;
import org.spin.core.util.excel.ExcelRow;
import org.spin.core.util.file.FileType;
import org.spin.core.util.file.FileTypeUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Iterator;
import java.util.Objects;

/**
 * Excel工具类
 * <p>Created by xuweinan on 2017/9/3.</p>
 *
 * @author xuweinan
 */
public abstract class ExcelUtils {

    public enum Type {
        XLS, XLSX
    }

    /**
     * 读取Excel文件内容
     *
     * @param is     输入流
     * @param reader Excel行处理器
     */
    public static void readWorkBook(InputStream is, FinalConsumer<ExcelRow> reader) {
        byte[] trait = new byte[16];
        FileType fileType;
        BytesCombinedInputStream bcis;
        try {
            bcis = new BytesCombinedInputStream(is, 16);
            bcis.readCombinedBytes(trait);
            fileType = FileTypeUtils.detectFileType(trait);
        } catch (IOException e) {
            throw new SimplifiedException(ErrorCode.IO_FAIL, "输入流读取失败", e);
        }
        if (Objects.isNull(fileType)) {
            throw new SimplifiedException(ErrorCode.IO_FAIL, "不支持的文件类型");
        } else if (fileType.equals(FileType.Document.XLS)) {
            readWorkBook(bcis, Type.XLS, reader);
        } else if (fileType.equals(FileType.Document.XLSX)) {
            readWorkBook(bcis, Type.XLSX, reader);
        } else {
            throw new SimplifiedException(ErrorCode.IO_FAIL, "不支持的文件类型");
        }
    }

    /**
     * 读取Excel文件内容
     *
     * @param is     输入流
     * @param type   类型，xls或xlsx
     * @param reader Excel行处理器
     */
    public static void readWorkBook(InputStream is, Type type, FinalConsumer<ExcelRow> reader) {
        Workbook workbook;
        try {
            if (Type.XLS.equals(type)) {
                workbook = new HSSFWorkbook(is);
            } else {
                workbook = new XSSFWorkbook(is);
            }
        } catch (IOException e) {
            throw new SimplifiedException(ErrorCode.IO_FAIL, "读取文件失败", e);
        }
        // 循环工作表Sheet
        for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
            Sheet sheet = workbook.getSheetAt(sheetIndex);
            if (sheet == null) {
                continue;
            }
            ExcelRow rowData = new ExcelRow(sheetIndex, sheet.getSheetName(), -1, sheet.getRow(sheet.getLastRowNum()).getLastCellNum() * 3);
            // 循环行Row
            Row row;
            boolean hasValidCell;
            for (int rowNum = 0; rowNum <= sheet.getLastRowNum(); rowNum++) {
                hasValidCell = false;
                row = sheet.getRow(rowNum);
                if (row == null || row.getLastCellNum() < 1) {
                    continue;
                }
                rowData.cleanRow();
                rowData.setRowIndex(rowNum);

                Iterator<Cell> cells = row.cellIterator();
                String cellValue;
                Cell cell;
                while (cells.hasNext()) {
                    cell = cells.next();
                    cellValue = getCellValue(cell);
                    rowData.setColumn(cell.getColumnIndex(), cellValue);
                    hasValidCell = hasValidCell || StringUtils.isNotEmpty(cellValue);
                }
                if (hasValidCell) {
                    reader.accept(rowData);
                }
            }
        }
    }

    /**
     * 读取Excel文件内容
     *
     * @param workbookFile Excel文件
     * @param reader       Excel行处理器
     */
    public static void readFromFile(File workbookFile, FinalConsumer<ExcelRow> reader) {
        Type type;
        if (workbookFile.getName().toLowerCase().endsWith("xls")) {
            type = Type.XLS;
        } else if (workbookFile.getName().toLowerCase().endsWith("xlsx")) {
            type = Type.XLSX;
        } else {
            throw new SimplifiedException(ErrorCode.IO_FAIL, "不支持的文件类型");
        }

        try (InputStream is = new FileInputStream(workbookFile)) {
            readWorkBook(is, type, reader);
        } catch (FileNotFoundException e) {
            throw new SimplifiedException(ErrorCode.IO_FAIL, "读取的文件不存在", e);
        } catch (IOException e) {
            throw new SimplifiedException(ErrorCode.IO_FAIL, "读取文件失败", e);
        }
    }

    /**
     * 读取Excel文件内容
     *
     * @param workbookFilePath Excel文件路径
     * @param reader           Excel行处理器
     */
    public static void readFromFile(String workbookFilePath, FinalConsumer<ExcelRow> reader) {
        File workbookFile = new File(workbookFilePath);
        readFromFile(workbookFile, reader);
    }

    /**
     * 得到Excel表中的值
     *
     * @param cell Excel中的每一个单元格
     * @return 单元格的值(字符串形式)
     */
    private static String getCellValue(Cell cell) {
        String result;
        switch (cell.getCellType()) {
            case STRING:
                result = cell.getStringCellValue();
                break;
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    Date date = cell.getDateCellValue();
                    result = DateUtils.formatDateForSecond(date);
                } else {
                    result = String.valueOf(cell.getNumericCellValue());
                }
                break;
            case FORMULA:
                try {
                    result = cell.getStringCellValue();
                } catch (IllegalStateException ignore) {
                    if (DateUtil.isCellDateFormatted(cell)) {
                        result = DateUtils.formatDateForSecond(cell.getDateCellValue());
                    } else {
                        result = String.valueOf(cell.getNumericCellValue());
                    }
                }
                break;
            case BOOLEAN:
                result = String.valueOf(cell.getBooleanCellValue());
                break;
            case BLANK:
                result = "";
                break;
            default:
                result = String.valueOf(cell.getStringCellValue());

        }
        return result;
    }
}
