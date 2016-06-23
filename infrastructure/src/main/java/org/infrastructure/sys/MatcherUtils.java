package org.infrastructure.sys;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 规则验证公用方法
 *
 * @author yebo
 * @contact 电话: 18755388130, QQ: 48831414
 * @create 2015年8月4日 上午11:02:10
 * @version V1.0
 */
public class MatcherUtils {

	/** 手机或者固定电话 */
	final public static String _TEL_OR_MOB = "(1[2|3|4|5|7|8|][0-9]{9})|((0[0-9]{2,3}\\-)?([2-9][0-9]{6,7})+(\\-[0-9]{1,4})?)";

	/** 手机 */
	final public static String _MOB = "1[2|3|4|5|7|8|][0-9]{9}";

	/** 固话 */
	final public static String _TEL = "(0[0-9]{2,3}\\-)?([2-9][0-9]{6,7})+(\\-[0-9]{1,4})?";

	/**
	 * 验证字符串是否满足正则表达式规则
	 * 
	 * @param reg
	 *            正则表达式
	 * @param str
	 *            字符串
	 * @return
	 */
	public static boolean isMatch(String reg, String str) {
		Pattern p = Pattern.compile(reg);
		return p.matcher(str).matches();
	}

	/**
	 * 截取符合正则表达式的部分字符串
	 * 
	 * @param reg
	 *            正则表达式
	 * @param str
	 *            字符串
	 * @return
	 */
	public static List<String> listMatches(String reg, String str) {
		List<String> ms = new ArrayList<String>();
		Pattern p = Pattern.compile(reg);
		Matcher m = p.matcher(str);
		while (m.find()) {
			ms.add(m.group());
		}
		return ms;
	}

	public static void main(String[] args) {
		// 座机
		String tel = "ABDCASDASD0571-88292913123123123";
		System.out.println(listMatches(_TEL, tel));
		// 手机
		String mob = "18712345678ASDASD;;;11231;;192;2345678;;asdasd234567813012345678";
		System.out.println(listMatches(_MOB, mob));

		// 混合
		String all = tel + mob;
		System.out.println(listMatches(_TEL_OR_MOB, all));

	}
}
