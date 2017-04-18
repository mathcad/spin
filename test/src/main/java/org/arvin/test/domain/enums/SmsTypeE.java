package org.arvin.test.domain.enums;

import org.spin.annotations.UserEnum;

/**
 * 短信类型
 *
 * @author xuweinan
 */
@UserEnum("短信类型")
public enum SmsTypeE {
    未知(0), 注册(1), 修改密码(2), 重置交易密码(4), 验证手机号(5), 修改手机号(6), 绑定微信用户(7);

    int value;

    SmsTypeE(int value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }
}
