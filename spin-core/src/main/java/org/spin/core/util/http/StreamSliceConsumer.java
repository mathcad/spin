package org.spin.core.util.http;

/**
 * 流切片消费者
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2020/9/27</p>
 *
 * @author xuweinan
 * @version 1.0
 */
@FunctionalInterface
public interface StreamSliceConsumer {

    /**
     * 流切片消费开始
     *
     * @param contentType   content类型
     * @param contentLength 总长度
     */
    default void start(String contentType, long contentLength) {
    }

    /**
     * 消费流切片
     *
     * @param slice    内容
     * @param sliceLen 内容实际长度
     * @param percent  百分比
     */
    void accept(byte[] slice, int sliceLen, double percent);

    /**
     * 流消费成功结束
     */
    default void finish() {
    }
}
