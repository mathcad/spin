package org.spin.data.delayqueue;

import org.spin.core.util.MathUtils;

/**
 * 调度误差滑动窗口
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2021/10/23</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class OffsetWindow {
    private static final int WINDOW_SIZE = 100;

    private final long[] offset = new long[WINDOW_SIZE];
    private int idx = 0;
    private int cnt = 0;

    public void put(long offset, long ceil) {
        this.offset[idx++] = Math.min(offset, ceil);
        if (cnt < WINDOW_SIZE) cnt = idx;
        idx %= WINDOW_SIZE;
    }

    public long getOffset() {
        if (0 == cnt) return 0L;
        long accu = 0;
        int i = 0;
        while (i < offset.length) {
            accu += offset[i++];
        }
        return accu / cnt;
    }
}
