package org.spin.sys;

/**
 * Token关联信息实体
 * <p>
 * Created by xuweinan on 2016/10/3.
 *
 * @author xuweinan
 */
public class TokenInfo {
    private Long generateTime = System.currentTimeMillis();
    private String userId;

    public TokenInfo(String userId) {
        this.userId = userId;
    }

    public Long getGenerateTime() {
        return generateTime;
    }

    public String getUserId() {
        return userId;
    }
}