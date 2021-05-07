package org.spin.cloud.feign;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowException;
import com.alibaba.csp.sentinel.slots.system.SystemBlockException;
import feign.FeignException;
import feign.RetryableException;
import feign.codec.DecodeException;
import feign.codec.EncodeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.cloud.throwable.BizException;
import org.spin.core.ErrorCode;
import org.spin.core.util.BeanUtils;
import org.spin.core.util.ExceptionUtils;
import org.spin.web.throwable.FeignHttpException;

import java.net.UnknownHostException;
import java.util.function.Supplier;

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
    protected String errorMsg;

    /**
     * 是否被降级
     */
    private boolean degrade = false;

    /**
     * 是否被流控
     */
    private boolean flowControl = false;

    /**
     * 是否被参数限流
     */
    private boolean paramFlowControl = false;

    /**
     * 是否被系统保护
     */
    private boolean systemBlock = false;

    /**
     * 是否被访问控制
     */
    private boolean authorityControl = false;

    /**
     * 是否是sentinel熔断
     */
    private boolean blocked = false;

    public AbstractFallback(Throwable cause) {
        cause = cause instanceof DecodeException || cause instanceof EncodeException ?
            (cause.getCause() == null ? cause : cause.getCause()) : cause;


        Throwable ex = ExceptionUtils.getCause(cause,
            BlockException.class,
            FeignException.class);
        this.cause = null == ex ? cause : ex;


    }

    public void prepare(String method) {
        StringBuilder warnMsg = new StringBuilder();
        warnMsg.append("\n-Feign客户端调用异常:  - ").append(method).append("\n|");
        if (cause instanceof DegradeException) {
            degrade = true;
            blocked = true;
            errorMsg = String.format("由于本次请求触发熔断规则, 对资源[%s]的请求已被降级", ((DegradeException) cause).getRuleLimitApp());
        } else if (cause instanceof FlowException) {
            flowControl = true;
            blocked = true;
            errorMsg = String.format("由于本次请求触发流控规则, 对资源[%s]的请求已被拒绝", ((FlowException) cause).getRuleLimitApp());
        } else if (cause instanceof ParamFlowException) {
            paramFlowControl = true;
            blocked = true;
            errorMsg = String.format("由于本次请求触发热点参数限流规则, 对资源[%s]的请求已被拒绝", ((ParamFlowException) cause).getRuleLimitApp());
        } else if (cause instanceof SystemBlockException) {
            systemBlock = true;
            blocked = true;
            errorMsg = String.format("由于本次请求触发系统保护规则, 对资源[%s]的请求已被拒绝", ((SystemBlockException) cause).getRuleLimitApp());
        } else if (cause instanceof AuthorityException) {
            authorityControl = true;
            blocked = true;
            errorMsg = String.format("由于本次请求触发访问控制规则, 对资源[%s]的请求已被拒绝", ((AuthorityException) cause).getRuleLimitApp());
        } else if (cause instanceof BlockException) {
            blocked = true;
            errorMsg = String.format("由于服务调控, 对资源[%s]的请求已被拒绝", ((BlockException) cause).getRuleLimitApp());
        } else if (cause instanceof FeignException) {
            int status = BeanUtils.getFieldValue(cause, "status");
            if (status >= 400) {
                String msg = "远程调用失败: " + ErrorCode.INTERNAL_ERROR.getDesc();
                if (status == 404) {
                    msg = "远程调用失败: 请求的资源不存在";
                } else if (status > 501 && status < 505) {
                    msg = "远程调用失败: 服务暂时不可用";
                } else if (status == 405) {
                    msg = "远程调用失败: 不支持的请求类型";
                }
                warnMsg.append("\n|--").append(msg).append("-").append(cause.getMessage());
                logger.warn(warnMsg.toString(), cause);
                throw new FeignHttpException(status,
                    ((FeignException) cause).hasRequest() ? ((FeignException) cause).request().url() : "",
                    cause.getMessage(), msg, cause);
            } else {
                Throwable e = cause;
                if (e instanceof RetryableException) {
                    logger.warn("请求重试后仍然失败");
                    e = cause.getCause();
                }
                if (e instanceof UnknownHostException) {
                    errorMsg = String.format("网络中找不到的主机名: %s", e.getMessage());
                } else {
                    warnMsg.append("\n|--远程服务调用出错: ").append(e.getMessage());
                    logger.warn(warnMsg.toString(), cause);
                    return;
                }
            }
        } else {
            warnMsg.append("\n|--远程服务调用出错: ").append(cause.getMessage());
            logger.warn(warnMsg.toString(), cause);
            return;
        }


        logger.warn(warnMsg.append("\n|--").append(errorMsg).toString());
    }

    /**
     * 直接抛出异常实现快速失败
     * <pre>
     *     每个熔断方法中, 如果不需要自行处理熔断逻辑, 只需要快速失败, 可以调用该方法
     * </pre>
     *
     * @param <T> 返回数据类型
     * @return 返回数据(实际不可能返回)
     */
    protected <T> T rethrowException() {
        if (null == errorMsg) {
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            } else {
                throw new RuntimeException(cause);
            }
        } else {
            throw new BizException(errorMsg, cause);
        }
    }

    /**
     * 针对Sentinel Breaker的处理逻辑, 如果对应的处理器为空或其他非Sentinel的中断, 抛出异常
     *
     * @param degradeHandler     降级处理
     * @param flowHandler        流控处理
     * @param paramFlowHandler   参数流控处理
     * @param systemBlockHandler 系统保护处理
     * @param authorityHandler   访问控制处理
     * @param <T>                返回数据类型
     * @return 返回数据
     */
    protected <T> T doWhileBlocked(Supplier<T> degradeHandler,
                                   Supplier<T> flowHandler,
                                   Supplier<T> paramFlowHandler,
                                   Supplier<T> systemBlockHandler,
                                   Supplier<T> authorityHandler) {
        if (degrade && null != degradeHandler) {
            return degradeHandler.get();
        } else if (flowControl && null != flowHandler) {
            return flowHandler.get();
        } else if (paramFlowControl && null != paramFlowHandler) {
            return paramFlowHandler.get();
        } else if (systemBlock && null != systemBlockHandler) {
            return systemBlockHandler.get();
        } else if (authorityControl && null != authorityHandler) {
            return authorityHandler.get();
        }

        return rethrowException();
    }

    /**
     * 针对Sentinel Breaker的处理逻辑, 如果对应的处理器为空或其他非Sentinel的中断, 抛出异常
     *
     * @param blockHandler break处理
     * @param <T>          返回数据类型
     * @return 返回数据
     */
    protected <T> T doWhileBlocked(Supplier<T> blockHandler) {
        if (blocked && null != blockHandler) {
            return blockHandler.get();
        } else {
            return rethrowException();
        }
    }

    public boolean isDegrade() {
        return degrade;
    }

    public boolean isFlowControl() {
        return flowControl;
    }

    public boolean isParamFlowControl() {
        return paramFlowControl;
    }

    public boolean isSystemBlock() {
        return systemBlock;
    }

    public boolean isAuthorityControl() {
        return authorityControl;
    }

    public boolean isBlocked() {
        return blocked;
    }
}
