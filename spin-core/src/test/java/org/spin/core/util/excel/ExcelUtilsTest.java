package org.spin.core.util.excel;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * description
 *
 * @author wangy QQ 837195190
 * <p>Created by thinkpad on 2018/5/4.<p/>
 */
class ExcelUtilsTest {

    @Test
    void readWorkBook() {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try (InputStream fis = loader.getResourceAsStream("ExcelDemo.xls")) {
            ExcelUtils.readWorkBook(fis, (row -> System.out.println(row.getSheetIndex() + row.getSheetName() + row.getRowIndex() + Arrays.toString(row.getRow()))));
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (InputStream fis = loader.getResourceAsStream("ExcelDemo.xlsx")) {
            ExcelUtils.readWorkBook(fis, (row -> System.out.println(row.getSheetIndex() + row.getSheetName() + row.getRowIndex() + Arrays.toString(row.getRow()))));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}