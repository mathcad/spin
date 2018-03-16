package org.spin.data.extend;

import org.hibernate.engine.transaction.jta.platform.internal.AbstractJtaPlatform;
import org.springframework.transaction.jta.JtaTransactionManager;
import org.springframework.util.Assert;

import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

/**
 * Spring与Hibernate的JTA事务适配器
 * <p>Created by xuweinan on 2017/12/2.</p>
 *
 * @author xuweinan
 */
public class SpringJtaPlatformAdapter extends AbstractJtaPlatform {
    private static final long serialVersionUID = 94651604466519290L;
    private static TransactionManager transactionManager;
    private static UserTransaction userTransaction;

    @Override
    protected TransactionManager locateTransactionManager() {
        Assert.notNull(transactionManager, "TransactionManager has not been setted");
        return transactionManager;
    }

    @Override
    protected UserTransaction locateUserTransaction() {
        Assert.notNull(userTransaction, "UserTransaction has not been setted");
        return userTransaction;
    }

    public static void setJtaTransactionManager(JtaTransactionManager jtaTransactionManager) {
        transactionManager = jtaTransactionManager.getTransactionManager();
        userTransaction = jtaTransactionManager.getUserTransaction();
    }

    public static void setTransactionManager(TransactionManager transactionManager) {
        SpringJtaPlatformAdapter.transactionManager = transactionManager;
    }

    public static void setUserTransaction(UserTransaction userTransaction) {
        SpringJtaPlatformAdapter.userTransaction = userTransaction;
    }
}
