package org.spin.core.security;

import org.spin.core.util.ClassUtils;

import java.lang.reflect.Constructor;
import java.security.Provider;
import java.security.Security;

/**
 * Security Provider的侦测类
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/2/12</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class ProviderDetector {
    private static Provider provider;

    static {
        try {
            //noinspection unchecked
            Constructor<Provider> providerConstructor = (Constructor<Provider>) ClassUtils.getClass("org.bouncycastle.jce.provider.BouncyCastleProvider").getDeclaredConstructor();
            provider = providerConstructor.newInstance();
            Security.addProvider(provider);
        } catch (Exception ignore) {
            // no bouncyCastle provider
        }
    }

    public static Provider getProvider() {
        return provider;
    }

    public static void setProvider(Provider provider) {
        ProviderDetector.provider = provider;
    }
}
