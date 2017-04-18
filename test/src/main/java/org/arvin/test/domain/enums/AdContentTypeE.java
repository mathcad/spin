package org.arvin.test.domain.enums;


import org.spin.annotations.UserEnum;

/**
 * 广告内容类型
 *
 * @author xuweinan
 */
@UserEnum("广告内容类型")
public enum AdContentTypeE {
    图片(1), 文本(2);

    int value;

    AdContentTypeE(int value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }
}
