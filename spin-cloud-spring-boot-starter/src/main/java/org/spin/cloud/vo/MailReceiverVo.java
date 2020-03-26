package org.spin.cloud.vo;


import java.io.Serializable;

public class MailReceiverVo implements Serializable {

    private static final long serialVersionUID = 2572049154391543010L;

    /**
     * 企业ID
     */
    private Long enterpriseId;

    /**
     * 接受人ID
     */
    private String receiver;

    public static MailReceiverVo aReceiver(String receiver, Long enterpriseId) {
        return new MailReceiverVo().withReceiver(receiver).withEnterpriseId(enterpriseId);
    }

    public static MailReceiverVo aReceiver(String receiver) {
        return new MailReceiverVo().withReceiver(receiver);
    }

    public MailReceiverVo withEnterpriseId(Long enterpriseId) {
        this.enterpriseId = enterpriseId;
        return this;
    }

    public MailReceiverVo withReceiver(String receiver) {
        this.receiver = receiver;
        return this;
    }

    public Long getEnterpriseId() {
        return enterpriseId;
    }

    public void setEnterpriseId(Long enterpriseId) {
        this.enterpriseId = enterpriseId;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }
}
