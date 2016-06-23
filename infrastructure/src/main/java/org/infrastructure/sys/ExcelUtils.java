package org.infrastructure.sys;

import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;

/**
 * Excel的辅助处理类，负责导入的Excel内容处理
 * 
 * @author cz
 * 
 * @create 2015.09.07
 * 
 * @version V2.0
 * */
public class ExcelUtils {
	/**
     * 读取xls文件内容
     * 
     * @return List<XlsDto>对象
     * @throws IOException
     *             输入/输出(i/o)异常
     */
    public static void readXls(InputStream is,RowReader reader) throws IOException {
        HSSFWorkbook hssfWorkbook = new HSSFWorkbook(is);

        // 循环工作表Sheet
        for (int numSheet = 0; numSheet < hssfWorkbook.getNumberOfSheets(); numSheet++) {
            HSSFSheet hssfSheet = hssfWorkbook.getSheetAt(numSheet);
            if (hssfSheet == null) {
                continue;
            }
            // 循环行Row
            int rowIndex = 0;
            for (int rowNum = 0; rowNum <= hssfSheet.getLastRowNum(); rowNum++) {
                HSSFRow hssfRow = hssfSheet.getRow(rowNum);
                if (hssfRow == null) {
                    continue;
                }
                
                Iterator<Cell> cells = hssfRow.cellIterator();
                List<String> row = new ArrayList<String>();
                
                while(cells.hasNext()){
                	HSSFCell c = (HSSFCell)cells.next();
                	row.add(getCellValue(c));
                	
                }
                reader.readRow(rowIndex,row.toArray(new String[]{}));
                rowIndex++;
            }
        }
    }
 
    /**
     * 得到Excel表中的值
     * 
     * @param cell
     *            Excel中的每一个格子
     * @return Excel中每一个格子中的值
     */
    @SuppressWarnings("static-access")
    static String getCellValue(HSSFCell cell) {
        if (cell.getCellType() == cell.CELL_TYPE_BOOLEAN) {
            return String.valueOf(cell.getBooleanCellValue());
        } else if (cell.getCellType() == cell.CELL_TYPE_NUMERIC) {
        	
    		SimpleDateFormat miDtFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        	if (HSSFDateUtil.isCellDateFormatted(cell)) {
        		double d = cell.getNumericCellValue();
    			Date date = HSSFDateUtil.getJavaDate(d);
    			return miDtFmt.format(date);
        	}else{
        		DecimalFormat nf = new DecimalFormat("####################.################");  
        		return nf.format(cell.getNumericCellValue());        		
        	}
        }else if(cell.getCellType() == cell.CELL_TYPE_FORMULA){
        	cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);        
            return String.valueOf(cell.getNumericCellValue());     
        }else{
            return String.valueOf(cell.getStringCellValue());
        } 
    }
    
    /**
     * 单行数据读取
     * 
     * @author zhouxiang2
     *
     */
    public static interface RowReader{
    	
    	void readRow(int rowIndex, String[] row);
    }

}
