package org.spin.core.session;

import org.spin.core.Assert;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.function.Supplier;

/**
 * 在线用户接口
 * <p>
 * 用户实体应实现此接口,并且保证可以被正确地序列化<br>
 * 用户应该将自定义的SessionUser实现的提供者注册给SessionUser类
 * </p>
 *
 * @author xuweinan
 * @version 1.0
 * @see Serializable
 */
public abstract class SessionUser<PK extends Serializable> implements Serializable {
    private static Supplier<? extends SessionUser<? extends Serializable>> userSupplier = null;

    public static <PK extends Serializable> void registerSupplier(Supplier<? extends SessionUser<PK>> supplier) {
        userSupplier = supplier;
    }

    /**
     * 获取当前线程上绑定的用户
     *
     * @param <PK> id类型参数
     * @param <S>  SessionUser类型参数
     * @return 当前用户
     */
    @SuppressWarnings("unchecked")
    public static <PK extends Serializable, S extends SessionUser<PK>> S getCurrent() {
        if (null == userSupplier) {
            return null;
        } else {
            return (S) userSupplier.get();
        }
    }

    /**
     * 获取当前线程上绑定的用户, 如果不存在, 抛出异常
     *
     * @param <PK> id类型参数
     * @param <S>  SessionUser类型参数
     * @param msg  当前用户不存在时的出错提示,默认为"当前用户没有登录"
     * @return 当前用户
     */
    public static <PK extends Serializable, S extends SessionUser<PK>> S getCurrentNonNull(String... msg) {
        String nonMsg = msg == null || msg.length == 0 ? "当前用户没有登录" : msg[0];
        return Assert.notNull(SessionUser.<PK, S>getCurrent(), nonMsg);
    }

    /**
     * 获取当前用户ID
     *
     * @return 用户ID
     */
    public abstract PK getId();

    /**
     * 获取当前用户名称
     *
     * @return 用户名称
     */
    public abstract String getName();

    /**
     * 获取当前用户登录时间
     *
     * @return 登录时间
     */
    public abstract LocalDateTime getLoginTime();

    /**
     * 获取当前的Session ID
     *
     * @return Session ID
     */
    public abstract String getSessionId();

    /**
     * 获取当前用户登录IP
     *
     * @return 登录IP
     */
    public abstract String getLoginIp();
}
