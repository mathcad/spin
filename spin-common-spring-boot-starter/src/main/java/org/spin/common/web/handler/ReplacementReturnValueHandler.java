package org.spin.common.web.handler;

import org.springframework.core.Ordered;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;

/**
 * 可替换其他处理器的请求返回结果处理器
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/3/27</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public interface ReplacementReturnValueHandler<T extends HandlerMethodReturnValueHandler> extends HandlerMethodReturnValueHandler, Ordered {

    /**
     * 声明需要替换的结果处理器类，必须完全匹配
     *
     * @return 需要替换的结果处理器类
     */
    Class<T> replace();
}
