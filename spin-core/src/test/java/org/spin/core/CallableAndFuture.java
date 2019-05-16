package org.spin.core;

import org.spin.core.util.excel.ExcelUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * <p>Created by xuweinan on 2017/6/23.</p>
 *
 * @author xuweinan
 */
public class CallableAndFuture {
    public static void main(String[] args) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
//        try (InputStream fis = loader.getResourceAsStream("ExcelDemo.xls")) {
//            ExcelUtils.readWorkBook(fis, (row -> System.out.println(row.getSheetIndex() + row.getSheetName() + row.getRowIndex() + Arrays.toString(row.getRow()))));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        try (InputStream fis = new FileInputStream(new File("C:\\Users\\Mathcat\\Desktop\\充值订单明细-20190510114722.xlsx"))) {
            ExcelUtils.readWorkBook(fis, (row -> System.out.println(row.getSheetIndex() + row.getSheetName() + row.getRowIndex() + Arrays.toString(row.getRow()))));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
