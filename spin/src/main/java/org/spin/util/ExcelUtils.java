package org.spin.util;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.spin.sys.ErrorAndExceptionCode;
import org.spin.throwable.SimplifiedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class ExcelUtils {
    private static final Logger logger = LoggerFactory.getLogger(ExcelUtils.class);

    public enum Type {
        XLS, XLSX
    }

    /**
     * 读取xls文件内容
     */
    public static void readWorkBook(InputStream is, Type type, RowReader reader) {
        Workbook workbook;
        try {
            if (Type.XLS.equals(type))
                workbook = new HSSFWorkbook(is);
            else
                workbook = new XSSFWorkbook(is);
        } catch (IOException e) {
            throw new SimplifiedException(ErrorAndExceptionCode.IO_FAIL, "读取文件失败", e);
        }
        // 循环工作表Sheet
        for (int numSheet = 0; numSheet < workbook.getNumberOfSheets(); numSheet++) {
            Sheet sheet = workbook.getSheetAt(numSheet);
            if (sheet == null) {
                continue;
            }
            // 循环行Row
            int rowIndex = 0;
            for (int rowNum = 0; rowNum <= sheet.getLastRowNum(); rowNum++) {
                Row row = sheet.getRow(rowNum);
                if (row == null) {
                    continue;
                }

                List<String> rowData = new ArrayList<>();

                Iterator<Cell> cells = row.cellIterator();
                while (cells.hasNext()) {
                    rowData.add(getCellValue(cells.next()));
                }
                reader.readRow(rowIndex, rowData);
                rowIndex++;
            }
        }
    }

    public static void readFromFile(File workbookFile, RowReader reader) {
        Type type;
        if (workbookFile.getName().toLowerCase().endsWith("xls"))
            type = Type.XLS;
        else if (workbookFile.getName().toLowerCase().endsWith("xlsx"))
            type = Type.XLSX;
        else
            throw new SimplifiedException(ErrorAndExceptionCode.IO_FAIL, "不支持的文件类型");
        InputStream is;
        try {
            is = new FileInputStream(workbookFile);
        } catch (FileNotFoundException e) {
            throw new SimplifiedException(ErrorAndExceptionCode.IO_FAIL, "读取文件失败", e);
        }
        readWorkBook(is, type, reader);
    }

    public static void readFromFile(String workbookFilePath, RowReader reader) {
        File workbookFile = new File(workbookFilePath);
        readFromFile(workbookFile, reader);
    }

    /**
     * 得到Excel表中的值
     *
     * @param cell Excel中的每一个单元格
     * @return Excel中每一个单元格中的值
     */
    private static String getCellValue(Cell cell) {
        if (cell.getCellType() == Cell.CELL_TYPE_BOOLEAN) {
            return String.valueOf(cell.getBooleanCellValue());
        } else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
            if (DateUtil.isCellDateFormatted(cell)) {
                Date date = DateUtil.getJavaDate(cell.getNumericCellValue());
                return DateUtils.formatDateForSecond(date);
            } else {
                DecimalFormat nf = new DecimalFormat("####################.################");
                return nf.format(cell.getNumericCellValue());
            }
        } else if (cell.getCellType() == Cell.CELL_TYPE_FORMULA) {
            cell.setCellType(Cell.CELL_TYPE_NUMERIC);
            return String.valueOf(cell.getNumericCellValue());
        } else {
            return String.valueOf(cell.getStringCellValue());
        }
    }

    /**
     * 单行数据读取
     */
    @FunctionalInterface
    public interface RowReader {
        void readRow(int rowIndex, List<String> row);
    }
}