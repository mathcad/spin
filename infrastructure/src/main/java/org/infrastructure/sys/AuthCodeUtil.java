package org.infrastructure.sys;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.infrastructure.sms.SMSUtils;

public class AuthCodeUtil implements Serializable {

	/**
	 * 验证码图片
	 */
	private static final long serialVersionUID = 1L;
	private static char[] mapTable = { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h',
			'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u',
			'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7',
			'8', '9' };

	public static String getImageCode(int width, int height, OutputStream os,HttpServletRequest request) {
		if (width <= 0)
			width = 60;
		if (height <= 0)
			height = 20;
		BufferedImage image = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_RGB);
		// 获取图形上下文
		Graphics g = image.getGraphics();
		// 设定背景色
		g.setColor(new Color(255, 255, 255));
		g.fillRect(0, 0, width, height);
		// 画边框
		g.setColor(new Color(0xFF8D66));
		g.drawRect(0, 0, width - 1, height - 1);
		// 取随机产生的认证码
		String strEnsure = "";
		// 4代表4位验证码,如果要生成更多位的认证码,则加大数值
		for (int i = 0; i < 4; ++i) {
			strEnsure += mapTable[(int) (mapTable.length * Math.random())];
		}
		// 　　将认证码显示到图像中,如果要生成更多位的认证码,增加drawString语句
		g.setColor(new Color(0x285a75));
		g.setFont(new Font("Atlantic Inline", Font.BOLD|Font.ITALIC, 26));
		String str = strEnsure.substring(0, 1);
		g.drawString(str, 8, 23);
		str = strEnsure.substring(1, 2);
		g.drawString(str, 31, 18);
		str = strEnsure.substring(2, 3);
		g.drawString(str, 54, 15);
		str = strEnsure.substring(3, 4);
		g.drawString(str, 77, 20);
		// 随机产生50个干扰点
		Random rand = new Random();
		for (int i = 0; i < 50; i++) {
			int x = rand.nextInt(width);
			int y = rand.nextInt(height);
			g.drawOval(x, y, 1, 1);
		}
		//画一条奇怪随机的黑色曲线,,我放弃了
//		for (int i = 0; i < 50; i++) {
//		
//		}
		
		// 释放图形上下文
		g.dispose();
		try {
			HttpSession session=request.getSession();
			session.setAttribute("imageCode", strEnsure);
//			session.setMaxInactiveInterval(300);//设置session的最大值是3分钟
			// 输出图像到页面
			ImageIO.write(image, "JPEG", os);
		} catch (IOException e) {
			return "";
		}
		return strEnsure;
	}
	/***
	 * 数字验证码
	 * @param length
	 * @return
	 */
	public static String getRandomNum(int length){
	String num="";
	if(length>0){
		for(int i=0;i<length;i++){
			num+=(int)(Math.random()*10);
		}
	 }else{
		 num=String.valueOf((int)(Math.random()*10000));
	 } 
	return num;
	}
	/***
	 * 生成数字验证码
	 * @return
	 */
	public static String getRandomNum(){
		return getRandomNum(0);
	}
	//手机获得数字验证码
	public static String getNumAuthCode(String mobile,String code){
			String content;
			 if(null!=mobile && !("").equals(mobile+"")){
				 try {
					content =	SMSUtils.sendSMS(mobile, code);
					
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
					return "0";
				}
				 if(Constants.SMS_RECIVE_SUCCESS.equals(content)){
					 return "1";
				 }
			 }
			 return "0";
		}
}
