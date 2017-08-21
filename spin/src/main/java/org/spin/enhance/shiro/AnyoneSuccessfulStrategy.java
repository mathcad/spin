package org.spin.enhance.shiro;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.pam.AbstractAuthenticationStrategy;
import org.apache.shiro.realm.Realm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.util.CollectionUtils;

/**
 * Shiro认证成功策略
 * <p>多个Realm中，有任意一个认证成功，即成功</p>
 *
 * @author xuweinan
 */
public class AnyoneSuccessfulStrategy extends AbstractAuthenticationStrategy {
    private static final Logger logger = LoggerFactory.getLogger(AnyoneSuccessfulStrategy.class);

    public AuthenticationInfo afterAllAttempts(AuthenticationToken token, AuthenticationInfo aggregate) throws AuthenticationException {
        if (aggregate == null || CollectionUtils.isEmpty(aggregate.getPrincipals())) {
            throw new AuthenticationException("Authentication token of type [" + token.getClass() + "] " +
                "could not be authenticated by any configured realms.  Please ensure that at least one realm can " +
                "authenticate these tokens.");
        }

        return aggregate;
    }

    public AuthenticationInfo afterAttempt(Realm realm, AuthenticationToken token, AuthenticationInfo info, AuthenticationInfo aggregate, Throwable t)
        throws AuthenticationException {
        if (t != null) {
            if (t instanceof AuthenticationException) {
                throw ((AuthenticationException) t);
            } else {
                String msg = "Unable to acquire account data from realm [" + realm + "].  The [" +
                    getClass().getName() + " implementation requires all configured realm(s) to operate successfully " +
                    "for a successful authentication.";
                throw new AuthenticationException(msg, t);
            }
        }
        if (info == null) {
            String msg = "Realm [" + realm + "] could not find any associated account data for the submitted " +
                "AuthenticationToken [" + token + "].  The [" + getClass().getName() + "] implementation requires " +
                "all configured realm(s) to acquire valid account data for a submitted token during the " +
                "log-in process.";
            throw new UnknownAccountException(msg);
        }
        logger.debug("Account successfully authenticated using realm [{}]", realm);
        merge(info, aggregate);
        return aggregate;
    }
}
