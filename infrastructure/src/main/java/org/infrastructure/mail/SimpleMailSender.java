package org.infrastructure.mail;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

public class SimpleMailSender extends SimpleMailMessage {

	/**
	 * @Fields serialVersionUID : TODO(用一句话描述这个变量表示什么)
	 */
	private static final long serialVersionUID = 45439365287397168L;

	private JavaMailSender sender;
	private MimeMessageHelper messageHellper;
	// private FreeMarkerConfigurer freeMarkerConfigurer;
	private String from;
	private String[] to;
	private String replyTo;
	private String displayName;

	public JavaMailSender getSender() {
		return sender;
	}

	public void setSender(JavaMailSender sender) {
		this.sender = sender;
	}

	public MimeMessageHelper getMessageHellper() {
		return messageHellper;
	}

	public void setMessageHellper(MimeMessageHelper messageHellper) {
		this.messageHellper = messageHellper;
	}

	// public FreeMarkerConfigurer getFreeMarkerConfigurer() {
	// return freeMarkerConfigurer;
	// }
	// public void setFreeMarkerConfigurer(FreeMarkerConfigurer
	// freeMarkerConfigurer) {
	// this.freeMarkerConfigurer = freeMarkerConfigurer;
	// }
	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String[] getTo() {
		return to;
	}

	public void setTo(String[] to) {
		this.to = to;
	}

	public String getReplyTo() {
		return replyTo;
	}

	public void setReplyTo(String replyTo) {
		this.replyTo = replyTo;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
}
