package org.spin.cloud.idempotent;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.cloud.annotation.Idempotent;
import org.spin.cloud.throwable.BizException;
import org.spin.cloud.util.CloudInfrasContext;
import org.spin.cloud.util.Env;
import org.spin.cloud.vo.CurrentUser;
import org.spin.core.concurrent.DistributedLock;
import org.spin.core.util.DateUtils;
import org.spin.core.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 幂等接口代理切面
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2020/7/13</p>
 *
 * @author xuweinan
 */
@Aspect
@Component
public class IdempotentAspect implements Ordered {
    private static final Logger logger = LoggerFactory.getLogger(IdempotentAspect.class);
    private static final String IDENPOTENT_RESULT_CACHE_KEY = "IDENPOTENT_RESULT_CACHE:";

    public static final String IDEMPOTENT_ID = "X-Request-Id";

    private final DistributedLock distributedLock;
    private final RedisTemplate<Object, Object> redisTemplate;
    private final boolean ready;

    private final SpelExpressionParser parser = new SpelExpressionParser();
    private final DefaultParameterNameDiscoverer nameDiscoverer = new DefaultParameterNameDiscoverer();

    private long guaranteeTime = 180_000L;

    public IdempotentAspect(@Autowired(required = false) DistributedLock distributedLock,
                            @Autowired(required = false) RedisTemplate<Object, Object> redisTemplate) {
        this.distributedLock = distributedLock;
        this.redisTemplate = redisTemplate;
        ready = null != distributedLock && null != redisTemplate;
    }

    @Pointcut("@annotation(org.spin.cloud.annotation.Idempotent)")
    private void idenpotentMethod() {
    }

    @Around("idenpotentMethod()")
    public Object idenpotentProxy(ProceedingJoinPoint joinPoint) throws Throwable {
        IdempotentResult cache;

        Method apiMethod = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Idempotent idempotent = apiMethod.getAnnotation(Idempotent.class);
        String idempotentId = extractIdempotentId(joinPoint);
        if (StringUtils.isNotEmpty(idempotentId) && ready) {
            // 幂等处理
            String key = IDENPOTENT_RESULT_CACHE_KEY + Env.getAppName() + ":" + joinPoint.getSignature().toShortString() + "-" + idempotentId;
            logger.info("开始为接口 [{}] 进行幂等担保处理, 本次业务id: {}", joinPoint.getSignature().toLongString(), key);


            try {
                boolean locked = (idempotent.reentrantable() ? distributedLock.lock(key, guaranteeTime, (int) guaranteeTime / 200, 200L)
                    : distributedLock.lock(key, 100, 0, 0L));
                if (locked) {
                    cache = (IdempotentResult) redisTemplate.opsForValue().get(key);
                    if (null == cache) {
                        cache = doActualInvoke(joinPoint);
                        redisTemplate.opsForValue().set(key, cache, guaranteeTime, TimeUnit.MILLISECONDS);
                    } else {
                        if (Objects.equals(cache.getSignature(), joinPoint.getSignature().toLongString())) {
                            logger.info("当前请求 [{}] 被多次重试, 使用缓存结果确保幂等, 本次业务id: {}", cache.getSignature(), key);
                        } else {
                            logger.error("当前请求 [{}] 上的业务id与 {} 重复, 担保失败. 即将进行物理操作 本次业务id: {}",
                                joinPoint.getSignature().toLongString(), cache.getSignature(), key);
                            cache = doActualInvoke(joinPoint);
                        }
                    }
                } else {
                    if (idempotent.reentrantable()) {
                        logger.error("幂等担保代理无法获取[ {} ]上的分布式锁, 担保失败. 即将进行物理操作. 本次业务id: {}", joinPoint.getSignature().toLongString(), key);
                        cache = doActualInvoke(joinPoint);
                        redisTemplate.opsForValue().set(key, cache, guaranteeTime, TimeUnit.MILLISECONDS);
                    } else {
                        throw new BizException(idempotent.errorMessage());
                    }
                }
            } finally {
                distributedLock.releaseLock(key);
            }
        } else {
            if (ready) {
                logger.warn("当前请求中没有请求ID, 跳过幂等担保代理逻辑: {}", joinPoint.getSignature().toLongString());
            } else {
                logger.warn("系统未配置Redis, 无法启用幂等担保代理: {}", joinPoint.getSignature().toLongString());
            }
            cache = doActualInvoke(joinPoint);
        }

        if (cache.isSuccess()) {
            return cache.getResult();
        } else {
            throw cache.getException();
        }
    }

    public void setGuaranteeTime(String guaranteeTime) {
        this.guaranteeTime = DateUtils.periodToMs(guaranteeTime);
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    private String extractIdempotentId(ProceedingJoinPoint joinPoint) {
        String idempotentId;

        Method apiMethod = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Idempotent idempotent = apiMethod.getAnnotation(Idempotent.class);
        idempotentId = idempotent.value();
        if (StringUtils.isNotEmpty(idempotentId)) {
            idempotentId = parseSpel(idempotentId, apiMethod, joinPoint.getArgs());
            CloudInfrasContext.setIdempotentInfo(idempotentId);
        }
        if (StringUtils.isEmpty(idempotentId)) {
            idempotentId = CloudInfrasContext.getIdempotentInfo();
        }
        return idempotentId;
    }

    private String parseSpel(String spel, Method method, Object[] args) {
        Expression expression = parser.parseExpression(spel);
        EvaluationContext context = new StandardEvaluationContext();
        String[] paramNames = nameDiscoverer.getParameterNames(method);
        if (null != CurrentUser.getCurrent()) {
            context.setVariable("myself", CurrentUser.getCurrent());
        }
        if (null != args && null != paramNames && paramNames.length == args.length) {
            for (int i = 0; i < args.length; i++) {
                context.setVariable(paramNames[i], args[i]);
            }
        }
        return StringUtils.toString(expression.getValue(context));
    }

    private IdempotentResult doActualInvoke(ProceedingJoinPoint joinPoint) {
        IdempotentResult cache = new IdempotentResult();
        cache.setSignature(joinPoint.getSignature().toLongString());
        try {
            Object result = joinPoint.proceed();
            if (result != null && !(result instanceof Serializable)) {
                throw new BizException(String.format("幂等接口[ %s ]的返回值必须可以被序列化, 实际类型: %s",
                    joinPoint.getSignature().toLongString(),
                    result.getClass().getName()));
            }
            cache.setResult((Serializable) result);
            cache.setSuccess(true);
        } catch (Throwable throwable) {
            // 缓存异常信息
            cache.setException(throwable);
            cache.setSuccess(false);
        }
        return cache;
    }
}
