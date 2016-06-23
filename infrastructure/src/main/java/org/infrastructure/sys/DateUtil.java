package org.infrastructure.sys;

import java.util.Calendar;
import java.util.Date;

public class DateUtil {
	/**
	 * 获得某时间过去周数的时间
	 * 
	 * @param date
	 * @param amount
	 * @return
	 */
	public static Date[] getPassWeekDates(Date date, Integer amount) {
		Date[] dates = new Date[2];
		Calendar dateStart = Calendar.getInstance();
		dateStart.setTime(date);
		dateStart.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		dateStart.add(Calendar.DAY_OF_MONTH, -amount * 7);

		Calendar dateEnd = Calendar.getInstance();
		dateEnd.setTime(date);
		dateEnd.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
		dateEnd.add(Calendar.DAY_OF_MONTH, -(amount - 1) * 7);

		dates[0] = dateStart.getTime();
		dates[1] = dateEnd.getTime();
		return dates;
	}

	/**
	 * 获得某时间过去月数的时间
	 * 
	 * @param date
	 * @param amount
	 * @return
	 */
	public static Date[] getPassMonthDates(Date date, int amount) {
		Date[] dates = new Date[2];
		Calendar dateStart = Calendar.getInstance();
		dateStart.setTime(date);
		dateStart.set(Calendar.DAY_OF_MONTH, dateStart.getActualMinimum(Calendar.DAY_OF_MONTH));
		dateStart.add(Calendar.MONTH, -amount);

		Calendar dateEnd = Calendar.getInstance();
		dateEnd.setTime(date);
		dateEnd.set(Calendar.DAY_OF_MONTH, dateEnd.getActualMaximum(Calendar.DAY_OF_MONTH));
		dateEnd.add(Calendar.MONTH, -amount);

		dates[0] = dateStart.getTime();
		dates[1] = dateEnd.getTime();
		return dates;
	}

	/**
	 * 日期加减公用方法
	 * 
	 * @param date
	 *            原日期
	 * @param type
	 *            日期单位 Calendar.YEAR Calendar.MONTH Calendar.DAY ...
	 * @param amt
	 *            加减数
	 * @return
	 */
	public static Date calculate(Date date, int type, int amt) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(type, amt);
		return cal.getTime();
	}

}
