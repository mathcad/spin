package org.spin.datasource.toolkit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.Assert;
import org.spin.core.throwable.SimplifiedException;
import org.spin.datasource.CurrentDatasourceInfo;
import org.spin.datasource.Ds;
import org.springframework.core.NamedThreadLocal;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * 核心基于ThreadLocal的切换数据源工具类
 *
 * @author TaoYu Kanyuxia
 * @since 1.0.0
 */
public final class DynamicDataSourceContextHolder {
    private static final Logger logger = LoggerFactory.getLogger(DynamicDataSourceContextHolder.class);

    private static DynamicDatasourceHandler interceptor;

    /**
     * 为什么要用链表存储(准确的是栈)
     * <pre>
     * 为了支持嵌套切换，如ABC三个service都是不同的数据源
     * 其中A的某个业务要调B的方法，B的方法需要调用C的方法。一级一级调用切换，形成了链。
     * 传统的只设置当前线程的方式不能满足此业务需求，必须使用栈，后进先出。
     * </pre>
     */
    private static final ThreadLocal<Deque<CurrentDatasourceInfo>> LOOKUP_KEY_HOLDER = new NamedThreadLocal<Deque<CurrentDatasourceInfo>>("dynamic-datasource") {
        @Override
        protected Deque<CurrentDatasourceInfo> initialValue() {
            return new ArrayDeque<>();
        }
    };

    private DynamicDataSourceContextHolder() {
    }

    /**
     * 获得当前线程数据源
     *
     * @return 数据源名称
     */
    public static CurrentDatasourceInfo peek() {
        return LOOKUP_KEY_HOLDER.get().peek();
    }

    /**
     * 设置当前线程数据源
     * <p>
     * 如非必要不要手动调用，调用后确保最终清除
     * </p>
     *
     * @param ds 数据源名称
     */
    public static void push(String ds) {
        push(new CurrentDatasourceInfo(Assert.notEmpty(ds, "数据源名称不能为空")));
    }

    /**
     * 设置当前线程数据源
     * <p>
     * 如非必要不要手动调用，调用后确保最终清除
     * </p>
     *
     * @param ds 数据源名称
     */
    public static void push(CurrentDatasourceInfo ds) {
        CurrentDatasourceInfo peek = peek();
        if (null == peek) {
            peek = new CurrentDatasourceInfo(Ds.getPrimaryDataSource(), Ds.getDefaultCatalog(Ds.getPrimaryDataSource()));
        }

        LOOKUP_KEY_HOLDER.get().push(Assert.notNull(ds, "数据源不能为空"));

        if (null != interceptor) {
            try {
                boolean succ = interceptor.handle(peek, Assert.notNull(ds, "数据源不能为空"));
                if (!succ) {
                    throw new SimplifiedException("当前上下文不支持切换数据源");
                }
            } catch (Exception e) {
                logger.error("切换数据源拦截器 [{} -> {}] 处理失败: {}", peek, ds, e.getMessage());
                LOOKUP_KEY_HOLDER.get().poll();
                throw e;
            }
        } else {
            if (TransactionSynchronizationManager.isActualTransactionActive() && !peek.getDatasource().equals(ds.getDatasource())) {
                // 在Spring的JDBC事务上下文中切换数据源会破坏事务一致性
                logger.warn("*** Current DataSource [{}] is CHANGING to [{}] in a JDBC Transaction, It would not be EFFECTIVE or Consistency might be broken!!! ***",
                    peek, ds);
            }
        }

        logger.info("**************** 数据源已切换至 {} ****************", ds);
    }

    /**
     * 清空当前线程数据源
     * <p>
     * 如果当前线程是连续切换数据源 只会移除掉当前线程的数据源名称
     * </p>
     */
    public static void poll() {
        Deque<CurrentDatasourceInfo> deque = LOOKUP_KEY_HOLDER.get();
        CurrentDatasourceInfo current = deque.poll();
        CurrentDatasourceInfo peek = deque.peek();
        if (null == peek) {
            peek = new CurrentDatasourceInfo(Ds.getPrimaryDataSource(), Ds.getDefaultCatalog(Ds.getPrimaryDataSource()));
        }

        if (null != interceptor) {
            try {
                boolean succ = interceptor.handle(current, peek);
                if (!succ) {
                    throw new SimplifiedException("当前上下文不支持切换数据源");
                }
            } catch (Exception e) {
                logger.error("切换数据源拦截器 [{} -> {}] 处理失败: {}", current, peek, e.getMessage());
                LOOKUP_KEY_HOLDER.get().push(current);
                throw e;
            }
        }

        logger.info("**************** 数据源已切回至 {} ****************", peek.toString());
        if (deque.isEmpty()) {
            LOOKUP_KEY_HOLDER.remove();
        }
    }

    /**
     * 强制清空本地线程
     * <p>
     * 防止内存泄漏，如手动调用了push可调用此方法确保清除
     * </p>
     */
    public static void clear() {
        LOOKUP_KEY_HOLDER.remove();
    }

    public static void registerInterceptor(DynamicDatasourceHandler interceptor) {
        DynamicDataSourceContextHolder.interceptor = interceptor;
    }
}
