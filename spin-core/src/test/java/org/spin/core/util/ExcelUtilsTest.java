package org.spin.core.util;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * <p>Created by xuweinan on 2018/5/6.</p>
 *
 * @author xuweinan
 */
class ExcelUtilsTest {

    @Test
    void readWorkBook() {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try (InputStream fis = loader.getResourceAsStream("ExcelDemo.xls")) {
            ExcelUtils.readWorkBook(fis, (sheetIndex, sheetName, rowIndex, row) -> System.out.println(rowIndex + ":" + row.get(0)));
            assertTrue(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
