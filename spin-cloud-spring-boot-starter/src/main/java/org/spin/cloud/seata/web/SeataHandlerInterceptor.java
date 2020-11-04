package org.spin.cloud.seata.web;

import io.seata.core.context.RootContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author xiaojing
 * <p>
 * Seata HandlerInterceptor, Convert Seata information into
 * @see io.seata.core.context.RootContext from http request's header in
 * {@link org.springframework.web.servlet.HandlerInterceptor#preHandle(HttpServletRequest, HttpServletResponse, Object)},
 * And clean up Seata information after servlet method invocation in
 * {@link org.springframework.web.servlet.HandlerInterceptor#afterCompletion(HttpServletRequest, HttpServletResponse, Object, Exception)}
 */
public class SeataHandlerInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(SeataHandlerInterceptor.class);

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                             @NonNull Object handler) {

        String xid = RootContext.getXID();
        String rpcXid = request.getHeader(RootContext.KEY_XID);
        if (logger.isDebugEnabled()) {
            logger.debug("xid in RootContext {} xid in RpcContext {}", xid, rpcXid);
        }

        if (xid == null && rpcXid != null) {
            RootContext.bind(rpcXid);
            if (logger.isDebugEnabled()) {
                logger.debug("bind {} to RootContext", rpcXid);
            }
        }
        return true;
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                @NonNull Object handler, Exception e) {
        if (RootContext.inGlobalTransaction()) {
            String rpcXid = request.getHeader(RootContext.KEY_XID);

            if (StringUtils.isEmpty(rpcXid)) {
                return;
            }

            String unbindXid = RootContext.unbind();
            if (logger.isDebugEnabled()) {
                logger.debug("unbind {} from RootContext", unbindXid);
            }
            if (!rpcXid.equalsIgnoreCase(unbindXid)) {
                logger.warn("xid in change during RPC from {} to {}", rpcXid, unbindXid);
                if (unbindXid != null) {
                    RootContext.bind(unbindXid);
                    logger.warn("bind {} back to RootContext", unbindXid);
                }
            }
        }
    }

}
