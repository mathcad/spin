package org.spin.core.util.http;

/**
 * Keystore类型
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2020/6/10</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public enum KeyStoreType {
    JCEKS("JCEKS"),
    JKS("JKS"),
    DKS("DKS"),
    PKCS11("PKCS11"),
    PKCS12("PKCS12"),
    WINDOWS_MY("Windows-MY"),
    BKS("BKS");

    private final String value;

    KeyStoreType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
