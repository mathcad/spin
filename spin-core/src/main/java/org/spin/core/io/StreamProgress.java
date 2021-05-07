package org.spin.core.io;

/**
 * Stream进度条
 */
@FunctionalInterface
public interface StreamProgress {

    /**
     * 开始
     */
    default void start() {
    }

    default void progress(long progressSize) {
        progress(progressSize, "");
    }

    /**
     * 进行中
     *
     * @param progressSize 已经进行的大小
     * @param remark       备注
     */
    void progress(long progressSize, String remark);

    /**
     * 结束
     */
    default void finish() {
    }
}
