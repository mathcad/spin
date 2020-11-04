package org.spin.core.security;

import org.spin.core.trait.Evaluatable;

/**
 * 对称加密算法的工作模式，未引用其他Security Provider的情况下只能使用ECB模式(默认ECB)
 * <p>Created by xuweinan on 2016/8/15.</p>
 *
 * @author xuweinan
 * @version 1.1
 */
public enum Mode implements Evaluatable<String> {

    /**
     * 电子密码本模式：Electronic codebook
     */
    ECB("ECB", false),

    /**
     * 密码分组链接：Cipher-block chaining
     */
    CBC("CBC", true),

    /**
     * 填充密码块链接：Propagating Cipher-block chaining
     */
    PCBC("PCBC", true),

    /**
     * 密文反馈:Cipher feedback
     */
    CFB("CFB", true),

    /**
     * 输出反馈：Output feedback
     */
    OFB("OFB", true),

    /**
     * 计数器模式：Counter
     */
    CTR("CTR", true),

    /**
     * Galois/Counter Mode
     */
    GCM("GCM", true);

    private final String value;
    private final boolean needIv;

    Mode(String value, boolean needIv) {
        this.value = value;
        this.needIv = needIv;
    }

    @Override
    public String getValue() {
        return this.value;
    }

    public boolean isNeedIv() {
        return needIv;
    }
}
