package org.spin.core.concurrent;

/**
 * 分布式锁的凭据
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2020/10/20</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class LockTicket {
    private boolean success;
    private String ticket;
    private String key;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getTicket() {
        return ticket;
    }

    public void setTicket(String ticket) {
        this.ticket = ticket;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
