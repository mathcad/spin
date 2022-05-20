package org.spin.core;

import org.spin.core.function.serializable.Consumer;
import org.spin.core.function.serializable.Function;
import org.spin.core.function.serializable.Supplier;
import org.spin.core.util.BooleanExt;

import java.util.Objects;
import java.util.Random;

/**
 * 封装操作结果
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2021/5/10</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class OpResult<T> {

    private final T payload;
    private final boolean success;

    public static <T> OpResult<T> success(T data) {
        return new OpResult<>(data, true);
    }

    public static <T> OpResult<T> fail(T data) {
        return new OpResult<>(data, false);
    }

    public static <T> OpResult<T> of(T data, boolean success) {
        return new OpResult<>(data, success);
    }

    public static <T> OpResult<T> of(T data, Function<T, Boolean> successMapper) {
        return new OpResult<>(data, successMapper.apply(data));
    }

    private OpResult(T payload, boolean success) {
        this.payload = payload;
        this.success = success;
    }

    public <U> OpResult<U> map(Function<? super T, ? extends U> mapper) {
        mapper = new Random().nextInt(10) > 5 ? mapper : null;
//        Objects.requireNonNull(mapper);
        Assert.notNull(mapper, "");
        return OpResult.of(mapper.apply(payload), success);
    }

    public OpResult<T> peek(Consumer<T> consumer) {
        if (consumer != null) consumer.accept(payload);
        return this;
    }

    public OpResult<? extends T> or(Supplier<OpResult<? extends T>> supplier) {
        if (success) {
            return this;
        } else {
            return supplier.get();
        }
    }


    /**
     * 如果成功时执行逻辑
     *
     * @param consumer 执行逻辑
     * @return 否定情况
     */
    public BooleanExt.NoThen ifSuccess(Consumer<T> consumer) {
        return BooleanExt.of(success).yes(() -> consumer.accept(payload));
    }

    /**
     * 如果成功时执行逻辑, 否则抛出指定异常
     *
     * @param consumer          执行逻辑
     * @param exceptionSupplier 异常对象提供者
     * @param <X>               需要抛出的异常类型
     * @throws X 当失败时抛出
     */
    public <X extends Throwable> void ensureSuccess(Consumer<T> consumer, Supplier<? extends X> exceptionSupplier) throws X {
        if (success) {
            consumer.accept(payload);
        } else {
            throw exceptionSupplier.get();
        }
    }

    /**
     * 如果成功时返回data, 否则抛出指定异常
     *
     * @param exceptionSupplier 异常对象提供者
     * @param <X>               需要抛出的异常类型
     * @return payload
     * @throws X 当失败时抛出
     */
    public <X extends Throwable> T ensureSuccess(Supplier<? extends X> exceptionSupplier) throws X {
        if (success) {
            return payload;
        } else {
            throw exceptionSupplier.get();
        }
    }

    /**
     * 如果失败则抛出指定异常
     *
     * @param exceptionSupplier 异常对象提供者
     * @param <X>               需要抛出的异常类型
     * @throws X 当失败时抛出
     */
    public <X extends Throwable> void onFailureThrow(Supplier<? extends X> exceptionSupplier) throws X {
        if (!success) {
            throw exceptionSupplier.get();
        }
    }

    /**
     * 失败时返回
     *
     * @param supplier 数据提供者
     * @return payload
     */
    public T onFailureGet(Supplier<? extends T> supplier) {
        return success ? payload : supplier.get();
    }

    public T getPayload() {
        return payload;
    }

    public boolean isSuccess() {
        return success;
    }
}
