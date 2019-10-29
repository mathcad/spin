package org.spin.core.util.excel;

import org.junit.jupiter.api.Test;
import org.spin.core.util.file.FileType;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
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
//        try (InputStream fis = loader.getResourceAsStream("ExcelDemo.xls")) {
//            ExcelUtils.readWorkBook(fis, (row -> System.out.println(row.getSheetIndex() + row.getSheetName() + row.getRowIndex() + Arrays.toString(row.getRow()))));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        try (InputStream fis = new FileInputStream(new File("C:\\Users\\Mathcat\\Desktop\\goods(1).xlsx"))) {
            ExcelUtils.readWorkBook(fis, (row -> System.out.println(row.getSheetIndex() + "-" + row.getSheetName() + "-" + row.getRowIndex() + "-" + Arrays.toString(row.getRow()))));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testExport() {
        Iterable<?> sheet1Data = new ArrayList<>();
        Iterable<?> sheet2Data = new ArrayList<>();

        ExcelGrid grid = ExcelGrid.ofFileName("asdfasdf")
            .appendSheet("demo", // 指定sheet名称，在一个工作簿中，sheet名称不能重复
                s -> s.appendColumn("订单号", "orderNo")
                    .appendColumn("下单时间", "createTime")
                    .appendColumn("渠道", "mrchShortCode")
                    .appendColumn("渠道订单号", "jd_order_id")
                    .appendColumn("商品名称/价格", "goodsNames")
                    .appendColumn("订单总价", "amount")
                    .appendColumn("赠送", "presentScore")
                    .appendColumn("买家", "userName")
                    .appendColumn("订单状态", "orderState")
                    .appendColumn("实付", "orderPay")
                    .appendColumn("运费", "orderFreight")
                    .appendColumn("付款时间", "payTime")
                    .appendColumn("支付方式", "channelType")
                    .appendColumn("订单开票", "makeInvoice")
            )
            .appendSheet(s -> s.appendColumn("距离", "distance")); // 不指定sheet名称时，sheet名称默认为Sheet+索引(从1开始，如Sheet1)

        // 写法1
        ExcelModel excelModel = new ExcelModel(grid, sheet1Data, sheet2Data);
        // 写法2 通过sheet名称来指定数据
        // ExcelModel excelModel = new ExcelModel(grid).putData("demo", sheet1Data).putData("Sheet2", sheet2Data);

        ExcelUtils.generateWorkBook(FileType.Document.XLSX, excelModel, () -> new FileOutputStream(new File("D:\\a.xlsx")));

    }
}
