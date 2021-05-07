package org.spin.data.throwable;

import org.spin.core.throwable.SpinException;

/**
 * 分布式锁异常
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2020/3/2</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class DistributedLockException extends SpinException {
    private static final long serialVersionUID = 877281669709324680L;

    public DistributedLockException(String message) {
        super(message);
    }

    public DistributedLockException(String message, Throwable e) {
        super(message, e);
    }
}

