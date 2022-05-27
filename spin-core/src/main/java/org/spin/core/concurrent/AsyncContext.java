package org.spin.core.concurrent;

import org.spin.core.util.StringUtils;

import java.util.HashMap;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2021/4/7</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class AsyncContext extends HashMap<String, Object> {
    private final String scheduleThreadName;

    public AsyncContext() {
        scheduleThreadName = Thread.currentThread().getName();
    }

    public String getScheduleThreadName() {
        return scheduleThreadName;
    }

    @SuppressWarnings("unchecked")
    public <T> T getObj(String key) {
        return (T) get(key);
    }

    public String getString(String key) {
        return StringUtils.toString(get(key));
    }
}
