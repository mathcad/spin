package org.spin.boot.options;

/**
 * Secret存储方式
 * <p>Created by xuweinan on 2017/9/16.</p>
 *
 * @author xuweinan
 */
public enum SecretStorage {
    /**
     * 内存中存储，重启会丢失。分布式部署或需要持久化保存不应使用该方式
     */
    IN_MENORY,

    /**
     * Redis存储，
     */
    REDIS
}
