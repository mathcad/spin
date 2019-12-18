package org.spin.data.pk.meta;

/**
 * ID生成方式
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2017/5/5</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public enum IdGenMethodE {

    /**
     * 嵌入发布模式
     */
    EMBED(0),

    /**
     * 中心服务器发布模式
     */
    CENTRAL_SERVICE(1),

    /**
     * REST发布模式
     */
    REST(2);

    private int value;

    IdGenMethodE(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
