package org.arvin.test.domain.enums;

import org.spin.annotations.UserEnum;

/**
 * 广告类型
 * Created by xuweinan on 2017/1/15.
 *
 * @author xuweinan
 */
@UserEnum("广告类型")
public enum AdTypeE {
    用户广告(1), 后台广告(2), 轮播广告(3);

    int value;

    AdTypeE(int value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }
}
