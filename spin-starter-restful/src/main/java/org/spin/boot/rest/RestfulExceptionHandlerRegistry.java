package org.spin.boot.rest;

import org.spin.boot.converter.RestfulExceptionHandler;
import org.spin.core.util.ClassUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

/**
 * Restful请求执行异常处理器
 * <p>Created by xuweinan on 2017/9/18.</p>
 *
 * @author xuweinan
 */
public class RestfulExceptionHandlerRegistry {
    private static final List<RestfulExceptionHandler> handlers = new LinkedList<>();

    private RestfulExceptionHandlerRegistry() {
    }

    /**
     * 注册异常处理器
     *
     * @param exceptionCls 异常类型
     * @param handler      处理逻辑
     * @param order        优先级
     * @param <T>          异常类型参数
     */
    public static <T extends Throwable> void register(Class<T> exceptionCls, Function<Throwable, String> handler, int order) {
        RestfulExceptionHandler h = new RestfulExceptionHandler() {
            @Override
            public Class<T> getExceptionCls() {
                return exceptionCls;
            }

            @Override
            public String handler(Throwable throwable) {
                return handler.apply(throwable);
            }

            @Override
            public int getOrder() {
                return order;
            }
        };

        register(h);
    }

    public static void register(RestfulExceptionHandler handler) {
        synchronized (handlers) {
            handlers.removeIf(tmp -> ClassUtils.equal(tmp.getExceptionCls(), handler.getExceptionCls()));
            handlers.add(handler);
            handlers.sort(Comparator.comparingInt(RestfulExceptionHandler::getOrder));
        }
    }

    /**
     * 获取指定异常的处理器
     *
     * @param exceptionCls 异常类型
     * @return 处理器
     */
    public static RestfulExceptionHandler getHandler(Class<? extends Throwable> exceptionCls) {
        RestfulExceptionHandler accuracy = null;
        List<RestfulExceptionHandler> matchs = new ArrayList<>(handlers.size());
        for (RestfulExceptionHandler handler : handlers) {
            if (ClassUtils.equal(handler.getExceptionCls(), exceptionCls)) {
                accuracy = handler;
                break;
            } else if (handler.getExceptionCls().isAssignableFrom(exceptionCls)){
                matchs.add(handler);
            }
        }
        return null != accuracy ? accuracy : (matchs.isEmpty() ? null : matchs.get(0));
    }
}
