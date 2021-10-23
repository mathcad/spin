package org.spin.data.delayqueue;

/**
 * 调度误差滑动窗口
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2021/10/23</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class OffsetWindow {
    private static final int WINDOW_SIZE = 5;

    private final long[] offset = new long[WINDOW_SIZE];
    private int cnt = 0;

    public void put(long offset, long ceil) {
        this.offset[cnt++] = Math.min(offset, ceil);
        cnt = cnt % WINDOW_SIZE;
    }

    public long getOffset() {
        long accu = 0;
        int i = 0;
        while (i < offset.length) {
            accu += offset[i++];
        }
        return accu / WINDOW_SIZE;
    }
}
