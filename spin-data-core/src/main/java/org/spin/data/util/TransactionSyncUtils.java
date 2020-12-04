package org.spin.data.util;

import org.spin.core.function.BoolConsumer;
import org.spin.core.function.Handler;
import org.springframework.lang.Nullable;
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
     * 返回当前事务的隔离级别（如果有的话）。当准备一个新建的资源（例如 JDBC 连接）时由资源管理代码调用
     *
     * @return 当前公开的隔离级别，源于JDBC连接常量（等价于相应的TransactionDefinition常量），如果没有，则为 null
     * @see java.sql.Connection#TRANSACTION_READ_UNCOMMITTED
     * @see java.sql.Connection#TRANSACTION_READ_COMMITTED
     * @see java.sql.Connection#TRANSACTION_REPEATABLE_READ
     * @see java.sql.Connection#TRANSACTION_SERIALIZABLE
     * @see org.springframework.transaction.TransactionDefinition#ISOLATION_READ_UNCOMMITTED
     * @see org.springframework.transaction.TransactionDefinition#ISOLATION_READ_COMMITTED
     * @see org.springframework.transaction.TransactionDefinition#ISOLATION_REPEATABLE_READ
     * @see org.springframework.transaction.TransactionDefinition#ISOLATION_SERIALIZABLE
     * @see org.springframework.transaction.TransactionDefinition#getIsolationLevel()
     */
    @Nullable
    public static Integer getCurrentTransactionIsolationLevel() {
        return TransactionSynchronizationManager.getCurrentTransactionIsolationLevel();
    }

    /**
     * 返回当前是否有实际事务处于活动状态。这里指当前线程此时是否与实际事务关联，而不仅仅是与活动事务同步关联。
     *
     * @return 是否存在活动事务
     */
    public static boolean isActualTransactionActive() {
        return TransactionSynchronizationManager.isActualTransactionActive();
    }

    /**
     * 事务提交前执行操作
     *
     * @param body  需要执行的操作, 当没有活动事务时，立刻原地执行
     * @param order 优先级。较小的值拥有更高的优先级
     */
    public static void beforeCommit(final BoolConsumer body, final int order) {
        if (isActualTransactionActive()) {
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
        } else {
            body.accept(false);
        }
    }

    /**
     * 事务完成前执行操作。在beforeCommit之后执行
     *
     * @param body  需要执行的操作, 当没有活动事务时，立刻原地执行
     * @param order 优先级。较小的值拥有更高的优先级
     */
    public static void beforeCompletion(final Handler body, final int order) {
        if (isActualTransactionActive()) {
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
        } else {
            body.handle();
        }
    }

    /**
     * 事务提交后执行操作
     *
     * @param body  需要执行的操作, 当没有活动事务时，立刻原地执行
     * @param order 优先级。较小的值拥有更高的优先级
     */
    public static void afterCommit(final Handler body, final int order) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
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
        } else {
            body.handle();
        }
    }

    /**
     * 事务完成后执行操作
     *
     * @param body  需要执行的操作, 当没有活动事务时，立刻原地执行
     * @param order 优先级。较小的值拥有更高的优先级
     */
    public static void afterCompletion(final IntConsumer body, final int order) {
        if (isActualTransactionActive()) {
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
        } else {
            body.accept(2);
        }
    }
}
