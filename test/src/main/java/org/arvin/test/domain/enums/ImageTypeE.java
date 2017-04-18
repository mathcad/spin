package org.arvin.test.domain.enums;

import org.spin.annotations.UserEnum;

/**
 * 图片类型
 *
 * @author xuweinan
 */
@UserEnum("图片类型")
public enum ImageTypeE {
    未知(1), 广告内容图(2), 广告封面图(3), 头像(4), 轮播图(5), 模板封面图(6);

    int value;

    ImageTypeE(int value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }
}
