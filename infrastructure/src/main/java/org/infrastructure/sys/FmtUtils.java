package org.infrastructure.sys;

import java.text.SimpleDateFormat;


/**
 * 格式化辅助类
 *
 * @author zhou
 */
public class FmtUtils {

    /**
     * 格式化消息
     *
     * @param tplt   模板字符串参数{0},参数2{1}
     * @param params 格式化参数数值
     * @return 格式化后的消息
     */
    public static String format(String tplt, Object... params) {
        String rslt = tplt;
        for (int i = 0; i < params.length; i++) {
            String strVal = (params[i] == null ? "" : params[i].toString());
            rslt = rslt.replace("{" + i + "}", strVal);
        }
        return rslt;
    }

    public static SimpleDateFormat getDateFmt(int len) {
        SimpleDateFormat dateFmt = null;
        if (len == 19)
            dateFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        else if (len == 16)
            dateFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        else if (len == 10)
            dateFmt = new SimpleDateFormat("yyyy-MM-dd");
        return dateFmt;
    }
}
