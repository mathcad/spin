package org.infrastructure.sys;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * 对象序列化反序列化工具
 * 
 * @author yuyong
 * @contact 电话: 18226737060, QQ: 867046050
 * @create 2016年3月4日 上午8:37:03
 * @version V1.0
 */
public class ObjectSerialize {
	// 对象序列化为字符串
	public static String serialize(Object obj) throws Exception {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		ObjectOutputStream objectOutputStream = new ObjectOutputStream(
				byteArrayOutputStream);
		objectOutputStream.writeObject(obj);
		String serStr = byteArrayOutputStream.toString("ISO-8859-1");// 必须是ISO-8859-1
		serStr = java.net.URLEncoder.encode(serStr, "UTF-8");// 编码后字符串不是乱码（不编也不影响功能）
		objectOutputStream.close();
		byteArrayOutputStream.close();
		return serStr;
	}

	// 字符串反序列化为对象
	public static Object unSerialize(String serStr) throws Exception {
		String redStr = java.net.URLDecoder.decode(serStr, "UTF-8");
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
				redStr.getBytes("ISO-8859-1"));
		ObjectInputStream objectInputStream = new ObjectInputStream(
				byteArrayInputStream);
		Object obj = objectInputStream.readObject();
		objectInputStream.close();
		byteArrayInputStream.close();
		return obj;
	}
}
