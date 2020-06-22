package org.spin.wx;

import org.junit.jupiter.api.Test;
import org.spin.wx.base.MessageData;
import org.spin.wx.base.SubscribeMsgEntity;
import org.spin.wx.base.WxConfigInfo;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2020/6/22</p>
 *
 * @author xuweinan
 * @version 1.0
 */
class WxHelperTest {

    @Test
    void testSubscribeMsg() {
        String openid = "oYjv40HwT6fLYBf0AHg0StCuINQs";
        WxConfigInfo configInfo = new WxConfigInfo();
        configInfo.setAppId("wxd8fd717ca8acb775");
        configInfo.setAppSecret("6a565df9b24053cc8137be6ae46b64de");
        WxConfigManager.putConfig("default", configInfo);
        SubscribeMsgEntity msgEntity = new SubscribeMsgEntity();
        msgEntity.setTouser(openid);
        msgEntity.setMiniprogramState("developer");
        msgEntity.setTemplateId("TiYytZt7xC86qKUjMSH2Sp8yaVYgKwBamzVOHzBo4iM");
        MessageData data = new MessageData();
        data.put("thing1", "mmp").put("date2", "2020-06-22 16:00:00");
        msgEntity.setData(data);
        WxHelper.sendSubscribeMsg(msgEntity);
    }
}
