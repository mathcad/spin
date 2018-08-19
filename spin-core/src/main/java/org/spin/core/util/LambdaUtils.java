package org.spin.core.util;

import org.spin.core.function.serializable.BiConsumer;
import org.spin.core.function.serializable.Function;
import org.spin.core.throwable.SimplifiedException;

import java.lang.invoke.SerializedLambda;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lambda工具类
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2018/8/19.</p>
 *
 * @author xuweinan
 */
public abstract class LambdaUtils {


    private static final Map<Class, WeakReference<SerializedLambda>> FUNC_CACHE = new ConcurrentHashMap<>();

    private LambdaUtils() {
    }


    /**
     * 从可序列化的lambda中解析出相关信息
     *
     * @param lambda 函数式接口
     * @param <T>    参数类型
     * @return lambda信息
     */
    public static <T> SerializedLambda resolveLambda(Function<T, ?> lambda) {

        Class clazz = lambda.getClass();
        return Optional.ofNullable(FUNC_CACHE.get(clazz))
            .map(WeakReference::get)
            .orElseGet(() -> {
                try {
                    Method writeReplace = lambda.getClass().getDeclaredMethod("writeReplace", null);
                    ReflectionUtils.makeAccessible(writeReplace);
                    writeReplace.setAccessible(true);
                    SerializedLambda lambdaInfo = (SerializedLambda) writeReplace.invoke(lambda);
                    FUNC_CACHE.put(clazz, new WeakReference<>(lambdaInfo));
                    return lambdaInfo;
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    throw new SimplifiedException("SerializedLambda解析失败", e);
                }
            });
    }

    /**
     * 从可序列化的lambda中解析出相关信息
     *
     * @param lambda 函数式接口
     * @return lambda信息
     */
    public static <T, R> SerializedLambda resolveLambda(BiConsumer<T, R> lambda) {

        Class clazz = lambda.getClass();
        return Optional.ofNullable(FUNC_CACHE.get(clazz))
            .map(WeakReference::get)
            .orElseGet(() -> {
                try {
                    Method writeReplace = lambda.getClass().getDeclaredMethod("writeReplace", null);
                    ReflectionUtils.makeAccessible(writeReplace);
                    writeReplace.setAccessible(true);
                    SerializedLambda lambdaInfo = (SerializedLambda) writeReplace.invoke(lambda);
                    FUNC_CACHE.put(clazz, new WeakReference<>(lambdaInfo));
                    return lambdaInfo;
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    throw new SimplifiedException("SerializedLambda解析失败", e);
                }
            });
    }
}
