package org.spin.data.delayqueue;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2021/10/23</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class DelayMessage {
    private String messageId;
    private String topic;
    private long delayTimeInMillis;
    private long triggerTime;
    private long scheduleTime;
    private String payload;

    public DelayMessage(String messageId, String topic, long delayTimeInMillis, long triggerTime, String payload) {
        this.messageId = messageId;
        this.topic = topic;
        this.delayTimeInMillis = delayTimeInMillis;
        this.triggerTime = triggerTime;
        this.payload = payload;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public long getDelayTimeInMillis() {
        return delayTimeInMillis;
    }

    public void setDelayTimeInMillis(long delayTimeInMillis) {
        this.delayTimeInMillis = delayTimeInMillis;
    }

    public long getTriggerTime() {
        return triggerTime;
    }

    public void setTriggerTime(long triggerTime) {
        this.triggerTime = triggerTime;
    }

    public long getScheduleTime() {
        return scheduleTime;
    }

    public void setScheduleTime(long scheduleTime) {
        this.scheduleTime = scheduleTime;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    @Override
    public String toString() {
        return "{" +
            "\"messageId\":\"" + messageId + '\"' +
            ",\"topic\":\"" + topic + '\"' +
            ",\"delayTimeInMillis\":" + delayTimeInMillis +
            ",\"triggerTime\":" + triggerTime +
            ",\"scheduleTime\":" + scheduleTime +
            ",\"payload\":\"" + payload + '\"' +
            '}';
    }
}
