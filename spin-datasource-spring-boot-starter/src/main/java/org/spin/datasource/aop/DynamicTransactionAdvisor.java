package org.spin.datasource.aop;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.spin.core.util.StringUtils;
import org.spin.datasource.tx.ConnectionFactory;
import org.spin.datasource.tx.TransactionContext;
import org.springframework.lang.NonNull;

import java.util.UUID;

/**
 * @author funkye
 */
public class DynamicTransactionAdvisor implements MethodInterceptor {

    @Override
    public Object invoke(@NonNull MethodInvocation methodInvocation) throws Throwable {
        if (StringUtils.isNotEmpty(TransactionContext.getXID())) {
            return methodInvocation.proceed();
        }
        boolean state = true;
        Object o;
        String xid = UUID.randomUUID().toString();
        TransactionContext.bind(xid);
        try {
            o = methodInvocation.proceed();
        } catch (Exception e) {
            state = false;
            throw e;
        } finally {
            ConnectionFactory.notify(state);
            TransactionContext.remove();
        }
        return o;
    }
}
