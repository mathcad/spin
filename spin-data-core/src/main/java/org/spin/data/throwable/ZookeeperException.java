package org.spin.data.throwable;

import org.apache.zookeeper.KeeperException;
import org.spin.core.ErrorCode;
import org.spin.core.throwable.SimplifiedException;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2020/3/2</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class ZookeeperException extends SimplifiedException {
    private static final long serialVersionUID = -6315329503841905147L;

    private final String path;

    public ZookeeperException(KeeperException e) {
        super(ErrorCode.with(e.code().intValue(), e.code().name()), e);
        this.path = e.getPath();
    }

    public ZookeeperException(KeeperException e, String message) {
        super(ErrorCode.with(e.code().intValue(), e.code().name()), message, e);
        this.path = e.getPath();
    }

    public String getPath() {
        return path;
    }
}

