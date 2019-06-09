package org.spin.common.id;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * 根据时间环境生成有序id
 * @author YIJIUE
 */
public class Senquence {

    // 生成绝对唯一订单号
    public static String getOrderIdByUUId() {
        Date date=new Date();
        // 局部变量 无需担心多线程安全问题
        DateFormat format = new SimpleDateFormat("yyyyMMdd");
        String time = format.format(date);
        int hashCodeV = UUID.randomUUID().toString().hashCode();
        if (hashCodeV < 0) {//有可能是负数
            hashCodeV = -hashCodeV;
        }
        // 0 代表前面补充0
        // 4 代表长度为4
        // d 代表参数为正数型
        return time + String.format("%011d", hashCodeV);
    }
}
