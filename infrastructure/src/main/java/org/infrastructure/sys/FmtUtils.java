package org.infrastructure.sys;

import java.text.SimpleDateFormat;


/**
 * 格式化辅助类
 *
 * @author zhou
 */
public class FmtUtils {

    private static final String yearPattern = "\\d{4}";
    private static final String shortYearPattern = "\\d{2}";
    private static final String alignMonthPattern = "0[1-9]|1[0-2]";
    private static final String alignDayPattern = "0[1-9]|[1-2]\\d|3[0-1]";
    private static final String alignHourPattern = "0[1-9]|1[0-2]";
    private static final String alignMinutePattern = "[0-5]\\d";
    private static final String alignSecondPattern = "[0-5]\\d";

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
