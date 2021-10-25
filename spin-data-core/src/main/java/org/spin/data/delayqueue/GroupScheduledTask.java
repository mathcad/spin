package org.spin.data.delayqueue;

import org.slf4j.Logger;

import java.io.Serializable;

/**
 * 集群调度任务
 * <p>禁止捕获不可序列化的变量</p>
 * <p>Created by xuweinan on 2021/10/24</p>
 *
 * @author xuweinan
 * @version 1.0
 */
@FunctionalInterface
public interface GroupScheduledTask extends Serializable {

    void handleMessage(String message, Logger logger) throws Exception;
}
