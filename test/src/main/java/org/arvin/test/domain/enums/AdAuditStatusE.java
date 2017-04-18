package org.arvin.test.domain.enums;


import org.spin.annotations.UserEnum;

/**
 * 广告审核状态
 *
 * @author xuweinan
 */
@UserEnum("广告审核状态")
public enum AdAuditStatusE {
    未审核(1), 审核通过(2), 审核不通过(3);

    int value;

    AdAuditStatusE(int value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }
}
