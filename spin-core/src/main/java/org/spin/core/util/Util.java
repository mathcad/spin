package org.spin.core.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 表示一个工具类，不允许实例化
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2020/7/8</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public abstract class Util {

    protected Util() {
        throw new UnsupportedOperationException("工具类不允许实例化");
    }

    private static final ConcurrentHashMap<Class<? extends Util>, CountDownLatch> DOWN_LATCH_MAP = new ConcurrentHashMap<>();

    protected static void registerLatch(Class<? extends Util> utilClass) {
        if (!DOWN_LATCH_MAP.containsKey(utilClass)) {
            DOWN_LATCH_MAP.put(utilClass, new CountDownLatch(1));
        }
    }

    protected static void ready(Class<? extends Util> utilClass) {
        if (DOWN_LATCH_MAP.containsKey(utilClass)) {
            DOWN_LATCH_MAP.get(utilClass).countDown();
        } else {
            DOWN_LATCH_MAP.put(utilClass, new CountDownLatch(0));
        }
    }

    protected static void awaitUntilReady(Class<? extends Util> utilClass) {
        if (DOWN_LATCH_MAP.containsKey(utilClass)) {
            try {
                DOWN_LATCH_MAP.get(utilClass).await(30L, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                // do nothing
            }
        }
    }
}
