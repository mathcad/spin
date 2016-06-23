package org.infrastructure.sys;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Map;

import org.infrastructure.util.HashUtils;
import org.infrastructure.util.StringUtils;


/**
 * 格式化辅助类
 *
 * @author zhou
 */
public class FmtUtils {

    /**
     * 坐标补零
     *
     * @param number
     * @param o
     * @return
     */
    public static String fillZero(int number, Object o) {
        String pt = "";
        for (int i = 0; i < number; i++) {
            pt += "0";
        }
        java.text.DecimalFormat df = new java.text.DecimalFormat(pt);
        return df.format(o);
    }

    /**
     * 模糊文本内容，替换为*号
     *
     * @param content
     * @param start
     * @param len
     * @return
     * @version 1.0
     */
    public static String replaceStars(String content, int start, int len) {
        StringBuilder newStr = new StringBuilder();
        newStr.append(content.substring(0, start));
        for (int i = 0; i < len; i++)
            newStr.append("*");
        newStr.append(content.substring(start + len));
        return newStr.toString();
    }

    /**
     * 格式化
     *
     * @param dv
     * @param dg
     * @return
     */
    public static String formatDoublel(Double dv, int dg) {
        String pt = "";
        for (int i = 0; i < dg; i++) {
            pt += "0";
        }
        java.text.DecimalFormat df = new java.text.DecimalFormat("#." + pt);
        return df.format(dv);
    }

    /**
     * 格式化消息
     *
     * @param tplt   模板字符串参数{0},参数2{1}
     * @param params 格式化参数数值
     * @return
     */
    public static String format(String tplt, Object... params) {
        String rslt = tplt;
        for (int i = 0; i < params.length; i++) {
            String strVal = (params[i] == null ? "" : params[i].toString());
            rslt = rslt.replace("{" + i + "}", strVal);
        }
        return rslt;
    }

    /**
     * 格式化消息
     * {}插值
     *
     * @param tplt   模板字符串参数{key1},参数2{key2}
     * @param params 格式化参数数值 Map本身
     * @return 格式化后的字符串
     */
    public static String format(String tplt, Map params) {
        String rslt = tplt;
        for (Object key : params.keySet()) {
            String strVal = HashUtils.getStringValue(params, key.toString());
            String strRep = "{" + key + "}";
            if (rslt.contains(strRep)) {
                rslt = rslt.replace(strRep, strVal == null ? "" : strVal);
            }
        }
        return rslt;
    }


    /**
     * 格式化消息
     * ${}插值
     *
     * @param tplt   模板字符串参数{key1},参数2{key2}
     * @param params 格式化参数数值 Map本身
     * @return 格式化后的字符串
     */
    @SuppressWarnings("unchecked")
    public static String format$(String tplt, Map params) {
        String rslt = tplt;
        for (Object key : params.keySet()) {
            String strVal = HashUtils.getStringValue(params, key.toString());
            String strRep = "${" + key + "}";
            if (rslt.contains(strRep)) {
                rslt = rslt.replace(strRep, strVal == null ? "" : strVal);
            }
        }
        return rslt;
    }

    public static SimpleDateFormat getDateFmt(boolean secondOrDay) {
        return !secondOrDay ? new SimpleDateFormat("yyyy-MM-dd") : new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
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

    /**
     * 字符串转TIMESTAMP
     *
     * @throws Exception
     */
    public static Timestamp getTimestamp(String date) throws Exception {
        if (StringUtils.isNotEmpty(date)) {
            return new Timestamp(getDateFmt(date.length()).parse(date).getTime());
        }
        return null;
    }

}
