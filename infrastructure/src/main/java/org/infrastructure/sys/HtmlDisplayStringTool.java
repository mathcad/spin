package org.infrastructure.sys;
/**
 * @ClassName: 
 * @Description: 页面显示相关工具类
 * @author zhaobaodong
 */
public class HtmlDisplayStringTool {
	/**  
	 * 得到一个字符串的长度,显示的长度,一个汉字或日韩文长度为1,英文字符长度为0.5  
	 * @Description: 计算字符串在HTML中的长度,防止控件被超长的字符串逼烂
	 * @param String s 需要得到长度的字符串  
	 * @return int 得到的字符串长度  
	 */   
	public static double getLength(String s) {  
		double valueLength = 0;    
		//中日韩
		String zhongrihan = "([\u2E80-\u9fa5]|[\u0800-\u4e00]|[\uAC00-\uD7A3]){1}";  
		// 获取字段值的长度，如果含中文字符，则每个中文字符长度为2，否则为1    
		for (int i = 0; i < s.length(); i++) {    
			// 获取一个字符    
			String temp = s.substring(i, i + 1);    
			// 判断是否为中文日文韩文字符    
			if (temp.matches(zhongrihan)) {    
				// 中文日文韩文字符长度为1   
				valueLength += 1;    
			} else {    
				// 其他字符长度为0.5    
				valueLength += 0.5;    
			}    
		}    
		//进位取整    
		return  Math.ceil(valueLength);    
	}  

	/**
	 * 
	 * @param s  要弄的字符串
	 * @param max 截取到哪位
	 * @return
	 */
	public static String htmlSubString(String s,int max){
		//中日韩
		String zhongrihan = "([\u2E80-\u9fa5]|[\u0800-\u4e00]|[\uAC00-\uD7A3]){1}"; 
		StringBuilder sb=new StringBuilder();
		double valueLength = 0;    
		for (int i = 0; i < s.length(); i++) {    
			// 获取一个字符    
			String temp = s.substring(i, i + 1);    
			// 判断是否为中文日文韩文字符    
			if (temp.matches(zhongrihan)) {    
				// 中文日文韩文字符长度为1   
				valueLength += 1;
			} else {    
				// 其他字符长度为0.5    
				valueLength += 0.5;    
			}    
			if(valueLength>=max){
				return sb.toString();
			}else{
				sb.append(temp);
			}
		}    
		return sb.toString();
	}

	public static void main(String[] args){
		String a = "暂时没有  asda asdsaddasdad"+
				"暂时没有  asda asdsaddasdad"+
				"暂时没有  asda asdsaddasdad"+
				"暂时没有  asda asdsaddasdad"+
				"暂时没有  asda asdsaddasdad"+
				"暂时没有  asda asdsaddasdad"+
				"暂时没有  asda asdsaddasdad"+
				"暂时没有  asda asdsaddasdad"+
				"暂时没有  asda asdsaddasdad"+
				"暂时没有  asda asdsaddasdad"+
				"暂时没有  asda asdsaddasdad"+
				"暂时没有  asda asdsaddasdad"+
				"暂时没有  asda asdsaddasdad"+
				"暂时没有  asda asdsaddasdad"+
				"暂时没有  asda asdsaddasdad"+
				"暂时没有  asda asdsaddasdad"+
				"暂时没有  asda asdsaddasdad"+
				"暂时没有  asda asdsaddasdad"+
				"暂时没有  asda asdsaddasdad"+
				"暂时没有  asda asdsaddasdad";
		System.out.println(HtmlDisplayStringTool.getLength(a));
		System.out.println(HtmlDisplayStringTool.htmlSubString(a, 70));
	}
}
