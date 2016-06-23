package org.infrastructure.mail;

import java.util.HashMap;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.infrastructure.freemarker.SimpleFreeMarkerConfigurer;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import freemarker.template.Template;

/**
 * 邮件服务
 * MailSendService
 * 邮件服务
 * @author richard_du
 * 2014年5月10日 上午11:26:21
 *
 */
public class MailSendService {
	private JavaMailSender sender;
	private SimpleFreeMarkerConfigurer freeMarkerConfigurer; // FreeMarker的技术类

	// 自定义类属性
	private SimpleMailSender mailSender;

	public void setMailSender(SimpleMailSender mailSender) {
		this.mailSender = mailSender;
	}

	public void setFreeMarkerConfigurer(SimpleFreeMarkerConfigurer freeMarkerConfigurer) {
		this.freeMarkerConfigurer = freeMarkerConfigurer;
	}

	public void setSender(JavaMailSender sender) {
		this.sender = sender;
	}
	
	//通过模板构建邮件内容
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private String getMailText(String operatorId, String content, String templete) {
		String htmlText = "";
		try {
			// 通过指定模板名获取FreeMarker模板实例
			Template tpl = freeMarkerConfigurer.getConfiguration().getTemplate(templete);
			// FreeMarker通过Map传递动态数据
			Map map = new HashMap();
			map.put("email", mailSender.getTo()[0]);
			map.put("operatorId", operatorId); // 注意动态数据的key和模板标签中指定的属性相匹配
			map.put("content", content);
			// 解析模板并替换动态数据
			htmlText = FreeMarkerTemplateUtils.processTemplateIntoString(tpl, map);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return htmlText;
	}

	/**发送模板邮件
	 * @param opertatorId 管理员
	 * @param content  邮件内容
	 * @param adminMail 管理员邮箱
	 * @throws MessagingException
	 */
	public void sendTemplateMail(String opertatorId, String content, String adminMail, String templete)
			throws MessagingException {
		MimeMessage msg = sender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(msg, false, "utf-8");// 由于是html邮件
		helper.setFrom(((JavaMailSenderImpl) sender).getUsername());
		helper.setTo(adminMail);
		helper.setSubject("【贷多少】邮箱认证");
		String htmlText = getMailText(opertatorId, content, templete);// 使用模板生成html邮件内容
		helper.setText(htmlText, true);

		sender.send(msg);
		System.out.println("成功发送模板邮件");
	}

	/**发送测试邮件（指定好发内容和接收邮箱地址
	 * @param opertatorId 管理员
	 * @param content  邮件内容
	 * @throws MessagingException
	 */
	public void sendTestMail(String opertatorId, String content, String[] toMail, String templete)
			throws MessagingException {
		MimeMessage msg = sender.createMimeMessage();
		mailSender.setTo(toMail);
		MimeMessageHelper helper = new MimeMessageHelper(msg, false, "utf-8");// 由于是html邮件
		helper.setSubject("【贷多少】邮箱认证");
		String htmlText = getMailText(opertatorId, content, templete);// 使用模板生成html邮件内容
		helper.setText(htmlText, true);
		helper.setFrom(((JavaMailSenderImpl) sender).getUsername());
		helper.setTo(mailSender.getTo());

		sender.send(msg);
		System.out.println("成功发送模板邮件");
	}
}
