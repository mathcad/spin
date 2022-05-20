package org.spin.wx;

import org.spin.core.ErrorCode;
import org.spin.wx.throwable.WxException;

import java.util.Objects;

/**
 * 微信返回结果
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2021/12/13</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class WxResponse<T> {
    private Integer errcode;
    private String errmsg;
    private T data;

    public boolean isOk() {
        return Objects.equals(errcode, 0);
    }

    public WxException toException() {
        return new WxException(ErrorCode.with(this.getErrcode(), this.getErrmsg()));
    }

    public Integer getErrcode() {
        return errcode;
    }

    public void setErrcode(Integer errcode) {
        this.errcode = errcode;
    }

    public String getErrmsg() {
        return errmsg;
    }

    public void setErrmsg(String errmsg) {
        this.errmsg = errmsg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
