package org.spin.wx.base;

import java.util.HashMap;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2020/6/22</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class MessageData extends HashMap<String, MessageDataItem> {

    public MessageData put(String key, String value) {
        this.put(key, new MessageDataItem(value));
        return this;
    }
    
}
