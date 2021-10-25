package org.spin.data.delayqueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2021/10/24</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class GroupScheduledHandler implements DelayMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(GroupScheduledTask.class);

    private final String scheduleGroupId;

    public GroupScheduledHandler(String scheduleGroupId) {
        this.scheduleGroupId = scheduleGroupId;
    }

    @Override
    public String getTopic() {
        return scheduleGroupId;
    }

    public void handle(DelayMessage message) throws Exception {
        message.parseHandler().handleMessage(message.getPayload(), logger);
    }

    @Override
    public void handle(String message) throws Exception {
    }

    @Override
    public void handleException(String message, Exception e) {

    }
}
