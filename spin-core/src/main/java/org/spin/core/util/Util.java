package org.spin.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.function.serializable.Consumer;

import java.util.concurrent.*;

/**
 * 表示一个工具类，不允许实例化
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2020/7/8</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public abstract class Util {

    private static final Logger logger = LoggerFactory.getLogger(Util.class);
    private static final ConcurrentHashMap<Class<? extends Util>, CountDownLatch> DOWN_LATCH_MAP = new ConcurrentHashMap<>();
    private static final LinkedBlockingDeque<Class<? extends Util>> INIT_QUEUE = new LinkedBlockingDeque<>();
    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private static volatile boolean init = false;

    protected Util() {
        throw new UnsupportedOperationException("工具类不允许实例化");
    }

    public static synchronized void setInitiator(Consumer<Class<? extends Util>> initiator) {
        if (init) {
            return;
        }
        executorService.submit(() -> {
            while (true) {
                try {
                    Class<? extends Util> aClass = INIT_QUEUE.poll(5, TimeUnit.MINUTES);

                    if (null != aClass) {
                        initiator.accept(aClass);
                    }
                } catch (Exception e) {
                    logger.error("UtilClass 初始化异常", e);
                }
            }
        });
        init = true;
    }


    protected static void registerLatch(Class<? extends Util> utilClass) {
        if (!DOWN_LATCH_MAP.containsKey(utilClass)) {
            INIT_QUEUE.add(utilClass);
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
