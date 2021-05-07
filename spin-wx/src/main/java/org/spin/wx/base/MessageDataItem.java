package org.spin.wx.base;

import java.io.Serializable;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2020/6/22</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class MessageDataItem implements Serializable {
    private String value;

    public MessageDataItem() {
    }

    public MessageDataItem(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
