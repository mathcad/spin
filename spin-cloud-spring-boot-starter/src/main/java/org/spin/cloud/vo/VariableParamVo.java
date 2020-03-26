package org.spin.cloud.vo;

import org.spin.core.Assert;
import org.spin.core.util.StringUtils;

import java.util.HashMap;

/**
 * 变量短信接收者参数VO
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2020/3/5</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class VariableParamVo extends HashMap<String, Object> {

    public static VariableParamVo aParam(String phone) {
        return new VariableParamVo().withPhone(phone);
    }

    public VariableParamVo withPhone(String phone) {
        this.setPhone(phone);
        return this;
    }

    public VariableParamVo withParam(String key, String value) {
        this.put(Assert.notEmpty(key, "参数名称不能为空"), value);
        return this;
    }

    public String getPhone() {
        return StringUtils.toString(get("phone"));
    }

    public void setPhone(String phone) {
        put("phone", Assert.notBlank(phone, "手机号码不能为空"));
    }

}
