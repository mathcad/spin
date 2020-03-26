package org.spin.web.handler;

import org.spin.web.RestfulResponse;

import javax.servlet.http.HttpServletRequest;

/**
 * Web Mvc 全局异常处理器
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/10/17</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public interface WebExceptionHalder {

    /**
     * 处理逻辑
     *
     * @param appName app名称
     * @param e       异常
     * @param request 请求
     * @return RestfulResponse
     */
    RestfulResponse<Void> handler(String appName, Throwable e, HttpServletRequest request);

    /**
     * 是否支持指定的异常
     *
     * @param e 异常
     * @return 是否支持
     */
    boolean support(final Throwable e);

    default int order() {
        return Integer.MAX_VALUE;
    }
}
