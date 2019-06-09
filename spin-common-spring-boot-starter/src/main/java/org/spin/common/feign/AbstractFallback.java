package org.spin.common.feign;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.netflix.client.ClientException;
import feign.FeignException;
import feign.codec.DecodeException;
import feign.codec.EncodeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.common.throwable.BizException;
import org.spin.common.throwable.FeignHttpException;
import org.spin.core.ErrorCode;
import org.spin.core.throwable.SimplifiedException;
import org.spin.core.util.BeanUtils;
import org.spin.core.util.ExceptionUtils;

/**
 * 断路器抽象类
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/3/19</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public abstract class AbstractFallback {
    protected static final Logger logger = LoggerFactory.getLogger(AbstractFallback.class);
    protected final Throwable cause;

    public AbstractFallback(Throwable cause) {
        this.cause = cause instanceof DecodeException || cause instanceof EncodeException ? cause.getCause() : cause;
    }

    /**
     * 已知异常处理
     * <pre>
     *     每个熔断方法中都必须首先调用该方法
     * </pre>
     */
    protected void handleKnownException() {
        logger.warn("-Feign客户端调用异常: {}", Thread.currentThread().getStackTrace()[2]);
        logger.warn("|");

        Throwable ex = ExceptionUtils.getCause(this.cause, SimplifiedException.class,
            DegradeException.class,
            FlowException.class,
            BlockException.class,
            ClientException.class,
            FeignException.class);

        if (ex instanceof SimplifiedException) {
            logger.warn("|--远程调用出现业务异常[{}]", ex.getMessage());
            throw (SimplifiedException) ex;
        }

        if (ex instanceof DegradeException) {
            logger.warn("|--由于服务调控, 对资源[{}]的请求已被降级", ((DegradeException) ex).getRuleLimitApp());
            throw new BizException("由于服务调控, 本次请求已被降级");
        }

        if (ex instanceof FlowException) {
            logger.warn("|--由于流量控制, 对资源[{}]的请求已被拒绝", ((FlowException) ex).getRuleLimitApp());
            throw new BizException("由于服务流量控制, 本次请求已被拒绝");
        }

        if (ex instanceof BlockException) {
            logger.warn("|--由于服务调控, 对资源[{}]的请求已被拒绝", ((BlockException) ex).getRuleLimitApp());
            throw new BizException("由于服务调控, 本次请求已被拒绝");
        }

        if (ex instanceof ClientException) {
            logger.warn("|--远程调用异常: 错误类型[{}]-{}-{}", ((ClientException) ex).getErrorType(), ((ClientException) ex).getErrorCode(), ((ClientException) ex).getErrorMessage());
            throw new BizException("远程调用失败");
        }

        if (ex instanceof FeignException) {
            int status = BeanUtils.getFieldValue(ex, "status");
            if (status >= 400) {
                String msg = "远程调用失败: " + ErrorCode.INTERNAL_ERROR.getDesc();
                if (status == 404) {
                    msg = "远程调用失败: 请求的资源不存在";
                } else if (status > 501 && status < 505) {
                    msg = "远程调用失败: 服务暂时不可用";
                } else if (status == 405) {
                    msg = "远程调用失败: 不支持的请求类型";
                }
                logger.warn("|--{}-{}", msg, ex.getMessage());
                throw new FeignHttpException(status, ex.getMessage(), msg, ex);
            }
        }

        logger.warn("|--接口调用出错: ", this.cause);
    }
}
