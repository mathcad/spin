package org.spin.core;

import org.spin.core.util.RandomStringUtils;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * <p>Created by xuweinan on 2017/6/23.</p>
 *
 * @author xuweinan
 */
public class CallableAndFuture {
    public static void main(String[] args) {
        ExecutorService threadPool = Executors.newSingleThreadExecutor();
        Future<String> future2 = threadPool.submit(() -> RandomStringUtils.randomAlphanumeric(8));
        Future<String> future1 = threadPool.submit(() -> {
            Thread.sleep(5000);
            return RandomStringUtils.randomAlphanumeric(8);
        });
        try {
            System.out.println(future2.get());
            System.out.println(future1.get());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        threadPool.shutdown();
    }
}
