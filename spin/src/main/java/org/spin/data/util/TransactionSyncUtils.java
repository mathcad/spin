package org.spin.data.util;

import org.spin.core.function.BoolConsumer;
import org.spin.core.function.Handler;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.function.IntConsumer;

/**
 * 事务过程中插入额外逻辑的工具类
 * <p>Created by xuweinan on 2017/10/6.</p>
 *
 * @author xuweinan
 */
public class TransactionSyncUtils {

    /**
     * 事务提交前执行操作
     *
     * @param body  需要执行的操作
     * @param order 优先级。较小的值拥有更高的优先级
     */
    public static void beforeCommit(final BoolConsumer body, final int order) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void beforeCommit(boolean readOnly) {
                body.accept(readOnly);
            }

            @Override
            public int getOrder() {
                return order;
            }
        });
    }

    /**
     * 事务完成前执行操作。在beforeCommit之后执行
     *
     * @param body  需要执行的操作
     * @param order 优先级。较小的值拥有更高的优先级
     */
    public static void beforeCompletion(final Handler body, final int order) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void beforeCompletion() {
                body.handle();
            }

            @Override
            public int getOrder() {
                return order;
            }
        });
    }

    /**
     * 事务提交后执行操作
     *
     * @param body  需要执行的操作
     * @param order 优先级。较小的值拥有更高的优先级
     */
    public static void afterCommit(final Handler body, final int order) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                body.handle();
            }

            @Override
            public int getOrder() {
                return order;
            }
        });
    }

    /**
     * 事务完成后执行操作
     *
     * @param body  需要执行的操作
     * @param order 优先级。较小的值拥有更高的优先级
     */
    public static void afterCompletion(final IntConsumer body, final int order) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCompletion(int status) {
                body.accept(status);
            }

            @Override
            public int getOrder() {
                return order;
            }
        });
    }
}
