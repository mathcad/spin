package org.infrastructure.sys;

import java.math.BigDecimal;

public class ChinesizeUtil {
	// private static char[] s1 = { '零', '壹', '贰', '叁', '肆', '伍', '陆', '柒', '捌',
	// '玖' };
	// private static char[] s4 = { '分', '角', '元', '拾', '佰', '仟', '万', '拾', '佰',
	// '仟', '亿', '拾', '佰', '仟', '万' };

	/** 中文简体数字 */
	private static String CHS_NUMBER = "零一二三四五六七八九";

	/** 中文繁体数字 */
	private static String CHT_NUMBER = "零壹贰叁肆伍陆柒捌玖";
	
	/**
	 * 汉语中数字大写
	 */
	private static final String[] CN_UPPER_NUMBER = { "零", "壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖" };
	/**
	 * 汉语中货币单位大写，这样的设计类似于占位符
	 */
	private static final String[] CN_UPPER_MONETRAY_UNIT = { "分", "角", "元", "拾", "佰", "仟", "万", "拾", "佰", "仟", "亿", "拾", "佰", "仟", "兆", "拾", "佰", "仟" };
	/**
	 * 特殊字符：整
	 */
	private static final String CN_FULL = "整";
	/**
	 * 特殊字符：负
	 */
	private static final String CN_NEGATIVE = "负";
	/**
	 * 金额的精度，默认值为2
	 */
	private static final int MONEY_PRECISION = 2;
	/**
	 * 特殊字符：零元整
	 */
	private static final String CN_ZEOR_FULL = "零元" + CN_FULL;

	/**
	 * 把输入的金额转换为汉语中人民币的大写
	 * 
	 * @param numberOfMoney
	 *            输入的金额
	 * @return 对应的汉语大写
	 */
	public static String convertCNY(String money) {
		BigDecimal numberOfMoney = BigDecimal.valueOf(Double.valueOf(money));
		StringBuffer sb = new StringBuffer();
		// -1, 0, or 1 as the value of this BigDecimal is negative, zero, or
		// positive.
		int signum = numberOfMoney.signum();
		// 零元整的情况
		if (signum == 0) {
			return CN_ZEOR_FULL;
		}
		// 这里会进行金额的四舍五入
		long number = numberOfMoney.movePointRight(MONEY_PRECISION).setScale(0, 4).abs().longValue();
		// 得到小数点后两位值
		long scale = number % 100;
		int numUnit = 0;
		int numIndex = 0;
		boolean getZero = false;
		// 判断最后两位数，一共有四中情况：00 = 0, 01 = 1, 10, 11
		if (!(scale > 0)) {
			numIndex = 2;
			number = number / 100;
			getZero = true;
		}
		if ((scale > 0) && (!(scale % 10 > 0))) {
			numIndex = 1;
			number = number / 10;
			getZero = true;
		}
		int zeroSize = 0;
		while (true) {
			if (number <= 0) {
				break;
			}
			// 每次获取到最后一个数
			numUnit = (int) (number % 10);
			if (numUnit > 0) {
				if ((numIndex == 9) && (zeroSize >= 3)) {
					sb.insert(0, CN_UPPER_MONETRAY_UNIT[6]);
				}
				if ((numIndex == 13) && (zeroSize >= 3)) {
					sb.insert(0, CN_UPPER_MONETRAY_UNIT[10]);
				}
				sb.insert(0, CN_UPPER_MONETRAY_UNIT[numIndex]);
				sb.insert(0, CN_UPPER_NUMBER[numUnit]);
				getZero = false;
				zeroSize = 0;
			} else {
				++zeroSize;
				if (!(getZero)) {
					sb.insert(0, CN_UPPER_NUMBER[numUnit]);
				}
				if (numIndex == 2) {
					if (number > 0) {
						sb.insert(0, CN_UPPER_MONETRAY_UNIT[numIndex]);
					}
				} else if (((numIndex - 2) % 4 == 0) && (number % 1000 > 0)) {
					sb.insert(0, CN_UPPER_MONETRAY_UNIT[numIndex]);
				}
				getZero = true;
			}
			// 让number每次都去掉最后一个数
			number = number / 10;
			++numIndex;
		}
		// 如果signum == -1，则说明输入的数字为负数，就在最前面追加特殊字符：负
		if (signum == -1) {
			sb.insert(0, CN_NEGATIVE);
		}
		// 输入的数字小数点后两位为"00"的情况，则要在最后追加特殊字符：整
		if (!(scale > 0)) {
			sb.append(CN_FULL);
		}
		return sb.toString();
	}

	/**
	 * 中文数字转化
	 * 
	 * @param money
	 * @return
	 */
	public static String convertNum(String number) {
		StringBuffer res = new StringBuffer();
		for (int i = 0; i < number.length(); i++) {
			res.append(CHT_NUMBER.charAt(number.charAt(i) - '0'));
		}
		return res.toString();
	}
	
	/**
	 * 中文金额转化
	 * 
	 * @param money
	 * @return
	 */
	public static String convertCNYByCHT(String money) {
		StringBuffer rsult = new StringBuffer();
		String unit = "仟佰拾亿仟佰拾万仟佰拾元角分";
		String moneyStr = money.replace(".", "");
		int pos = unit.length() - moneyStr.length();
		boolean zero = false;
		for (int i = 0; i < moneyStr.length(); i++) {
			if (moneyStr.charAt(i) == '0') {
				zero = true;
				if (((pos + i + 1) % 4) == 0) {
					rsult.append(unit.charAt(pos + i));
					zero = false;
				}
			} else {
				if (zero) {
					rsult.append(CHT_NUMBER.charAt(0));
				}
				zero = false;
				rsult.append(CHT_NUMBER.charAt(moneyStr.charAt(i) - '0')).append(unit.charAt(pos + i));
			}
		}
		if (moneyStr.endsWith("00")) {
			rsult.append('整');
		} else if (moneyStr.endsWith("0")) {
			rsult.append("零分");
		}
		return rsult.toString();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		BigDecimal str=new BigDecimal("12345");
		java.text.DecimalFormat   df   =new   java.text.DecimalFormat("#.00");  
		System.out.println(convertCNY(String.valueOf(df.format(str.doubleValue()))));
	}
}
