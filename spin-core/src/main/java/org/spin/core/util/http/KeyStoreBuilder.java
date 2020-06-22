package org.spin.core.util.http;

import java.security.KeyStore;
import java.util.Map;
import java.util.Objects;

public class KeyStoreBuilder extends KeyStore.Builder {
    private final KeyStore keyStore;
    private final Map<String, KeyStore.ProtectionParameter> password;

    public KeyStoreBuilder(KeyStore keyStore, Map<String, KeyStore.ProtectionParameter> password) {
        Objects.requireNonNull(keyStore);
        this.keyStore = keyStore;
        this.password = password;
    }

    @Override
    public KeyStore getKeyStore() {
        return keyStore;
    }

    @Override
    public KeyStore.ProtectionParameter getProtectionParameter(String alias) {
        Objects.requireNonNull(alias);
        if (null == password) {
            return null;
        }

        int firstDot = alias.indexOf('.');
        int secondDot = alias.indexOf('.', firstDot + 1);
        if ((firstDot == -1) || (secondDot == firstDot)) {
            return null;
        }
        String keyStoreAlias = alias.substring(secondDot + 1);
        return password.get(keyStoreAlias);
    }
}
