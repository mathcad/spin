package org.spin.core.util.excel;

import org.apache.poi.hssf.eventusermodel.HSSFEventFactory;
import org.apache.poi.hssf.eventusermodel.HSSFRequest;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

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
        try (InputStream fis = loader.getResourceAsStream("ExcelDemo.xlsx")) {
            ExcelUtils.readWorkBook(fis, ((row) -> {
                System.out.println(row.getRowlist());
            }));

            ExcelUtils.readWorkBook(
//                new FileInputStream("C:\\Users\\thinkpad\\Desktop\\2.xlsx"),
                loader.getResourceAsStream("ExcelDemo.xls"),
                ((row) -> {
                System.out.println(row.getRow());
            }));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
