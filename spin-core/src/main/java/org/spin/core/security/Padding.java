package org.spin.core.security;

import org.spin.core.trait.Evaluatable;

/**
 * 分组加密算法的填充方式
 * <p>Created by xuweinan on 2016/8/15.</p>
 *
 * @author xuweinan
 * @version 1.1
 */
public enum Padding implements Evaluatable<String> {

    /**
     * NoPadding
     */
    NoPadding("NoPadding"),

    /**
     * ISO10126Padding
     */
    ISO10126Padding("ISO10126Padding"),

    /**
     * PKCS5Padding
     */
    PKCS5Padding("PKCS5Padding"),

    /**
     * PKCS7Padding
     */
    PKCS7Padding("PKCS7Padding");

    private final String value;

    Padding(String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return this.value;
    }
}
