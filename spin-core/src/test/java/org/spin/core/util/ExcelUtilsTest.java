package org.spin.core.util;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

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
            ExcelUtils.readWorkBook(fis, (row) ->
//                System.out.println(rowIndex + ":" + row.get(0))
                    System.out.println(row.getColumnNum() + Arrays.toString(row.getRow()))
            );
            assertTrue(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
